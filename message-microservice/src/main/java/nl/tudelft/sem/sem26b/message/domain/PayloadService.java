package nl.tudelft.sem.sem26b.message.domain;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service for handling payloads.
 */
@Service
public class PayloadService {

    private final transient MessagePayloadMessageRepository messagePayloadMessageRepository;

    /**
     * Instantiates a new PayloadService.
     *
     * @param messagePayloadMessageRepository the message-payload-message repository (a mouthful, I know).
     */
    @Autowired
    public PayloadService(MessagePayloadMessageRepository messagePayloadMessageRepository) {
        this.messagePayloadMessageRepository = messagePayloadMessageRepository;
    }

    /**
     * Attach a list of payloads to a message.
     *
     * @param message            The message to attach the payloads to.
     * @param messagePayloadList The list of payloads to attach.
     */
    public void attachMessagePayloadList(Message message, List<MessagePayload> messagePayloadList) {
        for (MessagePayload messagePayload : messagePayloadList) {
            attachPayload(message, messagePayload);
        }
    }

    /**
     * Attach a payload to a message.
     *
     * @param message The message to attach the payload to.
     * @param payload The payload to attach.
     */
    public MessagePayloadMessage attachPayload(Message message, MessagePayload payload) {
        MessagePayloadMessage messagePayloadMessage = new MessagePayloadMessage();
        messagePayloadMessage.setMessage(message);
        messagePayloadMessage.setPayloadId(payload.getId());
        messagePayloadMessage.setType(payload.getType());
        messagePayloadMessageRepository.save(messagePayloadMessage);
        return messagePayloadMessage;
    }

    /**
     * Get a list of payloads attached to a message.
     *
     * @param message The message to get the payloads from.
     * @return The list of payloads attached to the message.
     */
    public List<MessagePayload> getMessagePayload(Message message) {
        List<MessagePayloadMessage> messagePayloadMessages = getPayloadsForMessage(message);
        return messagePayloadMessages.stream()
            .map(
                messagePayloadMessage -> {
                    try {
                        return new MessagePayload(
                            messagePayloadMessage.getType(),
                            messagePayloadMessage.getPayloadId()
                        );
                    } catch (Exception e) {
                        return null;
                    }
                }
            ).filter(Objects::nonNull).collect(Collectors.toList());
    }


    /**
     * Find message payload by message it is linked to.
     *
     * @param message the message the payload is linked to.
     * @return the message payload linked to the message.
     */
    public List<MessagePayloadMessage> getPayloadsForMessage(Message message) {
        return messagePayloadMessageRepository.findAllByMessage(message);
    }

}
