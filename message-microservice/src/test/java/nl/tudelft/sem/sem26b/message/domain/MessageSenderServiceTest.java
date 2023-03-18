package nl.tudelft.sem.sem26b.message.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.LinkedList;
import nl.tudelft.sem.sem26b.message.authentication.AuthManager;
import nl.tudelft.sem.sem26b.message.models.PostMessageRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;

/**
 * Test class for MessageSenderService.
 */
public class MessageSenderServiceTest {

    private transient MessageRepository mockMessageRepository;
    private transient MessageValidator mockValidator;
    private transient AuthManager mockAuthManager;

    private transient MessageSenderService messageSenderService;

    /**
     * Set up for each test.
     * Mock and inject dependencies.
     */
    @BeforeEach
    public void setUp() {
        mockMessageRepository = Mockito.mock(MessageRepository.class);
        mockValidator = Mockito.mock(MessageValidator.class);
        mockAuthManager = Mockito.mock(AuthManager.class);
        messageSenderService = new MessageSenderService(mockMessageRepository, mockValidator, mockAuthManager);
    }

    /**
     * Test the postMessage method.
     * Happy flow, sending from HR.
     */
    @Test
    public void postMessageTestFromHr() {
        // Arrange
        PostMessageRequestModel payload = new PostMessageRequestModel();
        payload.setTo("1234");
        payload.setType(new MessageTypeAttributeConverter().convertToDatabaseColumn(MessageType.CONTRACT_APPROVE));
        payload.setContents("Hello world!");
        payload.setPayload(new LinkedList<>());
        when(mockAuthManager.getNetId()).thenReturn("someId");
        Message returnMessage = new Message(new MessageTarget("1234"), new MessageTarget("12344"),
            MessageType.CONTRACT_APPROVE, "Hello world!", Instant.now());
        when(mockMessageRepository.save(Mockito.any(Message.class))).thenReturn(returnMessage);

        // Act
        assertThat(messageSenderService.postMessage(payload, true)).isEqualTo(returnMessage);

        // Assert
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mockAuthManager, times(1)).getNetId();
        verify(mockValidator, times(1)).checkMessageIsValid(messageCaptor.capture());
        Message message = messageCaptor.getValue();
        verify(mockMessageRepository, times(1)).save(message);
        verifyNoMoreInteractions(mockMessageRepository);
        verifyNoMoreInteractions(mockValidator);
        verifyNoMoreInteractions(mockAuthManager);

        assertThat(message.getSender().isHr()).isTrue();
        assertThat(message.getReceiver().getNetId()).isEqualTo("1234");
        assertThat(message.getMessageType()).isEqualTo(MessageType.CONTRACT_APPROVE);
        assertThat(message.getContents()).isEqualTo("Hello world!");
        assertThat(message.getStatus().getSentAt()).isNotNull();
        assertThat(message.getStatus().getReadAt()).isNull();
        assertThat(message.getStatus().isWasRead()).isFalse();
    }

    /**
     * Test the postMessage method.
     * Happy flow, sending from a user.
     */
    @Test
    public void postMessageTestFromUser() {
        // Arrange
        PostMessageRequestModel payload = new PostMessageRequestModel();
        payload.setTo("1234");
        payload.setType(new MessageTypeAttributeConverter().convertToDatabaseColumn(MessageType.CONTRACT_APPROVE));
        String payloadContents = new String(new char[2000]).replace("\0", "a");
        payload.setContents(payloadContents);
        payload.setPayload(new LinkedList<>());
        when(mockAuthManager.getNetId()).thenReturn("someId");

        // Act
        messageSenderService.postMessage(payload, false);

        // Assert
        ArgumentCaptor<Message> messageCaptor = ArgumentCaptor.forClass(Message.class);
        verify(mockAuthManager, times(1)).getNetId();
        verify(mockValidator, times(1)).checkMessageIsValid(messageCaptor.capture());
        Message message = messageCaptor.getValue();
        verify(mockMessageRepository, times(1)).save(message);
        verifyNoMoreInteractions(mockMessageRepository);
        verifyNoMoreInteractions(mockValidator);
        verifyNoMoreInteractions(mockAuthManager);

        assertThat(message.getSender().getNetId()).isEqualTo("someId");
        assertThat(message.getReceiver().getNetId()).isEqualTo("1234");
        assertThat(message.getMessageType()).isEqualTo(MessageType.CONTRACT_APPROVE);
        assertThat(message.getContents()).isEqualTo(payloadContents);
        assertThat(message.getStatus().getSentAt()).isNotNull();
        assertThat(message.getStatus().getReadAt()).isNull();
        assertThat(message.getStatus().isWasRead()).isFalse();
    }

    /**
     * Test the postMessage method.
     * Test sending a message with contents too large.
     */
    @Test
    public void postMessageTestPayloadTooBig() {
        // Arrange
        PostMessageRequestModel payload = new PostMessageRequestModel();
        payload.setTo("1234");
        payload.setType(new MessageTypeAttributeConverter().convertToDatabaseColumn(MessageType.CONTRACT_APPROVE));
        payload.setContents(new String(new char[2001]).replace("\0", "a"));
        payload.setPayload(new LinkedList<>());
        when(mockAuthManager.getNetId()).thenReturn("someId");

        // Act&Assert
        assertThatThrownBy(() -> messageSenderService.postMessage(payload, false))
            .isInstanceOf(ResponseStatusException.class);
        verify(mockAuthManager, times(1)).getNetId();
        verifyNoMoreInteractions(mockMessageRepository);
        verifyNoMoreInteractions(mockValidator);
        verifyNoMoreInteractions(mockAuthManager);
    }

}
