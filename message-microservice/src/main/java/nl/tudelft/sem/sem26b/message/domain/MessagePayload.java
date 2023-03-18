package nl.tudelft.sem.sem26b.message.domain;

import java.util.Arrays;
import java.util.Locale;
import lombok.Getter;

/**
 * Message payload, used to link messages to other entities.
 * (Entity as message payload)
 */
public class MessagePayload {

    /**
     * Type representing a contract.
     */
    public static final String CONTRACT_MESSAGE_PAYLOAD_TYPE = "contract";

    /**
     * Type representing a sick leave. (redundant, sick leaves not yet implemented)
     */
    public static final String SICK_LEAVE_MESSAGE_PAYLOAD_TYPE = "sick";

    /**
     * Set of valid message payload types.
     */
    public static final String[] ALLOWED_PAYLOAD_TYPES = {
        CONTRACT_MESSAGE_PAYLOAD_TYPE,
        SICK_LEAVE_MESSAGE_PAYLOAD_TYPE,
    };

    /**
     * The type and id of the entity.
     * Getters done by Lombok (the @Getter annotation).
     */
    private @Getter final String type;
    private @Getter final long id;

    /**
     * Constructor for MessagePayload.
     *
     * @param type The type of the entity (case-insensitive).
     * @param id   The id of the entity.
     *             Throws IllegalArgumentException if the type is not supported.
     */
    public MessagePayload(String type, long id) {
        type = type.toLowerCase(Locale.ROOT);
        if (!Arrays.asList(ALLOWED_PAYLOAD_TYPES).contains(type)) {
            throw new IllegalArgumentException("Message payload of type " + type + " not supported.");
        }
        this.type = type;
        this.id = id;
    }
}
