package nl.tudelft.sem.template.contract.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import nl.tudelft.sem.template.contract.authentication.AuthManager;
import nl.tudelft.sem.template.contract.domain.Contract;
import nl.tudelft.sem.template.contract.domain.ContractRepository;
import nl.tudelft.sem.template.contract.domain.ContractService;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;
import nl.tudelft.sem.template.contract.handlers.CheckNetIdUniqueHandler;
import nl.tudelft.sem.template.contract.handlers.Handler;
import nl.tudelft.sem.template.contract.handlers.IsAuthenticatedHandler;
import nl.tudelft.sem.template.contract.handlers.IsHrHandler;
import nl.tudelft.sem.template.contract.handlers.IsReviewingPartyHandler;
import nl.tudelft.sem.template.contract.handlers.ValidateCandidateDetailsHandler;
import nl.tudelft.sem.template.contract.handlers.ValidateContractHandler;
import nl.tudelft.sem.template.contract.models.CreateCandidateRequestModel;
import nl.tudelft.sem.template.contract.models.HashedPassword;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/**
 * This controller is responsible for all contract related processes.
 */
@RestController
public class ContractController {

    @Autowired
    private final transient ContractService contractService;
    private final transient AuthManager authManager;
    @Autowired
    private final transient ContractRepository contractRepository;

    // Create initial handlers for the chains of each task
    private final transient Handler proposeHandler;
    private final transient Handler acceptHandler;
    private final transient Handler terminateHandler;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String ROLE_FIELD = "role";

    /**
     * Instantiates a new controller.
     *
     * @param authManager Spring Security component used to authenticate and authorize the user
     */
    @Autowired
    public ContractController(AuthManager authManager, ContractService contractService,
                              ContractRepository contractRepository) {
        this.authManager = authManager;
        this.contractService = contractService;
        this.contractRepository = contractRepository;

        //Initialize and set next handlers for accepting a proposal
        this.acceptHandler = new IsAuthenticatedHandler();

        //Initialize and set next handlers for creating a new proposal
        (this.proposeHandler = new IsAuthenticatedHandler())
            .nextHandler(new IsReviewingPartyHandler(contractRepository))
            .nextHandler(new ValidateContractHandler(contractRepository));

        //Initialize and set next handlers for terminating a contract
        (this.terminateHandler = new IsAuthenticatedHandler())
            .nextHandler(new IsHrHandler());
    }


    /**
     * Instantiates a new controller.
     *
     * @param authManager the AuthManager entity to be used.
     * @param contractService the ContractService entity to be used.
     * @param contractRepository the ContractRepository entity to be used.
     * @param acceptHandler the Handler for accepting a contract.
     * @param proposeHandler the Handler for proposing a contract.
     * @param terminateHandler the Handler for terminating a contract.
     */
    public ContractController(AuthManager authManager, ContractService contractService,
                              ContractRepository contractRepository,
            Handler acceptHandler, Handler proposeHandler, Handler terminateHandler) {
        this.authManager = authManager;
        this.contractService = contractService;
        this.contractRepository = contractRepository;

        //Initialize and set next handlers for accepting a proposal
        this.acceptHandler = acceptHandler;

        //Initialize and set next handlers for creating a new proposal
        this.proposeHandler = proposeHandler;

        //Initialize and set next handlers for terminating a contract
        this.terminateHandler = terminateHandler;


    }

    @GetMapping("/test")
    public String test() {
        System.out.println(authManager.getNetId());
        return authManager.getNetId();
    }

    /**
     * Method for getting a contract by ID.
     *
     * @param id the identifier for the contract to be found.
     * @return An Optional Contract container. This will contain NULL if the contract is not found.
     */
    @GetMapping("/{id}")
    public Optional<Contract> getContractById(@PathVariable Long id) {
        return contractRepository.findById(id);
    }

    /**
     * Endpoint for proposing an update to a contract.
     * First executes the corresponding chain of responsibility to validate the request, if successful
     * The contractService will update contract and send an http request to the message service to notify the other
     * party
     *
     * @param contract The contract to be proposed.
     * @param jwt      JWT token for authentication
     * @return status 200 OK if successful
     *      status 400 with error message passed down by the chain
     * @throws ContractHandlerException when error occurs
     */
    @PostMapping(path = "/propose", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> propose(@RequestHeader(name = AUTHORIZATION_HEADER) String jwt,
                                          @RequestBody Contract contract) throws ContractHandlerException {


        if (contract == null || (jwt.equals(""))) {
            return ResponseEntity.status(400).body("Contract and JWT token cannot be empty!\n");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("netId", authManager.getNetId());
        data.put("contract", contract);
        data.put("jwt", jwt);
        data.put(ROLE_FIELD, authManager.getRole());
        //Run chain, if fails throws exception
        try {
            proposeHandler.handle(data);
            return this.contractService.propose(contract, jwt);
        } catch (ContractHandlerException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }

    }

    /**
     * Endpoint for accepting a contract.
     * First executes the corresponding chain of responsibility to validate the request, if successful will update
     * the contract status to ACCEPTED and send an http request to the message service to notify the other party
     *
     * @param contractId The ID of the contract to be accepted.
     * @param jwt        JWT token for authentication
     * @return status 200 OK if successful
     *      status 400 with error message passed down by the chain
     * @throws ContractHandlerException when error occurs
     */
    @GetMapping(path = "/{contractId}/accept", produces = {MediaType.APPLICATION_JSON_VALUE,
        MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> accept(@PathVariable Long contractId,
                                         @RequestHeader(name = AUTHORIZATION_HEADER) String jwt) {

        if (contractId == null || (jwt.equals(""))) {
            return ResponseEntity.status(400).body("Contract ID and JWT token cannot be empty!\n");
        }

        //Create map of data needed for chain
        Map<String, Object> data = new HashMap<>();
        data.put("contract", contractService.getContractById(contractId));
        data.put("netId", authManager.getNetId());
        data.put(ROLE_FIELD, authManager.getRole());
        //Run chain, if fails throws exception
        try {
            acceptHandler.handle(data);
            return contractService.accept(contractId, jwt);
        } catch (ContractHandlerException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    /**
     * Endpoint for terminating a contract.
     * Calling this endpoint will update the contract status to TERMINATED and send an http request to the message
     * servic to notify the other party
     *
     * @param id  The ID of the contract to be terminated.
     * @param jwt JWT token for authentication
     * @return status 200 OK if successful or
     *      status 400 with error message passed through the chain
     * @throws ContractHandlerException Throw ContractHandlerException when error occurs
     */
    @GetMapping(path = "/{id}/terminate", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> terminate(@PathVariable Long id, @RequestHeader(name = AUTHORIZATION_HEADER) String jwt) {

        if (id == null || (jwt.equals(""))) {
            return ResponseEntity.status(400).body("Contract ID and JWT token cannot be empty!\n");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("contractId", id);
        data.put(ROLE_FIELD, authManager.getRole());
        //Run chain, if fails throws exception
        try {
            terminateHandler.handle(data);
            return contractService.terminate(id, jwt);
        } catch (ContractHandlerException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
}
