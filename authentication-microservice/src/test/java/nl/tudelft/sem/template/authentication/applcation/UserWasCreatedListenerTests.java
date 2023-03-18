package nl.tudelft.sem.template.authentication.applcation;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import nl.tudelft.sem.template.authentication.application.user.UserWasCreatedListener;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.EmployeeType;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.UserLoggedInEvent;
import nl.tudelft.sem.template.authentication.domain.user.UserWasCreatedEvent;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class UserWasCreatedListenerTests {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void onAccountCreated() {
        AppUser user = new AppUser(new NetId("user"), new HashedPassword("password"), EmployeeType.EMPLOYEE);
        UserWasCreatedListener userWasCreatedListener = new UserWasCreatedListener();
        UserWasCreatedEvent event = new UserWasCreatedEvent(user.getNetId());

        userWasCreatedListener.onAccountWasCreated(event);
        assertThat(outContent.toString()).isEqualTo("Account (" + event.getNetId().toString() + ") was created.\n");

    }

    @Test
    public void onLogin() {
        AppUser user = new AppUser(new NetId("user"), new HashedPassword("password"), EmployeeType.EMPLOYEE);
        UserWasCreatedListener userWasCreatedListener = new UserWasCreatedListener();
        UserLoggedInEvent event = new UserLoggedInEvent(user.getNetId());

        userWasCreatedListener.onUserLogin(event);
        assertThat(outContent.toString()).isEqualTo("Account (" + event.getNetId().toString() + ") was created.\n");

    }
}
