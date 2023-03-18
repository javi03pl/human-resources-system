package nl.tudelft.sem.template.contract.handlers;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import nl.tudelft.sem.template.contract.domain.Contract;
import nl.tudelft.sem.template.contract.domain.ContractRepository;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;


@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class IsReviewingPartyHandlerTests {

    @Autowired
    transient MockMvc mockMvc;
    @Autowired
    transient ContractRepository mockContractRepository;
    public static Handler isReviewingPartyHandler;

    @BeforeEach
    public void setup() {
        mockContractRepository = mock(ContractRepository.class);
        isReviewingPartyHandler = new IsReviewingPartyHandler(mockContractRepository);
    }

    @Test
    public void oppositePartyCandidate() {
        // Arrange
        Contract mockContract = mock(Contract.class);
        when(mockContract.getId()).thenReturn(0L);
        when(mockContract.getReviewer()).thenReturn(Contract.Reviewer.CANDIDATE);
        when(mockContract.getCandidateNetId()).thenReturn("netId2");


        when(mockContractRepository.findById(0L)).thenReturn(Optional.of(mockContract));

        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");
        data.put("role", "HR");
        data.put("contract", mockContract);

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
            .isThrownBy(() -> isReviewingPartyHandler.handle(data))
            .withMessage("You are not the reviewing party of this contract");
    }


    @Test
    public void oppositePartyEmployer() {
        // Arrange
        Contract mockContract = mock(Contract.class);
        when(mockContract.getId()).thenReturn(0L);
        when(mockContract.getReviewer()).thenReturn(Contract.Reviewer.EMPLOYER);
        when(mockContract.getCandidateNetId()).thenReturn("netId2");

        when(mockContractRepository.findById(0L)).thenReturn(Optional.of(mockContract));

        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");

        data.put("role", "Candidate");
        data.put("contract", mockContract);

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
            .isThrownBy(() -> isReviewingPartyHandler.handle(data))
            .withMessage("You are not the reviewing party of this contract");
    }

    @Test
    public void authorized() throws ContractHandlerException {
        // Arrange
        Contract mockContract = mock(Contract.class);
        when(mockContract.getId()).thenReturn(0L);
        when(mockContract.getReviewer()).thenReturn(Contract.Reviewer.CANDIDATE);
        when(mockContract.getCandidateNetId()).thenReturn("netId1");


        when(mockContractRepository.findById(0L)).thenReturn(Optional.of(mockContract));

        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");
        data.put("contract", mockContract);

        // Assert
        boolean actual = isReviewingPartyHandler.handle(data);
        assertThat(actual).isTrue();
    }


    @Test
    public void contractDoesNotExist() {
        // Arrange
        Contract mockContract = mock(Contract.class);
        when(mockContract.getId()).thenReturn(1L);
        when(mockContract.getReviewer()).thenReturn(Contract.Reviewer.CANDIDATE);
        when(mockContract.getCandidateNetId()).thenReturn("netId1");


        when(mockContractRepository.findById(0L)).thenReturn(Optional.of(mockContract));

        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");
        data.put("contract", mockContract);

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
            .isThrownBy(() -> isReviewingPartyHandler.handle(data)).withMessage("Contract does not exist");
    }

    @Test
    public void contractIdNullUnauthorized() {
        // Arrange
        Contract mockContract = mock(Contract.class);
        when(mockContract.getId()).thenReturn(null);
        when(mockContract.getReviewer()).thenReturn(Contract.Reviewer.CANDIDATE);
        when(mockContract.getCandidateNetId()).thenReturn("netId2");
        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");
        data.put("role", "");
        data.put("contract", mockContract);

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
            .isThrownBy(() -> isReviewingPartyHandler.handle(data))
            .withMessage("You are not allowed to create an initial proposal");
    }

    @Test
    public void contractIdNullAuthorized() {
        // Arrange
        Contract mockContract = mock(Contract.class);
        when(mockContract.getId()).thenReturn(null);
        when(mockContract.getReviewer()).thenReturn(Contract.Reviewer.CANDIDATE);
        when(mockContract.getCandidateNetId()).thenReturn("netId2");
        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");
        data.put("role", "HR");
        data.put("contract", mockContract);

        // Assert
        try {
            boolean actual = isReviewingPartyHandler.handle(data);
            assertThat(actual).isTrue();
        } catch (ContractHandlerException e) {
            Assertions.fail("Test failed because an exception is thrown");
        }
    }


}
