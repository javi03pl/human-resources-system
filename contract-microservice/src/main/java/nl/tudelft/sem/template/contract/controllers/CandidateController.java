package nl.tudelft.sem.template.contract.controllers;

import java.util.HashMap;
import java.util.Map;
import nl.tudelft.sem.template.contract.authentication.AuthManager;
import nl.tudelft.sem.template.contract.domain.Contract;
import nl.tudelft.sem.template.contract.domain.ContractRepository;
import nl.tudelft.sem.template.contract.domain.ContractService;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;
import nl.tudelft.sem.template.contract.handlers.CheckNetIdUniqueHandler;
import nl.tudelft.sem.template.contract.handlers.Handler;
import nl.tudelft.sem.template.contract.handlers.IsAuthenticatedHandler;
import nl.tudelft.sem.template.contract.handlers.IsHrHandler;
import nl.tudelft.sem.template.contract.handlers.ValidateCandidateDetailsHandler;
import nl.tudelft.sem.template.contract.handlers.ValidateContractHandler;
import nl.tudelft.sem.template.contract.models.CreateCandidateRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is responsible for creating new candidates with an associated contract proposal.
 */
@RestController
public class CandidateController {

    private final transient AuthManager authManager;

    private final transient Handler createCandidateHandler;

    @Autowired
    private final transient ContractService contractService;

    /**
     * Initiates a new controller.
     *
     * @param authManager Spring Security component used to authenticate and authorize the user
     * @param contractRepository the ContractRepository entity to be used.
     * @param contractService the ContractService entity to be used.
     */
    @Autowired
    public CandidateController(AuthManager authManager,
                               ContractRepository contractRepository, ContractService contractService) {
        this.authManager = authManager;
        this.contractService = contractService;

        //Initialize and set next handlers for creating a new candidate and initial proposal
        (this.createCandidateHandler = new IsAuthenticatedHandler())
            .nextHandler(new IsHrHandler())
            .nextHandler(new ValidateCandidateDetailsHandler())
            .nextHandler(new ValidateContractHandler(contractRepository))
            .nextHandler(new CheckNetIdUniqueHandler());
    }

    /**
     * Initiates a new controller.
     * Used for testing purposes.
     *
     * @param authManager Spring Security component used to authenticate and authorize the user
     * @param contractService the ContractRepository entity to be used.
     * @param createCandidateHandler the ContractService entity to be used.
     */
    public CandidateController(
        AuthManager authManager,
        ContractService contractService,
        Handler createCandidateHandler
    ) {
        this.authManager = authManager;

        this.contractService = contractService;

        //Initialize and set next handlers for creating a new candidate and initial proposal
        this.createCandidateHandler = createCandidateHandler;

    }

    /**
     * Endpoint for creating a new candidate and contract.
     * First executes the corresponding chain of responsibility to validate the request, if successful will make an
     * Http Post request to the appUser service with their login credentials and add the contract to the database
     *
     * @param requestBody CreateCandidateRequestModel contains a Contract and the candidate's netId and password
     * @param jwt         JWT token for authentication
     * @return status 200 OK if successful
     *      status 400 with error message passed down by the chain
     *      status 400 when error occurs with handlers
     */
    @PostMapping(path = "/createCandidate", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> createCandidate(@RequestHeader(name = "Authorization") String jwt,
                                                  @RequestBody CreateCandidateRequestModel requestBody) {

        try {
            if (requestBody == null || jwt.equals("")) {
                return ResponseEntity.status(400).body("The request body and JWT token cannot be empty!\n");
            }
            //extract content from requestBody
            String candidateNetId = requestBody.getCandidateNetId();
            String candidatePassword = requestBody.getCandidatePassword();
            Contract contract = requestBody.getContract();
            //Create map of data needed for this particular chain
            Map<String, Object> data = new HashMap<>();
            data.put("netId", authManager.getNetId());
            data.put("role", authManager.getRole());
            data.put("candidateNetId", candidateNetId);
            data.put("candidatePassword", candidatePassword);
            data.put("jwt", jwt);
            data.put("contract", contract);
            
            contract.setCandidateNetId(candidateNetId);
            contract.setReviewer(Contract.Reviewer.CANDIDATE);
            contract.setState(Contract.State.DRAFT);
            //run chain, if chain fails ContractHandlerException will be thrown and following code wont execute
            createCandidateHandler.handle(data);
            ResponseEntity<String> createCandidateResponse =
                contractService.createCandidate(contract, jwt, candidateNetId, candidatePassword);
            return ResponseEntity.ok(createCandidateResponse.getBody() + "\n" + contract.toString());
        } catch (ContractHandlerException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
