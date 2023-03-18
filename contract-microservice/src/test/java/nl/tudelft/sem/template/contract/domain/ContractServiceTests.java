package nl.tudelft.sem.template.contract.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatException;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import nl.tudelft.sem.template.contract.authentication.AuthManager;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;
import nl.tudelft.sem.template.contract.models.HashedPassword;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class ContractServiceTests {

    private ContractService contractService;
    private ContractRepository mockContractRepository;
    private AuthManager mockAuthManager;
    private Contract mockContract;


    @BeforeEach
    public void setup() {
        mockContractRepository = mock(ContractRepository.class);
        mockAuthManager = mock(AuthManager.class);
        mockContract = mock(Contract.class);
        contractService = new ContractService(mockContractRepository, mockAuthManager);

        when(mockContractRepository.save(any())).then(returnsFirstArg());
    }


    /*
    @Test
    void createCandidateTest() throws ContractHandlerException {
        //Arrange
        when(mockContract.getCandidateNetId()).thenReturn("NetId123");
        ResponseEntity<String> expected = ResponseEntity.ok().body("Contract created for candidate with netId"
                + mockContract.getCandidateNetId());

        //Act
        ResponseEntity<String> actual =
                contractService.createCandidate(mockContract, "exampleSecret", "canNetId",
                        new HashedPassword("canPass"), "Candidate");

        //Verify
        verify(mockContractRepository, times(1)).save(mockContract);
        assertThat(actual).isEqualTo(expected);
    }
    */



    @Test
    void proposeNullIdTest() throws ContractHandlerException {
        //Arrange
        Contract mockNewContract = mock(Contract.class);
        when(mockNewContract.toString()).thenReturn("newContract");

        when(mockContract.getId()).thenReturn(null);
        when(mockContract.getCandidateNetId()).thenReturn("NetIdCandidate123");

        when(mockAuthManager.getNetId()).thenReturn("NetId123");

        //Act
        ThrowableAssert.ThrowingCallable action = () -> contractService.propose(mockContract, "jwt");

        //Verify
        verify(mockContractRepository, never()).save(mockNewContract);
        assertThatExceptionOfType(ContractHandlerException.class)
                .isThrownBy(action);

    }

    @Test
    void proposeNoContractTest() throws ContractHandlerException {
        //Arrange
        when(mockContract.getId()).thenReturn(1L);
        when(mockContractRepository.findById(mockContract.getId())).thenReturn(Optional.empty());

        ResponseEntity<String> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //Act
        ThrowableAssert.ThrowingCallable action = () -> contractService.propose(mockContract, "jwt");

        //Verify
        verify(mockContractRepository, never()).save(any(Contract.class));
        assertThatExceptionOfType(ContractHandlerException.class)
                .isThrownBy(action);
    }

    @Disabled
    @Test
    void proposeContractReviewerIsEmployerTest() throws ContractHandlerException {
        //Arrange
        Contract mockExistingContract = mock(Contract.class);

        when(mockExistingContract.toString()).thenReturn("newContract");

        when(mockContract.getId()).thenReturn(1L);
        when(mockContract.getCandidateNetId()).thenReturn("NetIdCandidate123");
        when(mockExistingContract.getReviewer()).thenReturn(Contract.Reviewer.EMPLOYER);

        when(mockAuthManager.getNetId()).thenReturn("NetId123");
        when(mockContractRepository.findById(mockContract.getId())).thenReturn(Optional.of(mockExistingContract));

        ResponseEntity<String> expected = ResponseEntity.ok().body("newContract");

        //Act
        ResponseEntity<String> actual = contractService.propose(mockContract, "jwt");

        //Verify
        verify(mockContractRepository, times(1)).save(mockExistingContract);
        verify(mockExistingContract, times(1)).setReviewer(Contract.Reviewer.CANDIDATE);
        assertThat(actual).isEqualTo(expected);

    }


    @Disabled
    @Test
    void proposeContractReviewerIsCandidateTest() throws ContractHandlerException {
        //Arrange
        Contract mockExistingContract = mock(Contract.class);

        when(mockExistingContract.toString()).thenReturn("newContract");

        when(mockContract.getId()).thenReturn(1L);
        when(mockContract.getCandidateNetId()).thenReturn("NetIdCandidate123");
        when(mockExistingContract.getReviewer()).thenReturn(Contract.Reviewer.CANDIDATE);

        when(mockAuthManager.getNetId()).thenReturn("NetId123");
        when(mockContractRepository.findById(mockContract.getId())).thenReturn(Optional.of(mockExistingContract));

        ResponseEntity<String> expected = ResponseEntity.ok().body("newContract");

        //Act
        ResponseEntity<String> actual = contractService.propose(mockContract, "jwt");

        //Verify
        verify(mockContractRepository, times(1)).save(mockExistingContract);
        verify(mockExistingContract, times(1)).setReviewer(Contract.Reviewer.EMPLOYER);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void updateContractStatusNoContractTest() {
        //Arrange
        when(mockContractRepository.findById(anyLong())).thenReturn(Optional.empty());
        ResponseEntity<String> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        //Act
        ResponseEntity<String> actual = contractService.updateContractStatus(1L, Contract.State.ACCEPTED);

        //Verify
        assertThat(actual).isEqualTo(expected);
        verify(mockContractRepository, never()).save(any());
    }


    @Test
    void updateContractStatusNotAuthorizedCandidateTest() {
        //Arrange
        when(mockContractRepository.findById(anyLong())).thenReturn(Optional.of(mockContract));
        when(mockAuthManager.getNetId()).thenReturn("Candidate");
        when(mockAuthManager.getRole()).thenReturn("Candidate");

        when(mockContract.getState()).thenReturn(Contract.State.DRAFT);
        when(mockContract.getReviewer()).thenReturn(Contract.Reviewer.EMPLOYER);
        ResponseEntity<String> expected = ResponseEntity.status(401).body("Unauthorized");

        //Act
        ResponseEntity<String> actual = contractService.updateContractStatus(1L, Contract.State.ACCEPTED);

        //Verify
        assertThat(actual).isEqualTo(expected);
        verify(mockContract, never()).setState(any(Contract.State.class));
        verify(mockContractRepository, never()).save(any());
    }


    @Test
    void updateContractStatusNotAuthorizedEmployerTest() {
        //Arrange
        when(mockContractRepository.findById(anyLong())).thenReturn(Optional.of(mockContract));
        when(mockAuthManager.getNetId()).thenReturn("Employer");

        when(mockContract.getState()).thenReturn(Contract.State.DRAFT);
        when(mockContract.getReviewer()).thenReturn(Contract.Reviewer.CANDIDATE);
        when(mockContract.getCandidateNetId()).thenReturn("Candidate");
        ResponseEntity<String> expected = ResponseEntity.status(401).body("Unauthorized");

        //Act
        ResponseEntity<String> actual = contractService.updateContractStatus(1L, Contract.State.ACCEPTED);

        //Verify
        assertThat(actual).isEqualTo(expected);
        verify(mockContract, never()).setState(any(Contract.State.class));
        verify(mockContractRepository, never()).save(any());
    }

    @Test
    void updateContractStatusNotDraftTest1() {
        //Arrange
        when(mockContractRepository.findById(anyLong())).thenReturn(Optional.of(mockContract));
        when(mockContract.getState()).thenReturn(Contract.State.ACCEPTED);
        ResponseEntity<String> expected = ResponseEntity.status(401).body("Contract is not of type DRAFT");

        //Act
        ResponseEntity<String> actual = contractService.updateContractStatus(1L, Contract.State.ACCEPTED);

        //Verify
        assertThat(actual).isEqualTo(expected);
        verify(mockContract, never()).setState(any(Contract.State.class));
        verify(mockContractRepository, never()).save(any());
    }


    @Test
    void updateContractStatusNotDraftTest2() {
        //Arrange
        when(mockContractRepository.findById(anyLong())).thenReturn(Optional.of(mockContract));
        when(mockContract.getState()).thenReturn(Contract.State.TERMINATED);
        ResponseEntity<String> expected = ResponseEntity.status(401).body("Contract is not of type DRAFT");

        //Act
        ResponseEntity<String> actual = contractService.updateContractStatus(1L, Contract.State.ACCEPTED);

        //Verify
        assertThat(actual).isEqualTo(expected);
        verify(mockContract, never()).setState(any(Contract.State.class));
        verify(mockContractRepository, never()).save(any());
    }


    @Test
    void updateContractTest1() {
        //Arrange
        when(mockContractRepository.findById(anyLong())).thenReturn(Optional.of(mockContract));
        when(mockAuthManager.getNetId()).thenReturn("Employer");
        when(mockAuthManager.getRole()).thenReturn("HR");

        when(mockContract.getState()).thenReturn(Contract.State.DRAFT);
        when(mockContract.getReviewer()).thenReturn(Contract.Reviewer.EMPLOYER);

        ResponseEntity<String> expected = ResponseEntity.ok().build();

        //Act
        ResponseEntity<String> actual = contractService.updateContractStatus(1L, Contract.State.ACCEPTED);

        //Verify
        assertThat(actual).isEqualTo(expected);
        verify(mockContract, times(1)).setState(Contract.State.ACCEPTED);
        verify(mockContractRepository, times(1)).save(mockContract);
    }


    @Test
    void updateContractTest2() {
        //Arrange
        when(mockContractRepository.findById(anyLong())).thenReturn(Optional.of(mockContract));
        when(mockAuthManager.getNetId()).thenReturn("Candidate");

        when(mockContract.getState()).thenReturn(Contract.State.DRAFT);
        when(mockContract.getReviewer()).thenReturn(Contract.Reviewer.CANDIDATE);
        when(mockContract.getCandidateNetId()).thenReturn("Candidate");
        ResponseEntity<String> expected = ResponseEntity.ok().build();

        //Act
        ResponseEntity<String> actual = contractService.updateContractStatus(1L, Contract.State.ACCEPTED);

        //Verify
        assertThat(actual).isEqualTo(expected);
        verify(mockContract, times(1)).setState(Contract.State.ACCEPTED);
        verify(mockContractRepository, times(1)).save(mockContract);
    }
}
