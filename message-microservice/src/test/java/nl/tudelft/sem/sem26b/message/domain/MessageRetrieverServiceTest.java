package nl.tudelft.sem.sem26b.message.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import nl.tudelft.sem.sem26b.message.authentication.AuthManager;
import nl.tudelft.sem.sem26b.message.models.GetMessageResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for the MessageRetrieverService.
 */
public class MessageRetrieverServiceTest {

    private transient MessageRetrieverService retrieverService;

    private transient MessageRepository mockMessageRepository;
    private transient MessageValidator mockValidator;
    private transient AuthManager mockAuthManager;

    /**
     * Setup the test environment.
     * Mock and inject dependencies.
     */
    @BeforeEach
    public void setUp() {
        mockMessageRepository = Mockito.mock(MessageRepository.class);
        mockValidator = Mockito.mock(MessageValidator.class);
        mockAuthManager = Mockito.mock(AuthManager.class);
        retrieverService = new MessageRetrieverService(mockMessageRepository, mockValidator, mockAuthManager);
    }

    /**
     * Test the retrieveMessage method.
     * Repository returns a message.
     */
    @Test
    public void retrieveMessageTest() {
        // Arrange
        Message message = messageProvider(123);
        Optional<Message> messageOptional = Optional.of(message);
        when(mockMessageRepository.findById(21)).thenReturn(messageOptional);

        // Act&Assert
        assertThat(retrieverService.retrieveMessageById(21)).isEqualTo(message);
        verify(mockMessageRepository, times(1)).findById(21);
        verifyNoMoreInteractions(mockMessageRepository);
        verifyNoMoreInteractions(mockValidator);
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test the retrieveMessage method.
     * Repository returns an empty optional.
     */
    @Test
    public void retrieveMessageTestNullCase() {
        // Arrange
        Optional<Message> messageOptional = Optional.empty();
        when(mockMessageRepository.findById(221)).thenReturn(messageOptional);

        // Act&Assert
        assertThat(retrieverService.retrieveMessageById(221)).isNull();
        verify(mockMessageRepository, times(1)).findById(221);
        verifyNoMoreInteractions(mockMessageRepository);
        verifyNoMoreInteractions(mockValidator);
        verifyNoMoreInteractions(mockAuthManager);
    }


    /**
     * Test the openMessageIfApplicable method.
     * Case: message is for hr, user is hr - Message should be marked as opened.
     */
    @Test
    public void openMessageIfApplicableTestCase1() {
        // Arrange
        Message message = new Message(
            new MessageTarget("netId"), new MessageTarget(MessageTarget.HR_TARGET_LABEL),
            MessageType.OTHER, "body", Instant.ofEpochSecond(2L)
        );
        when(mockValidator.hasHrPermission()).thenReturn(true);

        // Act
        assertThat(message.getStatus().isWasRead()).isFalse();
        retrieverService.openMessageIfApplicable(message);

        // Assert
        assertThat(message.getStatus().isWasRead()).isTrue();
        verify(mockMessageRepository, times(1)).save(message);
        verify(mockValidator, times(1)).hasHrPermission();
        verifyNoMoreInteractions(mockMessageRepository);
        verifyNoMoreInteractions(mockValidator);
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test the openMessageIfApplicable method.
     * Case: message is for hr, user is not hr - Message should not be marked as opened.
     */
    @Test
    public void openMessageIfApplicableTestCase2() {
        // Arrange
        Message message = new Message(
            new MessageTarget("netId"), new MessageTarget(MessageTarget.HR_TARGET_LABEL),
            MessageType.OTHER, "body", Instant.ofEpochSecond(2L)
        );
        when(mockValidator.hasHrPermission()).thenReturn(false);

        // Act
        retrieverService.openMessageIfApplicable(message);

        // Assert
        assertThat(message.getStatus().isWasRead()).isFalse();
        verify(mockValidator, times(1)).hasHrPermission();
        verifyNoMoreInteractions(mockMessageRepository);
        verifyNoMoreInteractions(mockValidator);
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test the openMessageIfApplicable method.
     * Case: message is a user, user is sender - Message should not be marked as opened.
     */
    @Test
    public void openMessageIfApplicableTestCase3() {
        // Arrange
        Message message = new Message(
            new MessageTarget("netId"), new MessageTarget("netId2"),
            MessageType.OTHER, "body", Instant.ofEpochSecond(2L)
        );
        when(mockAuthManager.getNetId()).thenReturn("netId");

        // Act
        retrieverService.openMessageIfApplicable(message);

        // Assert
        assertThat(message.getStatus().isWasRead()).isFalse();
        verify(mockAuthManager, times(1)).getNetId();
        verifyNoMoreInteractions(mockMessageRepository);
        verifyNoMoreInteractions(mockValidator);
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test the openMessageIfApplicable method.
     * Case: message is a user, user is receiver - Message should be marked as opened.
     */
    @Test
    public void openMessageIfApplicableTestCase4() {
        // Arrange
        Message message = new Message(
            new MessageTarget("netId"), new MessageTarget("netId2"),
            MessageType.OTHER, "body", Instant.ofEpochSecond(2L)
        );
        when(mockAuthManager.getNetId()).thenReturn("netId2");

        // Act
        retrieverService.openMessageIfApplicable(message);

        // Assert
        assertThat(message.getStatus().isWasRead()).isTrue();
        verify(mockMessageRepository, times(1)).save(message);
        verify(mockAuthManager, times(1)).getNetId();
        verifyNoMoreInteractions(mockMessageRepository);
        verifyNoMoreInteractions(mockValidator);
        verifyNoMoreInteractions(mockAuthManager);
    }

    /**
     * Test the parseMessageToModel method.
     */
    @Test
    void parseMessageToModelTest() {
        Message message = messageProvider(12345);
        GetMessageResponseModel model = retrieverService.parseMessageToModel(message);

        assertThat(model.getMessageId()).isEqualTo(message.getId());
        assertThat(model.getSender()).isEqualTo(message.getSender().getNetId());
        assertThat(model.getReceiver()).isEqualTo(message.getReceiver().getNetId());
        assertThat(model.getMessageType()).isEqualTo(message.getMessageType().toString());
        assertThat(model.getContents()).isEqualTo(message.getContents());
        assertThat(model.getPayload()).isNullOrEmpty();
        assertThat(model.getSentAt()).isEqualTo(message.getStatus().getSentAt().toString());
    }


    /**
     * Generate a message for testing purposes.
     *
     * @param key a modifier for the message.
     * @return a message.
     */
    private Message messageProvider(int key) {
        return new Message(new MessageTarget("1234" + key), new MessageTarget("5678" + key),
            MessageType.OTHER, "Test message " + key, Instant.ofEpochSecond(key));
    }
}
