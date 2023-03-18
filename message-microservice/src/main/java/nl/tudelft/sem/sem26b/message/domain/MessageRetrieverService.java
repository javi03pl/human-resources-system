package nl.tudelft.sem.sem26b.message.domain;

import java.time.Instant;
import nl.tudelft.sem.sem26b.message.authentication.AuthManager;
import nl.tudelft.sem.sem26b.message.models.GetMessageResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service responsible for retrieving messages.
 */
@Service
public class MessageRetrieverService {

    private final transient MessageRepository messageRepository;
    private final transient MessageValidator validator;
    private final transient AuthManager authManager;


    /**
     * Instantiates a new MessageRetrieverService.
     *
     * @param messageRepository the message repository.
     * @param validator         the validator responsible for authorisation and validation.
     * @param authManager       the authentication manager responsible for retrieving details about token holder.
     */
    @Autowired
    public MessageRetrieverService(MessageRepository messageRepository, MessageValidator validator,
                                   AuthManager authManager) {
        this.messageRepository = messageRepository;
        this.validator = validator;
        this.authManager = authManager;
    }

    /**
     * Retrieve a message by its id.
     *
     * @param id The id of the message to retrieve.
     * @return The message with the given id or null if no such message exists.
     */
    public Message retrieveMessageById(int id) {
        return messageRepository.findById(id).orElse(null);
    }

    /**
     * Open a message if token bearer is the receiver.
     *
     * @param message The message to open.
     */
    public void openMessageIfApplicable(Message message) {
        if (message.getReceiver().isHr()) {
            //if message is for hr, check if user is hr
            if (validator.hasHrPermission()) {
                markMessageAsRead(message);
            }
        } else {
            //if message is for user, check if user is receiver
            if (message.getReceiver().equals(new MessageTarget(authManager.getNetId()))) {
                markMessageAsRead(message);
            }
        }
    }

    /**
     * Mark a message as read.
     * set the read time to the current time (if not already read).
     *
     * @param message The message to mark as read.
     */
    private void markMessageAsRead(Message message) {
        message.readMessage(Instant.now());
        messageRepository.save(message);
    }

    /**
     * Parse a message into a GetMessageResponseModel.
     * This function sets the payload to null, payload needs to be retrieved, parsed and set separately.
     *
     * @param message The message to parse.
     * @return The parsed message being a GetMessageResponseModel representation of the message.
     *     The "payload" argument is an empty MessagePayload array.
     */
    public GetMessageResponseModel parseMessageToModel(Message message) {
        return new GetMessageResponseModel(
            message.getId(),
            message.getSender().getNetId(),
            message.getReceiver().getNetId(),
            message.getMessageType().toString(),
            message.getContents(),
            new MessagePayload[]{},
            message.getStatus().getSentAt().toString()
        );
    }
}
