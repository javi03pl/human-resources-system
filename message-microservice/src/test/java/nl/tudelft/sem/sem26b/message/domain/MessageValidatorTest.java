package nl.tudelft.sem.sem26b.message.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import nl.tudelft.sem.sem26b.message.authentication.AuthManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

/**
 * Test class for the MessageValidator.
 */
public class MessageValidatorTest {

    private AuthManager mockAuthManager;

    private transient MessageValidator messageValidator;

    /**
     * Set up before each test.
     * Mock and inject dependencies.
     */
    @BeforeEach
    public void setUp() {
        mockAuthManager = Mockito.mock(AuthManager.class);
        messageValidator = new MessageValidator(mockAuthManager);
    }

    /**
     * Test message validation.
     * Happy flow per each type.
     */
    @Test
    public void validateMessageTestHappyFlow() {
        // sender is hr xor receiver is hr.
        messageValidator.checkMessageIsValid(
            messageProvider(MessageTarget.HR_TARGET_LABEL, "netId", MessageType.CONTRACT_PROPOSE));
        messageValidator.checkMessageIsValid(
            messageProvider("netId", MessageTarget.HR_TARGET_LABEL, MessageType.CONTRACT_PROPOSE));
        messageValidator.checkMessageIsValid(
            messageProvider(MessageTarget.HR_TARGET_LABEL, "netId", MessageType.CONTRACT_APPROVE));
        messageValidator.checkMessageIsValid(
            messageProvider("netId", MessageTarget.HR_TARGET_LABEL, MessageType.CONTRACT_APPROVE));

        // from HR to employee only.
        messageValidator.checkMessageIsValid(
            messageProvider(MessageTarget.HR_TARGET_LABEL, "netId", MessageType.CONTRACT_TERMINATE));
        messageValidator.checkMessageIsValid(
            messageProvider(MessageTarget.HR_TARGET_LABEL, "netId", MessageType.LEAVE_APPROVE));

        // from employee to HR only.
        messageValidator.checkMessageIsValid(
            messageProvider("netId", MessageTarget.HR_TARGET_LABEL, MessageType.DOCUMENT_REQUEST));
        messageValidator.checkMessageIsValid(
            messageProvider("netId", MessageTarget.HR_TARGET_LABEL, MessageType.LEAVE_REQUEST));
        messageValidator.checkMessageIsValid(
            messageProvider("netId", MessageTarget.HR_TARGET_LABEL, MessageType.CONTRACT_TERMINATE_REQUEST));

        // from anyone to anyone.
        messageValidator.checkMessageIsValid(
            messageProvider(MessageTarget.HR_TARGET_LABEL, "netId", MessageType.OTHER));
        messageValidator.checkMessageIsValid(
            messageProvider("netId", "netId2", MessageType.OTHER));
        messageValidator.checkMessageIsValid(
            messageProvider("netId", MessageTarget.HR_TARGET_LABEL, MessageType.OTHER));
    }

    /**
     * Test message validation.
     * Sender and receiver are the same, should throw exception.
     */
    @Test
    public void validateMessageTestSendingToYourself() {
        for (MessageType type : MessageType.values()) {
            // user to user
            Message messageFromUserToUser = messageProvider("netId", "netId", type);
            assertThatThrownBy(() -> messageValidator.checkMessageIsValid(messageFromUserToUser))
                .isInstanceOf(ResponseStatusException.class);

            // hr to hr
            Message messageFromHrToHr =
                messageProvider(MessageTarget.HR_TARGET_LABEL, MessageTarget.HR_TARGET_LABEL, type);
            assertThatThrownBy(() -> messageValidator.checkMessageIsValid(messageFromHrToHr))
                .isInstanceOf(ResponseStatusException.class);
        }
    }

    /**
     * Test message validation.
     * Sender and receiver do not agree with the type, should throw exception.
     */
    @Test
    public void validateMessageTestWrongType() {
        // sender is hr xor receiver is hr (supposed to).
        // test temporarily disabled due to connection issues in contract service.
        //        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
        //            messageProvider("netId", "netId2", MessageType.CONTRACT_PROPOSE)
        //        )).isInstanceOf(ResponseStatusException.class);
        //        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
        //            messageProvider("netId", "netId2", MessageType.CONTRACT_APPROVE)
        //        )).isInstanceOf(ResponseStatusException.class);

        // from HR to employee only (supposed to).
        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
            messageProvider("netId", "netId2", MessageType.CONTRACT_TERMINATE)
        )).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
            messageProvider("netId", MessageTarget.HR_TARGET_LABEL, MessageType.CONTRACT_TERMINATE)
        )).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
            messageProvider("netId", "netId2", MessageType.LEAVE_APPROVE)
        )).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
            messageProvider("netId", MessageTarget.HR_TARGET_LABEL, MessageType.LEAVE_APPROVE)
        )).isInstanceOf(ResponseStatusException.class);

        // from employee to HR only (supposed to).
        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
            messageProvider("netId", "netId2", MessageType.DOCUMENT_REQUEST)
        )).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
            messageProvider(MessageTarget.HR_TARGET_LABEL, "netId2", MessageType.DOCUMENT_REQUEST)
        )).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
            messageProvider("netId", "netId2", MessageType.LEAVE_REQUEST)
        )).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
            messageProvider(MessageTarget.HR_TARGET_LABEL, "netId2", MessageType.LEAVE_REQUEST)
        )).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
            messageProvider("netId", "netId2", MessageType.CONTRACT_TERMINATE_REQUEST)
        )).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> messageValidator.checkMessageIsValid(
            messageProvider(MessageTarget.HR_TARGET_LABEL, "netId2", MessageType.CONTRACT_TERMINATE_REQUEST)
        )).isInstanceOf(ResponseStatusException.class);
    }

    /**
     * Test checkHrPermission method.
     * The user is HR, no exception should be thrown.
     */
    @Test
    public void checkHrPermissionTestIsHr() {
        // Arrange
        when(mockAuthManager.getRole()).thenReturn("erhfvreiHRwNBVJUJJ");

        // Act
        messageValidator.checkHrPermission();

        // Assert
        verify(mockAuthManager, times(1)).getRole();
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test checkHrPermission method.
     * The user is not HR, exception should be thrown.
     */
    @Test
    public void checkHrPermissionTestIsNotHr() {
        // Arrange
        when(mockAuthManager.getRole()).thenReturn("erhfvreiwNBVJUJJ");

        // Act
        assertThatThrownBy(() -> messageValidator.checkHrPermission())
            .isInstanceOf(ResponseStatusException.class);

        // Assert
        verify(mockAuthManager, times(1)).getRole();
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test checkMailboxPermission method.
     * The user is HR, no exception should be thrown.
     */
    @Test
    public void checkMailboxPermissionTestIsHr() {
        // Arrange
        when(mockAuthManager.getRole()).thenReturn("erhfvreiHRwNBVJUJJ");

        // Act
        messageValidator.checkMailboxPermission("netId");

        // Assert
        verify(mockAuthManager, times(1)).getRole();
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test checkMailboxPermission method.
     * The user is not HR but owner of mailbox, no exception should be thrown.
     */
    @Test
    public void checkMailboxPermissionTestIsOwner() {
        // Arrange
        when(mockAuthManager.getRole()).thenReturn("erhfvreiwNBVJUJJ");
        when(mockAuthManager.getNetId()).thenReturn("netId");

        // Act
        messageValidator.checkMailboxPermission("netId");

        // Assert
        verify(mockAuthManager, times(1)).getRole();
        verify(mockAuthManager, times(1)).getNetId();
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test checkMailboxPermission method.
     * The user is not HR nor the owner of mailbox, exception should be thrown.
     */
    @Test
    public void checkMailboxPermissionTestNoAccess() {
        // Arrange
        when(mockAuthManager.getRole()).thenReturn("erhfvreiwNBVJUJJ");
        when(mockAuthManager.getNetId()).thenReturn("netId");

        // Act
        assertThatThrownBy(() -> messageValidator.checkMailboxPermission("netId2"))
            .isInstanceOf(ResponseStatusException.class);

        // Assert
        verify(mockAuthManager, times(1)).getRole();
        verify(mockAuthManager, times(1)).getNetId();
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test the checkReadPermission method.
     * The user is admin, no exception thrown.
     */
    @Test
    public void checkReadPermissionTestAdminCase() {
        // Arrange
        when(mockAuthManager.getRole()).thenReturn("HR and stuff");
        Message message = messageProvider("netId", "netId2", MessageType.OTHER);

        // Act
        messageValidator.checkReadPermission(message);

        // Verify
        verify(mockAuthManager, times(1)).getRole();
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test the checkReadPermission method.
     * The user is not admin, but sender, no exception thrown.
     */
    @Test
    public void checkReadPermissionTestSenderCase() {
        // Arrange
        when(mockAuthManager.getRole()).thenReturn("stuff");
        when(mockAuthManager.getNetId()).thenReturn("netId");
        Message message = messageProvider("netId", "netId2", MessageType.OTHER);

        // Act
        messageValidator.checkReadPermission(message);

        // Verify
        verify(mockAuthManager, times(1)).getRole();
        verify(mockAuthManager, times(1)).getNetId();
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test the checkReadPermission method.
     * The user is not admin, but receiver, no exception thrown.
     */
    @Test
    public void checkReadPermissionTestReceiverCase() {
        // Arrange
        when(mockAuthManager.getRole()).thenReturn("stuff");
        when(mockAuthManager.getNetId()).thenReturn("netId2");
        Message message = messageProvider("netId", "netId2", MessageType.OTHER);

        // Act
        messageValidator.checkReadPermission(message);

        // Verify
        verify(mockAuthManager, times(1)).getRole();
        verify(mockAuthManager, times(1)).getNetId();
        verifyNoMoreInteractions(mockAuthManager);
    }


    /**
     * Test the checkReadPermission method.
     * The user has no access, exception thrown.
     */
    @Test
    public void checkReadPermissionTestNoAccess() {
        // Arrange
        when(mockAuthManager.getRole()).thenReturn("stuff");
        when(mockAuthManager.getNetId()).thenReturn("netId3");
        Message message = messageProvider("netId", "netId2", MessageType.OTHER);

        // Act
        assertThatThrownBy(() -> messageValidator.checkReadPermission(message)).isInstanceOf(
            ResponseStatusException.class);

        // Verify
        verify(mockAuthManager, times(1)).getRole();
        verify(mockAuthManager, times(1)).getNetId();
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * A function that provides a message with given parameters.
     *
     * @param sender   The sender of the message.
     * @param receiver The receiver of the message.
     * @param type     The type of the message.
     * @return A message with given parameters.
     */
    private Message messageProvider(String sender, String receiver, MessageType type) {
        return new Message(new MessageTarget(sender), new MessageTarget(receiver), type, "test",
            Instant.ofEpochMilli(12L));
    }
}
