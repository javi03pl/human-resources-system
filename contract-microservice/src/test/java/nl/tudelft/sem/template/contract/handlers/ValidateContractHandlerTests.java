package nl.tudelft.sem.template.contract.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.Fail.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import nl.tudelft.sem.template.contract.domain.Contract;
import nl.tudelft.sem.template.contract.domain.ContractRepository;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ValidateContractHandlerTests {

    @Autowired
    transient MockMvc mockMvc;
    @Autowired
    transient ContractRepository mockContractRepository;
    public static Handler validateContractHandler;

    @BeforeEach
    public void setup() {
        mockContractRepository = mock(ContractRepository.class);
        validateContractHandler = new ValidateContractHandler(mockContractRepository);
    }

    @Test
    public void validateContractNoCandidateId() {
        // Arrange
        Contract mockContract = mock(Contract.class);
        when(mockContract.getId()).thenReturn(null);
        when(mockContract.getCandidateNetId()).thenReturn(null);

        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");
        data.put("contract", mockContract);

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
                .isThrownBy(() -> validateContractHandler.handle(data)).withMessage("No candidate ID");
    }

    @Test
    public void validateContractEmptyCandidateId() {
        // Arrange
        Contract mockContract = mock(Contract.class);
        when(mockContract.getId()).thenReturn(null);
        when(mockContract.getCandidateNetId()).thenReturn(new String());

        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");
        data.put("contract", mockContract);

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
                .isThrownBy(() -> validateContractHandler.handle(data)).withMessage("No candidate ID");
    }

    @Test
    public void validateContractNotDraft() {
        // Arrange
        Contract mockContract = mock(Contract.class);
        when(mockContract.getId()).thenReturn(0L);
        when(mockContract.getCandidateNetId()).thenReturn(null);
        when(mockContract.getState()).thenReturn(Contract.State.ACCEPTED);
        when(mockContractRepository.findById(0L)).thenReturn(Optional.of(mockContract));

        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");
        data.put("contract", mockContract);

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
                .isThrownBy(() -> validateContractHandler.handle(data))
            .withMessage("Contract must be of type DRAFT to propose");
    }

    @Test
    public void validateContractInvalid() {
        // Arrange
        Contract mockContract = mock(Contract.class);
        when(mockContract.getId()).thenReturn(0L);
        when(mockContract.getCandidateNetId()).thenReturn(null);
        when(mockContract.getState()).thenReturn(Contract.State.DRAFT);

        Date startDate = new Date(2022, Calendar.DECEMBER, 1);
        Date endDate = new Date(2021, Calendar.DECEMBER, 1);

        ReflectionTestUtils.setField(mockContract, "startDate", startDate);
        ReflectionTestUtils.setField(mockContract, "endDate", endDate);


        when(mockContractRepository.findById(0L)).thenReturn(Optional.of(mockContract));

        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");
        data.put("contract", mockContract);


        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
                .isThrownBy(() -> validateContractHandler.handle(data)).withMessage("This contract is not valid");
    }

    @Test
    public void validateContractValid() {
        // Arrange
        Contract mockContract = mock(Contract.class);
        when(mockContract.getId()).thenReturn(0L);
        when(mockContract.getCandidateNetId()).thenReturn(null);
        when(mockContract.getState()).thenReturn(Contract.State.DRAFT);

        Date startDate = new Date(2021, Calendar.DECEMBER, 1);
        Date endDate = new Date(2022, Calendar.DECEMBER, 1);

        ReflectionTestUtils.setField(mockContract, "startDate", startDate);
        ReflectionTestUtils.setField(mockContract, "endDate", endDate);


        when(mockContractRepository.findById(0L)).thenReturn(Optional.of(mockContract));

        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");
        data.put("contract", mockContract);


        // Assert
        try {
            boolean actual = validateContractHandler.handle(data);
            assertThat(actual).isTrue();
        } catch (ContractHandlerException e) {
            fail("Error is thrown by handler");
        }
    }

    @Test
    public void validateContractNoContract() {
        // Arrange
        Contract mockContract = null;
        Date startDate = new Date(2022, Calendar.DECEMBER, 1);


        Map<String, Object> data = new HashMap<>();
        data.put("netId", "netId1");
        data.put("contract", mockContract);


        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
                .isThrownBy(() -> validateContractHandler.handle(data))
                .withMessage("Contract is missing in request body");
    }
}
