package nl.tudelft.sem.sem26b.message.domain;

import java.util.Map;
import nl.tudelft.sem.sem26b.message.authentication.AuthManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Validator for MessageController.
 */
@Component
public class MessageValidator {
    private enum MessageSenderAndReceiverValidationType {
        XOR, SENDER_NOT_HR_RECEIVER_HR, SENDER_HR_RECEIVER_NOT_HR, ANY
    }

    private final transient AuthManager authManager;

    private static final String READ_AUTHORIZATION_FAILED_MESSAGE = "You are not allowed to access this message.";
    private static final String HR_AUTHORIZATION_FAILED_MESSAGE = "You don't have a HR role.";
    private static final String MAILBOX_AUTHORIZATION_FAILED_MESSAGE = "You are not allowed to access this mailbox.";
    private static final Map<MessageType, MessageSenderAndReceiverValidationType> fromToValidationMap = Map.of(
        MessageType.CONTRACT_PROPOSE, MessageSenderAndReceiverValidationType.XOR,
        MessageType.CONTRACT_APPROVE, MessageSenderAndReceiverValidationType.XOR,
        MessageType.CONTRACT_TERMINATE, MessageSenderAndReceiverValidationType.SENDER_HR_RECEIVER_NOT_HR,
        MessageType.LEAVE_APPROVE, MessageSenderAndReceiverValidationType.SENDER_HR_RECEIVER_NOT_HR,
        MessageType.DOCUMENT_REQUEST, MessageSenderAndReceiverValidationType.SENDER_NOT_HR_RECEIVER_HR,
        MessageType.LEAVE_REQUEST, MessageSenderAndReceiverValidationType.SENDER_NOT_HR_RECEIVER_HR,
        MessageType.CONTRACT_TERMINATE_REQUEST, MessageSenderAndReceiverValidationType.SENDER_NOT_HR_RECEIVER_HR
    );

    /**
     * Instantiates a new MessageValidator.
     *
     * @param authManager the authentication manager responsible for retrieving details about token holder.
     */
    @Autowired
    public MessageValidator(AuthManager authManager) {
        this.authManager = authManager;
    }


    /**
     * Checks if the given message is valid.
     *
     * @param message The message to check.
     * @throws ResponseStatusException if the message is invalid.
     *                                 Exception will cause a 400 Bad Request response.
     */
    public void checkMessageIsValid(Message message) {
        if (!isMessageValid(message)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid message");
        }
    }

    /**
     * Checks if the given message is valid.
     *
     * @param message The message to check.
     * @return true if the message is valid, false otherwise.
     */
    private boolean isMessageValid(Message message) {
        // If the sender and receiver are the same party, the message is invalid.
        if (message.getSender().equals(message.getReceiver())) {
            return false;
        }

        // Apply rules attached to the message type (see doc in MessageType.java).
        return areSenderAndReceiverTypesValid(message);
    }

    private boolean areSenderAndReceiverTypesValid(Message message) {
        switch (fromToValidationMap.getOrDefault(message.getMessageType(), MessageSenderAndReceiverValidationType.ANY)) {
            case XOR:
                return message.getSender().isHr() ^ message.getReceiver().isHr(); //XOR
            case SENDER_NOT_HR_RECEIVER_HR:
                return !message.getSender().isHr() && message.getReceiver().isHr();
            case SENDER_HR_RECEIVER_NOT_HR:
                return message.getSender().isHr() && !message.getReceiver().isHr();
            default:
                return true;
        }
    }

    /**
     * Checks if the token bearer is HR.
     *
     * @throws ResponseStatusException if the token bearer is not HR.
     *                                 Exception will cause a 401 Unauthorized response.
     */
    public void checkHrPermission() {
        if (!hasHrPermission()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, HR_AUTHORIZATION_FAILED_MESSAGE);
        }
    }

    /**
     * Checks if the token bearer is HR.
     *
     * @return true if the token bearer is HR, false otherwise.
     */
    protected boolean hasHrPermission() {
        return authManager.getRole().contains("HR");
    }

    /**
     * Checks if the token bearer has access to a mailbox of the given user.
     *
     * @param netId The NetID of the user whose mailbox to check.
     * @throws ResponseStatusException if the token bearer is not allowed to access the mailbox.
     *                                 Exception will cause a 401 Unauthorized response.
     */
    public void checkMailboxPermission(String netId) {
        if (!hasMailboxPermission(netId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, MAILBOX_AUTHORIZATION_FAILED_MESSAGE);
        }
    }


    /**
     * Check if the token bearer can access a mailbox of the given user.
     *
     * @param netId the NetID of the user whose mailbox to check.
     * @return true if the token bearer can access the mailbox, false otherwise.
     */
    protected boolean hasMailboxPermission(String netId) {
        // If the token bearer is HR, they can access any mailbox.
        if (hasHrPermission()) {
            return true;
        }
        // If the token bearer is the user whose mailbox is being accessed, they can access it.
        return authManager.getNetId().equals(netId);
    }

    /**
     * Checks if the token bearer has read permission for the message with the given id.
     *
     * @param message The message to check.
     * @throws ResponseStatusException if the token bearer is not allowed to read the message.
     *                                 Exception will cause a 401 Unauthorized response.
     */
    public void checkReadPermission(Message message) {
        if (!hasReadPermission(message)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, READ_AUTHORIZATION_FAILED_MESSAGE);
        }
    }

    /**
     * Check if the token bearer can access the message.
     * Token bearer must be the HR, sender or receiver of the message to be able to access it.
     *
     * @param message The message to check.
     * @return true if the token bearer can access the message, false otherwise.
     */
    protected boolean hasReadPermission(Message message) {
        if (hasHrPermission()) {
            return true;
        }
        String netId = authManager.getNetId();
        return message.getReceiver().getNetId().equals(netId) || message.getSender().getNetId().equals(netId);
    }
}
