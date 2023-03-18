package nl.tudelft.sem.template.contract.controllers;

import static java.util.Optional.empty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
import nl.tudelft.sem.template.contract.models.HashedPassword;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;


class ContractControllerTests {
    private ContractController contractController;
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

        contractController = new ContractController(mockAuthManager, mockContractService, mockContractRepository,
            mockHandler, mockHandler, mockHandler);

    }

    @Test
    public void getContractTestValidId() {
        //Arrange
        Contract mockContract = mock(Contract.class);
        Long id = 0L;
        when(mockContractRepository.findById(id)).thenReturn(Optional.ofNullable(mockContract));
        when(mockContractRepository.findById(not(eq(id)))).thenReturn(empty());

        //Act
        Optional<Contract> actual = contractController.getContractById(id);

        //Verify
        assertThat(actual).isEqualTo(Optional.ofNullable(mockContract));

    }


    @Test
    public void getContractTestInvalidId() {
        //Arrange
        Contract mockContract = mock(Contract.class);
        Long id = 0L;
        when(mockContractRepository.findById(id)).thenReturn(Optional.ofNullable(mockContract));
        when(mockContractRepository.findById(not(eq(id)))).thenReturn(empty());

        //Act
        Optional<Contract> actual = contractController.getContractById(1L);

        //Verify
        assertThat(actual).isEqualTo(empty());
    }


    @Test
    public void proposeTestFail() throws ContractHandlerException {

        //Arrange
        when(mockHandler.handle(any())).thenThrow(ContractHandlerException.class);

        //Act
        ResponseEntity<String> actual = contractController.propose("jwt", mockContract);

        //Verify
        assertThat(actual).toString().contains(ResponseEntity.status(400).body("").toString());
        verify(mockContractService, never()).propose(any(Contract.class), eq("jwt"));
    }


    @Test
    public void proposeTestSuccess() throws ContractHandlerException {

        //Arrange
        when(mockHandler.handle(any())).thenReturn(true);

        //Act
        ResponseEntity<String> actual = contractController.propose("jwt", mockContract);

        //Verify
        verify(mockContractService, times(1)).propose(mockContract, "jwt");
    }

    @Test
    public void proposeTestFail_mut1() throws ContractHandlerException {

        //Arrange
        when(mockHandler.handle(any())).thenReturn(true);

        //Act

        ResponseEntity<String> actual = contractController.propose("jwt", null);

        //Verify
        assertThat(actual).isEqualTo(ResponseEntity.status(400)
                .body("Contract and JWT token cannot be empty!\n"));
        verify(mockContractService, never()).propose(any(Contract.class), eq("jwt"));
    }

    @Test
    public void proposeTestFail_mut2() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenReturn(true);

        //Act

        ResponseEntity<String> actual = contractController.propose("", mockContract);

        //Verify
        assertThat(actual).isEqualTo(ResponseEntity.status(400)
                .body("Contract and JWT token cannot be empty!\n"));
        verify(mockContractService, never()).propose(any(Contract.class), eq(""));
    }

    @Test
    public void acceptTestFail() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenThrow(ContractHandlerException.class);

        //Act
        ResponseEntity<String> actual = contractController.accept(1L, "jwt");

        //Verify
        assertThat(actual).toString().contains(ResponseEntity.status(400).body("").toString());
        verify(mockContractService, never()).accept(anyLong(), anyString());
    }

    @Test
    public void acceptTestFail_mut1() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenThrow(ContractHandlerException.class);

        //Act
        ResponseEntity<String> actual = contractController.accept(null, "jwt");

        //Verify
        assertThat(actual).isEqualTo(ResponseEntity.status(400)
                .body("Contract ID and JWT token cannot be empty!\n"));
        verify(mockContractService, never()).accept(anyLong(), anyString());
    }

    @Test
    public void acceptTestFail_mut2() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenThrow(ContractHandlerException.class);

        //Act
        ResponseEntity<String> actual = contractController.accept(1L, "");

        //Verify
        assertThat(actual).isEqualTo(ResponseEntity.status(400)
                .body("Contract ID and JWT token cannot be empty!\n"));
        verify(mockContractService, never()).accept(anyLong(), anyString());
    }

    @Test
    public void acceptTestSuccess() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenReturn(true);

        //Act
        ResponseEntity<String> actual = contractController.accept(1L, "jwt");

        //Verify
        verify(mockContractService, times(1)).accept(1L, "jwt");
    }

    @Test
    public void terminateTestFail() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenThrow(ContractHandlerException.class);

        //Act
        ResponseEntity<String> actual = contractController.terminate(1L, "jwt");

        //Verify
        assertThat(actual).toString().contains(ResponseEntity.status(400).body("").toString());
        verify(mockContractService, never()).terminate(anyLong(), anyString());

    }

    @Test
    public void terminateTestFail_mut1() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenThrow(ContractHandlerException.class);

        //Act
        ResponseEntity<String> actual = contractController.terminate(1L, "");

        //Verify
        assertThat(actual).isEqualTo(ResponseEntity.status(400)
                .body("Contract ID and JWT token cannot be empty!\n"));
        verify(mockContractService, never()).accept(anyLong(), anyString());
    }

    @Test
    public void terminateTestFail_mut2() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenThrow(ContractHandlerException.class);

        //Act
        ResponseEntity<String> actual = contractController.terminate(null, "jwt");

        //Verify
        assertThat(actual).isEqualTo(ResponseEntity.status(400)
                .body("Contract ID and JWT token cannot be empty!\n"));
        verify(mockContractService, never()).accept(anyLong(), anyString());
    }

    @Test
    public void terminateTestSuccess() throws ContractHandlerException {
        //Arrange
        when(mockHandler.handle(any())).thenReturn(true);

        //Act
        ResponseEntity<String> actual = contractController.terminate(1L, "jwt");

        //Verify
        verify(mockContractService, times(1)).terminate(1L, "jwt");
    }

}
