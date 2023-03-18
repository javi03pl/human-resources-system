package nl.tudelft.sem.template.contract.domain;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import nl.tudelft.sem.template.contract.authentication.AuthManager;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;
import nl.tudelft.sem.template.contract.models.AppUserRequestModel;
import nl.tudelft.sem.template.contract.models.MessagePayload;
import nl.tudelft.sem.template.contract.models.PostMessageRequestModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * A DDD service for registering a new user.
 */
@Service
public class ContractService {

    private final transient ContractRepository contractRepository;
    private final transient AuthManager authManager;
    private final transient ResponseEntity<String> errorUnauthorized = ResponseEntity.status(401).body("Unauthorized");
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String HTTPREQUEST_FAILED_MESSAGE = "HttpRequest failed";
    private final transient RestTemplate restTemplate = new RestTemplate();


    public ContractService(ContractRepository contractRepository, AuthManager authManager) {
        this.contractRepository = contractRepository;
        this.authManager = authManager;
    }

    /**
     * Method for creating and saving a new candidate employee.
     *
     * @param contract          the Contract for the candidate employee.
     * @param jwt               a JWT Token for handlers.
     * @param candidateNetId    the netID of the candidate.
     * @param candidatePassword The (hashed) password of the candidate.
     * @return 200 OK when the candidate is successfully created.
     *      401 Unauthorized if the user is not allowed to create a candidate.
     * @throws ContractHandlerException when the handlers fail.
     */
    public ResponseEntity<String> createCandidate(Contract contract, String jwt, String candidateNetId,
                                                  String candidatePassword) throws ContractHandlerException {
        //if chain was successful create and send HttpRequest to user service to create the new account
        String addCandidateUrl = "http://localhost:8083/authentication/users/candidate";
        HttpHeaders header = new HttpHeaders();
        header.setContentType(MediaType.APPLICATION_JSON);
        header.add(AUTHORIZATION_HEADER, jwt);
        //create httpEntity with the body and header
        HttpEntity<?> httpEntity =
            new HttpEntity<Object>(new AppUserRequestModel(candidateNetId, candidatePassword, "CANDIDATE"), header);
        //try sending request, if it was successful create initial contract and return success message,
        //otherwise throw HttpClientErrorException
        try {
            ResponseEntity<String> responseEntity =
                restTemplate.exchange(addCandidateUrl, HttpMethod.POST, httpEntity, String.class);
            if (responseEntity.getStatusCode() == HttpStatus.OK) {
                contractRepository.save(contract);
                return responseEntity;
            } else {
                return errorUnauthorized;
            }
        } catch (HttpClientErrorException errorCode) {
            throw (new ContractHandlerException(HTTPREQUEST_FAILED_MESSAGE + errorCode.getResponseBodyAsString()));
        }
    }

    /**
     * Method that returns the reviewer of a contract.
     *
     * @param id the ID of the contract.
     * @return 200 OK with the reviewer info in the body.
     *      NOT_FOUND if contract is not found.
     */
    public ResponseEntity<String> getReviewerByContractId(Long id) {
        Optional<Contract> contract = contractRepository.findById(id);
        return contract.map(value -> ResponseEntity.ok(value.getReviewer().toString()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Method that returns a contract found by its ID.
     *
     * @param id the ID of the contract to be found.
     * @return 200 OK with the contract in the body.
     *      NOT_FOUND if the contract couldn't be found.
     */
    public ResponseEntity<Contract> getContractById(Long id) {
        Optional<Contract> contract = contractRepository.findById(id);
        return contract.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Method for proposing a contract.
     *
     * @param contract The contract to be proposed.
     * @param jwt      JWT token vor checking authentication
     * @return 401 "Unauthorized" if a non-HR user attempts to create a new proposal.
     *      400 BAD_REQUEST if candidate employee does not exist.
     *      400 BAD_REQUEST if the existing contract is empty.
     *      401 "Unauthorized" if the existing contract is not a draft.
     *      401 "Unauthorized" if the user is not allowed to review the contract.
     *      200 OK otherwise
     * @throws ContractHandlerException Exception when handler chain fails
     */
    public ResponseEntity<String> propose(Contract contract, String jwt) throws ContractHandlerException {
        // Update existing proposal
        Optional<Contract> existingContractOptional = contractRepository.findById(contract.getId());
        if (existingContractOptional.isEmpty()) {
            throw new ContractHandlerException("Contract with this id does not exist");
        }
        Contract existingContract = existingContractOptional.get();

        // Update contract data
        try {
            setContractData(contract, existingContract);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        // Change reviewer to opposite party
        if (existingContract.getReviewer().equals(Contract.Reviewer.EMPLOYER)) {
            existingContract.setReviewer(Contract.Reviewer.CANDIDATE);
        } else {
            existingContract.setReviewer(Contract.Reviewer.EMPLOYER);
        }

        // Save contract
        contractRepository.save(existingContract);

        // Set fromHr flag
        Boolean fromHr = existingContract.getReviewer().equals(Contract.Reviewer.CANDIDATE);

        //set jwt as authorization header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION_HEADER, jwt);
        // Create the http request body as a MultiValueMap which will contain the PostMessageRequestModel
        String to;
        if (existingContract.getReviewer().equals(Contract.Reviewer.CANDIDATE)) {
            to = existingContract.getCandidateNetId();
        } else {
            to = "HR";
        }
        String type = "contr-prop";
        String contents = "A new contract proposal has been sent";
        List<MessagePayload> payload =
            List.of(new MessagePayload(MessagePayload.CONTRACT_MESSAGE_PAYLOAD_TYPE,
                    existingContract.getId().intValue()));
        PostMessageRequestModel reqModel = new PostMessageRequestModel(to, type, contents, payload);
        try {
            // Send message to message service
            return this.sendMessage(headers, reqModel, fromHr);
        } catch (HttpClientErrorException errorCode) {
            throw (new ContractHandlerException(HTTPREQUEST_FAILED_MESSAGE + errorCode.getResponseBodyAsString()));
        }
    }

    /**
     * Method that accepts a contract proposal, it first sends an http request to the message service indicating that
     * the user accepted the contract, then updates the status of the contract in the contract DB.
     *
     * @param contractId The ID of the contract.
     * @param jwt        token of the user
     * @return ErrorUnauthorized if updating the contract fails,
     *      200 OK otherwise.
     * @throws ContractHandlerException with reason if http request fails
     */
    public ResponseEntity<String> accept(Long contractId, String jwt) throws ContractHandlerException {
        Optional<Contract> existingContractOptional = contractRepository.findById(contractId);
        if (existingContractOptional.isEmpty()) {
            throw new ContractHandlerException("Contract with this id does not exist");
        }
        Contract existingContract = existingContractOptional.get();
        if (!existingContract.getState().equals(Contract.State.DRAFT)) {
            throw new ContractHandlerException("Contract is not in DRAFT state");
        }


        this.updateContractStatus(contractId, Contract.State.DRAFT);

        // Send message
        //Create Http request to send to message service
        Boolean fromHr = authManager.getRole().contains("HR");
        //set jwt as authorization header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION_HEADER, jwt);
        // Create the http request body
        String to;
        if (existingContract.getReviewer().equals(Contract.Reviewer.CANDIDATE)) {
            to = "HR";
        } else {
            to = existingContract.getCandidateNetId();
        }
        String type = "contr-appr";
        String contents = "Contract proposal has been approved";

        List<MessagePayload> payload =
            List.of(new MessagePayload(MessagePayload.CONTRACT_MESSAGE_PAYLOAD_TYPE, contractId.intValue()));
        PostMessageRequestModel reqModel = new PostMessageRequestModel(to, type, contents, payload);


        try {
            // Send message to message service
            return this.sendMessage(headers, reqModel, fromHr);
        } catch (HttpClientErrorException errorCode) {
            throw (new ContractHandlerException(HTTPREQUEST_FAILED_MESSAGE + errorCode.getResponseBodyAsString()));
        }
    }


    /**
     * Method for terminating a contract.
     *
     * @param contractId Contract ID to be terminated
     * @param jwt        JWT token for veryfing authentication
     * @return HTTP Response entity
     * @throws ContractHandlerException Exception when handler chain fails
     */
    public ResponseEntity<String> terminate(Long contractId, String jwt) throws ContractHandlerException {
        Optional<Contract> existingContractOptional = contractRepository.findById(contractId);
        if (existingContractOptional.isEmpty()) {
            throw new ContractHandlerException("Contract with this id does not exist");
        }
        Contract existingContract = existingContractOptional.get();
        // Check if contract is in DRAFT state


        if (!existingContract.getState().equals(Contract.State.DRAFT)) {
            throw new ContractHandlerException("Contract is not in DRAFT state");
        }


        // Update contract status
        this.updateContractStatus(contractId, Contract.State.TERMINATED);

        // Send message
        //Create Http request to send to message service
        Boolean fromHr = authManager.getRole().contains("HR");
        //set jwt as authorization header
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add(AUTHORIZATION_HEADER, jwt);
        // Create the http request body
        String to = existingContract.getCandidateNetId();
        String type = "contr-appr";
        String contents = "Contract proposal has been terminated";
        List<MessagePayload> payload =
            List.of(new MessagePayload(MessagePayload.CONTRACT_MESSAGE_PAYLOAD_TYPE, contractId.intValue()));
        PostMessageRequestModel reqModel = new PostMessageRequestModel(to, type, contents, payload);

        try {
            // Send message to message esrvice
            return this.sendMessage(headers, reqModel, fromHr);
        } catch (HttpClientErrorException errorCode) {
            throw (new ContractHandlerException(HTTPREQUEST_FAILED_MESSAGE + errorCode.getResponseBodyAsString()));
        }
    }

    /**
     * Method that updates the status of a contract.
     *
     * @param contractId The ID of the contract.
     * @param newState   The State of the contract (ACCEPTED, DRAFT, TERMINATED).
     * @return 400 BAD_REQUEST if contract doesn't exist,
     *      401 Unauthorized if the requester is not authorized to change the contract status,
     *      401 "Contract is not of type Draft" if the user tries to change an ACCEPTED or a TERMINATED contract,
     *      200 OK otherwise.
     */
    public ResponseEntity<String> updateContractStatus(Long contractId, Contract.State newState) {
        Optional<Contract> contractOptional = contractRepository.findById(contractId);
        if (contractOptional.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Contract contract = contractOptional.get();

        if (!contract.getState().equals(Contract.State.DRAFT)) {
            return ResponseEntity.status(401).body("Contract is not of type DRAFT");
        }
        String userNetId = authManager.getNetId();
        if (newState.equals(Contract.State.ACCEPTED) && !isReviewing(userNetId, contract, authManager.getRole())) {
            return errorUnauthorized;
        }
        contract.setState(newState);
        contractRepository.save(contract);
        return ResponseEntity.ok().build();
    }

    /**
     * Method for checking if a user is authorized to review a contract.
     *
     * @param userNetId NetID of user
     * @param contract  Draft contract
     * @return Boolean indicating user is authorized to review the current contract.
     */
    private boolean isReviewing(String userNetId, Contract contract, String role) {
        if (contract.getReviewer().equals(Contract.Reviewer.EMPLOYER)
            && !role.contains("HR")) {
            // Contract is being reviewed by opposite party
            return false;
        }
        // Contract is being reviewed by opposite party
        return !contract.getReviewer().equals(Contract.Reviewer.CANDIDATE)
            || Objects.equals(contract.getCandidateNetId(), userNetId);
    }


    /**
     * Method for setting fields on a draft contract.
     *
     * @param proposedContract Proposed contract
     * @param existingContract Existing contract to be modified
     */
    private void setContractData(Contract proposedContract, Contract existingContract) {
        existingContract.setContractAdditions(proposedContract.getContractAdditions());
        existingContract.setSalaryInfo(proposedContract.getSalaryInfo());
        existingContract.setStartDate(proposedContract.getStartDate());
        existingContract.setEndDate(proposedContract.getEndDate());
    }

    /**
     * Method for sending message to message microservice.
     *
     * @param headers HttpHeaders
     * @param model PostMessageRequestModal for sending message
     * @param fromHr Flag if message is fromHr
     * @return Message ID
     * @throws HttpClientErrorException Http error when sending to message service fails
     */
    private ResponseEntity<String> sendMessage(HttpHeaders headers, PostMessageRequestModel model,
                                               Boolean fromHr) throws HttpClientErrorException {
        //Before updating contract in db,
        //Create Http request to send to message service
        String sendUrl = "http://localhost:8083/messages/message/send";
        if (fromHr) {
            sendUrl += "?fromHr";
        }
        //create httpEntity with the body and header
        HttpEntity<PostMessageRequestModel> httpEntity = new HttpEntity<>(model, headers);
        //try sending http request to message service indicating a new proposal was created. If it was successful update
        //contract and return success message, otherwise throw HttpClientErrorException

        // Send HTTP request
        return restTemplate.exchange(sendUrl, HttpMethod.POST, httpEntity, String.class);
    }

}
