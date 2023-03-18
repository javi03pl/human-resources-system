package nl.tudelft.sem.template.contract.handlers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;
import nl.tudelft.sem.template.contract.domain.ContractRepository;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class IsHrHandlerTests {

    @Autowired
    transient MockMvc mockMvc;
    @Autowired
    transient ContractRepository contractRepository;
    public static Handler createCandidateHandler;
    public static Handler proposeHandler;
    public static Handler acceptHandler;
    public static Handler terminateHandler;

    @BeforeAll
    void createChains() {
        //Initialize and set next handlers for creating a proposal
        createCandidateHandler = new IsAuthenticatedHandler().nextHandler(
            new IsHrHandler().nextHandler(
                new ValidateCandidateDetailsHandler().nextHandler(
                    new CheckNetIdUniqueHandler()
                )
            )
        );

        //Initialize and set next handlers for accepting a proposal
        acceptHandler = new IsAuthenticatedHandler().nextHandler(new IsReviewingPartyHandler(contractRepository));

        //Initialize and set next handlers for creating a new proposal
        proposeHandler = new IsAuthenticatedHandler().nextHandler(
            new IsReviewingPartyHandler(contractRepository).nextHandler(
                new ValidateContractHandler(contractRepository)));

        //Initialize and set next handlers for accepting a proposal
        terminateHandler = new IsAuthenticatedHandler().nextHandler(new IsHrHandler());
    }

    @BeforeEach
    public void setup() {
        //reset and start mock servers
    }

    @AfterEach
    public void stopServers() {
        //stop mock servers
    }

    //We need tests to test each Handler
    @Test
    public void isHr() throws ContractHandlerException {
        Map<String, Object> data = new HashMap<>();
        data.put("role", "HR");
        // Assert
        proposeHandler = new IsHrHandler();
        boolean actual = proposeHandler.handle(data);
        assertThat(actual).isTrue();
    }

    @Test
    public void isNotHr() {
        Map<String, Object> data = new HashMap<>();
        data.put("role", "CANDIDATE");

        // Assert
        proposeHandler = new IsHrHandler();
        ContractHandlerException e = assertThrows(ContractHandlerException.class, () -> proposeHandler.handle(data));
        assertThat(e.getMessage().contentEquals("User is not HR"));
    }

    @Test
    public void roleIsNull() {
        Map<String, Object> data = new HashMap<>();
        data.put("role", null);

        // Assert
        proposeHandler = new IsHrHandler();
        ContractHandlerException e = assertThrows(ContractHandlerException.class, () -> proposeHandler.handle(data));
        assertThat(e.getMessage().contentEquals("User is not HR"));
    }

    @Test
    public void roleToStringContainsHr() {
        Map<String, Object> data = new HashMap<>();
        Object object = new ObjectWithoutToString();
        data.put("role", object);
        // Assert
        proposeHandler = new IsHrHandler();
        ContractHandlerException e = assertThrows(ContractHandlerException.class, () -> proposeHandler.handle(data));
        assertThat(e.getMessage().contentEquals("User is not HR"));
    }

    class ObjectWithoutToString extends Object {
        @Override
        public String toString() {
            return "Test_HR_Class";
        }
    }
}