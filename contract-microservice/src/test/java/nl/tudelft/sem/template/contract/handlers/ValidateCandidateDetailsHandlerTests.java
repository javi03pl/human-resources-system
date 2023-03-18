package nl.tudelft.sem.template.contract.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import nl.tudelft.sem.template.contract.domain.ContractRepository;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;
import org.assertj.core.api.Assertions;
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
public class ValidateCandidateDetailsHandlerTests {

    @Autowired
    transient MockMvc mockMvc;
    @Autowired
    transient ContractRepository mockContractRepository;
    public static Handler validateCandidateDetailsHandler;

    @BeforeEach
    public void setup() {
        mockContractRepository = mock(ContractRepository.class);
        validateCandidateDetailsHandler = new ValidateCandidateDetailsHandler();
    }

    @Test
    public void candidateValid() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        byte[] array = new byte[50];
        new Random().nextBytes(array);
        array[49] = '\n';
        String generatedNetId = new String(array, StandardCharsets.UTF_8);
        data.put("candidateNetId", generatedNetId);
        data.put("candidatePassword", "password123");

        // Assert
        try {
            boolean actual = validateCandidateDetailsHandler.handle(data);
            assertThat(actual).isTrue();
        } catch (ContractHandlerException e) {
            Assertions.fail("Test failed because an exception is thrown.");
        }

    }


    @Test
    public void candidateIdNull() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("candidateNetId", null);
        data.put("candidatePassword", "password123");

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
                .isThrownBy(() -> validateCandidateDetailsHandler.handle(data)).withMessage("NetId or password is null");
    }


    @Test
    public void candidatePasswordNull() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("candidateNetId", "netId1");
        data.put("candidatePassword", null);

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
            .isThrownBy(() -> validateCandidateDetailsHandler.handle(data)).withMessage("NetId or password is null");
    }

    @Test
    public void candidatePasswordEmpty() {
        // Arrange
        Map<String, Object> data = new HashMap<>();
        data.put("candidateNetId", "netId1");
        data.put("candidatePassword", "");

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
            .isThrownBy(() -> validateCandidateDetailsHandler.handle(data))
            .withMessage("NetId and password must be between 3 and 50 chars long");
    }

    @Test
    public void candidateIdLengthOver50() {
        // Arrange

        String generatedNetId = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"; // 51 chars

        Map<String, Object> data = new HashMap<>();
        data.put("candidateNetId", generatedNetId);
        data.put("candidatePassword", "password123");

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
            .isThrownBy(() -> validateCandidateDetailsHandler.handle(data))
             .withMessage("NetId and password must be between 3 and 50 chars long");
    }


    @Test
    public void candidatePassLengthOver50() {
        // Arrange

        String generatedNetId = "netId123";

        Map<String, Object> data = new HashMap<>();
        data.put("candidateNetId", generatedNetId);
        data.put("candidatePassword", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"); //51 chars

        // Assert
        assertThatExceptionOfType(ContractHandlerException.class)
                .isThrownBy(() -> validateCandidateDetailsHandler.handle(data))
                .withMessage("NetId and password must be between 3 and 50 chars long");
    }

}
