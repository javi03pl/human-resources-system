package nl.tudelft.sem.template.contract.controllers;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import nl.tudelft.sem.template.contract.authentication.AuthManager;
import nl.tudelft.sem.template.contract.domain.Contract;
import nl.tudelft.sem.template.contract.domain.ContractRepository;
import nl.tudelft.sem.template.contract.domain.ContractService;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;
import nl.tudelft.sem.template.contract.handlers.Handler;
import nl.tudelft.sem.template.contract.models.CreateCandidateRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;


class CandidateControllerTests {
    private CandidateController candidateController;
    private AuthManager mockAuthManager;
    private ContractService mockContractService;
    private ContractRepository mockContractRepository;
    private Contract mockContract;
    private Handler mockHandler;

    @BeforeEach
    public void setup() {
        mockAuthManager = mock(AuthManager.class);
        mockContractRepository = mock(ContractRepository.class);
        mockHandler = mock(Handler.class);
        mockContract = mock(Contract.class);
        mockContractService = mock(ContractService.class);

        candidateController = new CandidateController(mockAuthManager, mockContractService,
             mockHandler);

    }

    @Test
    public void createCandidateTestFail() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenThrow(ContractHandlerException.class);


        CreateCandidateRequestModel model = new CreateCandidateRequestModel(mockContract,
            "netId1", "password1");

        //Act
        ResponseEntity<String> actual = candidateController.createCandidate("jwt", model);

        //Verify
        assertThat(actual).toString().contains(ResponseEntity.status(400).build().toString());
        verify(mockContractService, never()).createCandidate(any(Contract.class),
            anyString(), anyString(), anyString());
    }

    @Test
    public void createCandidateTestFail_emptyParam1() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenThrow(ContractHandlerException.class);

        //Act
        ResponseEntity<String> actual = candidateController.createCandidate("jwt", null);

        //Verify
        assertThat(actual).isEqualTo(ResponseEntity.status(400)
                .body("The request body and JWT token cannot be empty!\n"));
        verify(mockContractService, never()).createCandidate(any(Contract.class),
                anyString(), anyString(), anyString());
    }

    @Test
    public void createCandidateTestFail_emptyParam2() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenThrow(ContractHandlerException.class);


        CreateCandidateRequestModel model = new CreateCandidateRequestModel(mockContract,
                "netId1", "password1");

        //Act
        ResponseEntity<String> actual = candidateController.createCandidate("", model);

        //Verify
        assertThat(actual).isEqualTo(ResponseEntity.status(400)
                .body("The request body and JWT token cannot be empty!\n"));
        verify(mockContractService, never()).createCandidate(any(Contract.class),
                anyString(), anyString(), anyString());
    }

    @Test
    public void createCandidateTestSuccess() throws ContractHandlerException {

        //Arrange
        when(mockHandler.handle(any())).thenReturn(true);
        when(mockContractService.createCandidate(any(Contract.class), anyString(), anyString(), anyString()))
            .thenReturn(ResponseEntity.ok().body("success"));

        CreateCandidateRequestModel model = new CreateCandidateRequestModel(
            mockContract, "netId1", "password1");

        //Act
        candidateController.createCandidate("jwt", model);

        //Verify
        verify(mockContractService, times(1)).createCandidate(eq(mockContract),
            anyString(), anyString(), anyString());
    }
}
