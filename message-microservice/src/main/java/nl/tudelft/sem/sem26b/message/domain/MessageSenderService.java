package nl.tudelft.sem.sem26b.message.domain;

import java.time.Instant;
import nl.tudelft.sem.sem26b.message.authentication.AuthManager;
import nl.tudelft.sem.sem26b.message.models.PostMessageRequestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Service responsible for sending messages.
 */
@Service
public class MessageSenderService {

    private final transient MessageRepository messageRepository;
    private final transient MessageValidator validator;
    private final transient MessageTypeAttributeConverter messageTypeAttributeConverter;
    private final transient AuthManager authManager;

    /**
     * Maximum allowed length of a message.
     */
    private static final int MAX_MSG_LEN = 2000;

    /**
     * Instantiates a new MessageSenderService.
     *
     * @param messageRepository the message repository.
     * @param validator         the validator responsible for validating messages.
     * @param authManager       the authentication manager responsible for retrieving details about token holder.
     */
    @Autowired
    public MessageSenderService(MessageRepository messageRepository, MessageValidator validator,
                                AuthManager authManager) {
        this.messageRepository = messageRepository;
        this.validator = validator;
        this.messageTypeAttributeConverter = new MessageTypeAttributeConverter();
        this.authManager = authManager;
    }

    /**
     * Parses a PostMessageRequestModel into a Message, checks if the message is valid and sends it.
     * This function does not check, if the user has HR authorization.
     *
     * @param payload  The request body to parse.
     * @param sendAsHr Whether the message is sent from HR.
     * @return The message that was sent.
     * @throws ResponseStatusException if the message/payload is invalid.
     *                                 Exception will cause a 400 Bad Request response.
     */
    public Message postMessage(PostMessageRequestModel payload, boolean sendAsHr) {
        String senderId = authManager.getNetId();
        Message message = parseMessage(payload, sendAsHr, senderId);
        validator.checkMessageIsValid(message);
        return messageRepository.save(message);
    }

    /**
     * Parses a PostMessageRequestModel into a Message.
     *
     * @param payload  The request to parse.
     * @param sendAsHr whether the message is sent from HR (if false, the sender is from a user).
     * @param senderId The NetID of the user sending the message.
     * @return The parsed message.
     * @throws ResponseStatusException if the message/payload is invalid.
     *                                Exception will cause a 400 Bad Request response.
     */
    private Message parseMessage(PostMessageRequestModel payload, boolean sendAsHr, String senderId) {

        if (payload.getContents().length() > MAX_MSG_LEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "Message too long, maximum allowed length: " + MAX_MSG_LEN + " characters");
        }

        Instant now = Instant.now();

        return new Message(
            new MessageTarget(sendAsHr ? MessageTarget.HR_TARGET_LABEL : senderId),
            new MessageTarget(payload.getTo()),
            messageTypeAttributeConverter.convertToEntityAttribute(payload.getType()),
            payload.getContents(),
            now
        );
    }
}
