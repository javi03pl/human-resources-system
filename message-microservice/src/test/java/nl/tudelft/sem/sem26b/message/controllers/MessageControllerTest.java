package nl.tudelft.sem.sem26b.message.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import nl.tudelft.sem.sem26b.message.domain.MailboxService;
import nl.tudelft.sem.sem26b.message.domain.Message;
import nl.tudelft.sem.sem26b.message.domain.MessagePayload;
import nl.tudelft.sem.sem26b.message.domain.MessageRetrieverService;
import nl.tudelft.sem.sem26b.message.domain.MessageSenderService;
import nl.tudelft.sem.sem26b.message.domain.MessageTarget;
import nl.tudelft.sem.sem26b.message.domain.MessageType;
import nl.tudelft.sem.sem26b.message.domain.MessageValidator;
import nl.tudelft.sem.sem26b.message.domain.PayloadService;
import nl.tudelft.sem.sem26b.message.models.GetInboxOrOutboxResponseModel;
import nl.tudelft.sem.sem26b.message.models.GetMessageResponseModel;
import nl.tudelft.sem.sem26b.message.models.PostMessageRequestModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

/**
 * Test class for MessageController.
 */
public class MessageControllerTest {

    private transient MessageSenderService senderService;
    private transient MessageRetrieverService retrieverService;
    private transient MailboxService mailboxService;
    private transient PayloadService payloadService;
    private transient MessageValidator validator;

    private transient MessageController messageController;

    /**
     * Setup the dependencies.
     */
    @BeforeEach
    public void setup() {
        senderService = Mockito.mock(MessageSenderService.class);
        retrieverService = Mockito.mock(MessageRetrieverService.class);
        mailboxService = Mockito.mock(MailboxService.class);
        payloadService = Mockito.mock(PayloadService.class);
        validator = Mockito.mock(MessageValidator.class);
        messageController =
            new MessageController(
                senderService,
                retrieverService,
                mailboxService,
                payloadService,
                validator
            );
    }

    @Test
    public void getMessageByIdMessageNotFoundTest() {
        // Arrange
        when(retrieverService.retrieveMessageById(1)).thenReturn(null);

        //Act&Assert
        assertThatThrownBy(() -> messageController.getMessageById(1))
            .isInstanceOf(ResponseStatusException.class);

        verify(retrieverService, times(1)).retrieveMessageById(1);
        verifyNoMoreInteractions(retrieverService);
        verifyNoMoreInteractions(senderService);
        verifyNoMoreInteractions(mailboxService);
        verifyNoMoreInteractions(payloadService);
        verifyNoMoreInteractions(validator);
    }

    @Test
    public void getMessageByIdHappyFlowTest() {
        // Arrange
        Message message = new Message(new MessageTarget("test"),
            new MessageTarget("test2"), MessageType.OTHER, "test", Instant.ofEpochSecond(1233L));
        List<MessagePayload> payloads = List.of(new MessagePayload("contract", 132L));
        GetMessageResponseModel responseModel = new GetMessageResponseModel(123, "test", "test2",
            "tpe", "test", new MessagePayload[] {new MessagePayload("contract", 132L)}, "sentat");
        when(retrieverService.retrieveMessageById(1)).thenReturn(message);
        when(retrieverService.parseMessageToModel(message)).thenReturn(responseModel);
        when(payloadService.getMessagePayload(message)).thenReturn(payloads);

        //Act
        ResponseEntity<GetMessageResponseModel> response = messageController.getMessageById(1);

        //Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(responseModel);
        assertThat(Objects.requireNonNull(response.getBody()).getPayload()).isEqualTo(payloads.toArray());
        verify(retrieverService, times(1)).retrieveMessageById(1);
        verify(retrieverService, times(1)).openMessageIfApplicable(message);
        verify(retrieverService, times(1)).parseMessageToModel(message);
        verify(payloadService, times(1)).getMessagePayload(message);
        verify(validator, times(1)).checkReadPermission(message);
        verifyNoMoreInteractions(retrieverService);
        verifyNoMoreInteractions(senderService);
        verifyNoMoreInteractions(mailboxService);
        verifyNoMoreInteractions(payloadService);
        verifyNoMoreInteractions(validator);
    }

    @Test
    public void getHrInboxTest() {
        // Arrange
        MessageTarget target = new MessageTarget(MessageTarget.HR_TARGET_LABEL);
        GetInboxOrOutboxResponseModel responseModel = new GetInboxOrOutboxResponseModel(
            List.of(), 1, 2);
        when(mailboxService.getInbox(target, 42)).thenReturn(responseModel);

        //Act
        ResponseEntity<GetInboxOrOutboxResponseModel> response =
            messageController.getInbox("aa", true, 42);

        //Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(responseModel);
        verify(validator, times(1)).checkHrPermission();
        verify(mailboxService, times(1)).getInbox(target, 42);
        verifyNoMoreInteractions(retrieverService);
        verifyNoMoreInteractions(senderService);
        verifyNoMoreInteractions(mailboxService);
        verifyNoMoreInteractions(payloadService);
        verifyNoMoreInteractions(validator);
    }

    @Test
    public void getUserInboxTest() {
        // Arrange
        MessageTarget target = new MessageTarget("abc");
        GetInboxOrOutboxResponseModel responseModel = new GetInboxOrOutboxResponseModel(
            List.of(), 1, 2);
        when(mailboxService.getInbox(target, 42)).thenReturn(responseModel);

        //Act
        ResponseEntity<GetInboxOrOutboxResponseModel> response =
            messageController.getInbox("abc", false, 42);

        //Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(responseModel);
        verify(validator, times(1)).checkMailboxPermission("abc");
        verify(mailboxService, times(1)).getInbox(target, 42);
        verifyNoMoreInteractions(retrieverService);
        verifyNoMoreInteractions(senderService);
        verifyNoMoreInteractions(mailboxService);
        verifyNoMoreInteractions(payloadService);
        verifyNoMoreInteractions(validator);
    }


    @Test
    public void getHrOutboxTest() {
        // Arrange
        MessageTarget target = new MessageTarget(MessageTarget.HR_TARGET_LABEL);
        GetInboxOrOutboxResponseModel responseModel = new GetInboxOrOutboxResponseModel(
            List.of(), 1, 2);
        when(mailboxService.getOutbox(target, 42)).thenReturn(responseModel);

        //Act
        ResponseEntity<GetInboxOrOutboxResponseModel> response =
            messageController.getSentMessages("aa", true, 42);

        //Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(responseModel);
        verify(validator, times(1)).checkHrPermission();
        verify(mailboxService, times(1)).getOutbox(target, 42);
        verifyNoMoreInteractions(retrieverService);
        verifyNoMoreInteractions(senderService);
        verifyNoMoreInteractions(mailboxService);
        verifyNoMoreInteractions(payloadService);
        verifyNoMoreInteractions(validator);
    }

    @Test
    public void getUserOutboxTest() {
        // Arrange
        MessageTarget target = new MessageTarget("abc");
        GetInboxOrOutboxResponseModel responseModel = new GetInboxOrOutboxResponseModel(
            List.of(), 1, 2);
        when(mailboxService.getOutbox(target, 42)).thenReturn(responseModel);

        //Act
        ResponseEntity<GetInboxOrOutboxResponseModel> response =
            messageController.getSentMessages("abc", false, 42);

        //Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(responseModel);
        verify(validator, times(1)).checkMailboxPermission("abc");
        verify(mailboxService, times(1)).getOutbox(target, 42);
        verifyNoMoreInteractions(retrieverService);
        verifyNoMoreInteractions(senderService);
        verifyNoMoreInteractions(mailboxService);
        verifyNoMoreInteractions(payloadService);
        verifyNoMoreInteractions(validator);
    }

    @Test
    public void sendMessageFromHrTest() {
        // Arrange
        PostMessageRequestModel requestModel = new PostMessageRequestModel();
        requestModel.setTo("abc");
        requestModel.setType("test");
        requestModel.setContents("test");
        requestModel.setPayload(List.of());
        Message message = new Message(new MessageTarget("test"),
            new MessageTarget("test2"), MessageType.OTHER, "test", Instant.ofEpochSecond(1233L));
        when(senderService.postMessage(requestModel, true)).thenReturn(message);

        //Act
        ResponseEntity<Integer> response = messageController.sendMessage(true, requestModel);

        //Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(validator, times(1)).checkHrPermission();
        verify(senderService, times(1)).postMessage(requestModel, true);
        verify(payloadService, times(1)).attachMessagePayloadList(message, List.of());
        verifyNoMoreInteractions(retrieverService);
        verifyNoMoreInteractions(senderService);
        verifyNoMoreInteractions(mailboxService);
        verifyNoMoreInteractions(payloadService);
        verifyNoMoreInteractions(validator);
    }

    @Test
    public void sendMessageFromUserTest() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));

        PostMessageRequestModel requestModel = new PostMessageRequestModel();
        when(senderService.postMessage(requestModel, false)).thenReturn(new Message());

        messageController.sendMessage(false, requestModel);
        assertThat(output.toString()).contains("\n");
        verify(validator, times(0)).checkHrPermission();
        verifyNoMoreInteractions(validator);
        System.setOut(System.out);
    }
}
