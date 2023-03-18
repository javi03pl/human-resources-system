package nl.tudelft.sem.sem26b.message.controllers;

import java.util.List;
import nl.tudelft.sem.sem26b.message.domain.MailboxService;
import nl.tudelft.sem.sem26b.message.domain.Message;
import nl.tudelft.sem.sem26b.message.domain.MessagePayload;
import nl.tudelft.sem.sem26b.message.domain.MessageRetrieverService;
import nl.tudelft.sem.sem26b.message.domain.MessageSenderService;
import nl.tudelft.sem.sem26b.message.domain.MessageTarget;
import nl.tudelft.sem.sem26b.message.domain.MessageValidator;
import nl.tudelft.sem.sem26b.message.domain.PayloadService;
import nl.tudelft.sem.sem26b.message.models.GetInboxOrOutboxResponseModel;
import nl.tudelft.sem.sem26b.message.models.GetMessageResponseModel;
import nl.tudelft.sem.sem26b.message.models.PostMessageRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Controller for the message microservice.
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    private final transient MessageSenderService senderService;
    private final transient MessageRetrieverService retrieverService;
    private final transient MailboxService mailboxService;
    private final transient PayloadService payloadService;
    private final transient MessageValidator validator;

    /**
     * Constructor for MessageController.
     *
     * @param senderService    The service responsible for sending messages.
     * @param retrieverService The service responsible for retrieving messages.
     * @param mailboxService   The service responsible for retrieving inboxes and outboxes.
     * @param payloadService   The service responsible for attaching payloads to messages and retrieving them.
     * @param validator        The validator responsible for validation and authorization.
     */
    @Autowired
    public MessageController(MessageSenderService senderService,
                             MessageRetrieverService retrieverService,
                             MailboxService mailboxService,
                             PayloadService payloadService,
                             MessageValidator validator) {
        this.senderService = senderService;
        this.retrieverService = retrieverService;
        this.mailboxService = mailboxService;
        this.payloadService = payloadService;
        this.validator = validator;
    }

    /**
     * Endpoint for opening a message.
     *
     * @param messageId The id of the message to open.
     * @return The message.
     */
    @GetMapping("/{messageId}")
    public ResponseEntity<GetMessageResponseModel> getMessageById(
        @PathVariable Integer messageId
    ) {
        Message message = retrieverService.retrieveMessageById(messageId);
        if (message == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Message not found");
        }
        validator.checkReadPermission(message);
        retrieverService.openMessageIfApplicable(message);
        GetMessageResponseModel response = retrieverService.parseMessageToModel(message);
        List<MessagePayload> payload = payloadService.getMessagePayload(message);
        response.setPayload(payload.toArray(MessagePayload[]::new));

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for getting the inbox, returns max. 100 messages.
     *
     * @param netId  The NetID of the user to get the inbox of.
     * @param fromHr if set to true, requests the HR inbox. [default: false]
     * @param page   The page to get (e.g. Page = 1 will return first 100 messages). [default: 1] [allowed: 1 or higher]
     * @return IDs and amount of both read and unread messages; (will return max 100 IDs and the full count)
     */
    @GetMapping("/{netId}/inbox")
    public ResponseEntity<GetInboxOrOutboxResponseModel> getInbox(
        @PathVariable String netId,
        @RequestParam(name = "fromHr", required = false, defaultValue = "false") boolean fromHr,
        @RequestParam(name = "page", required = false, defaultValue = "1") int page
    ) {
        if (fromHr) {
            validator.checkHrPermission();
        } else {
            validator.checkMailboxPermission(netId);
        }
        MessageTarget target = new MessageTarget(fromHr ? MessageTarget.HR_TARGET_LABEL : netId);
        GetInboxOrOutboxResponseModel responseBody = mailboxService.getInbox(target, page);
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Endpoint for getting the outbox, returns max. 100 messages.
     *
     * @param netId  The NetID of the user to get the outbox of.
     * @param fromHr if set to true, requests the HR outbox. [default: false]
     * @param page   The page to get (e.g. Page = 1 will return first 100 messages).
     *               [default: 1] [allowed: 1 or higher, incorrect values will be set to 1]
     * @return IDs and amount of both read and unread messages (by the recipient) [return max 100 IDs and the full count]
     */
    @GetMapping("/{netId}/sentMessages")
    public ResponseEntity<GetInboxOrOutboxResponseModel> getSentMessages(
        @PathVariable String netId,
        @RequestParam(name = "fromHr", required = false, defaultValue = "false") boolean fromHr,
        @RequestParam(name = "page", required = false, defaultValue = "1") int page
    ) {
        if (fromHr) {
            validator.checkHrPermission();
        } else {
            validator.checkMailboxPermission(netId);
        }
        MessageTarget target = new MessageTarget(fromHr ? MessageTarget.HR_TARGET_LABEL : netId);
        GetInboxOrOutboxResponseModel responseBody = mailboxService.getOutbox(target, page);
        return ResponseEntity.ok(responseBody);
    }

    /**
     * Endpoint for sending a message.
     *
     * @param fromHr if set to true, the message is sent from HR. [default: false]
     * @param body   The message to send.
     * @return the id of the message.
     */
    @PostMapping("/send")
    public ResponseEntity<Integer> sendMessage(
        @RequestParam(name = "fromHr", required = false, defaultValue = "false") boolean fromHr,
        @RequestBody PostMessageRequestModel body
    ) {

        if (fromHr) {
            validator.checkHrPermission();
        }
        Message message = senderService.postMessage(body, fromHr);
        payloadService.attachMessagePayloadList(message, body.getPayload());
        System.out.println(message.getId());
        return ResponseEntity.ok(message.getId());
    }

}
