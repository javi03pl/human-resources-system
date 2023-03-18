package nl.tudelft.sem.template.contract.models;

import java.util.Arrays;
import java.util.Locale;
import lombok.Getter;

/**
 * Message payload, used to link messages to other entities.
 * (Entity as message payload)
 */
public class MessagePayload {

    public static final String CONTRACT_MESSAGE_PAYLOAD_TYPE = "contract";
    public static final String SICK_LEAVE_MESSAGE_PAYLOAD_TYPE = "sick";

    /**
     * Set of valid message payload types.
     */
    private static final String[] allowedPayloadTypes = {
        CONTRACT_MESSAGE_PAYLOAD_TYPE,
        SICK_LEAVE_MESSAGE_PAYLOAD_TYPE,
    };

    /**
     * The type and id of the entity.
     * Getters done by Lombok (the @Getter annotation).
     */
    private @Getter final String type;
    private @Getter final int id;

    /**
     * Constructor for MessagePayload.
     *
     * @param type The type of the entity (case-insensitive).
     * @param id   The id of the entity.
     *             Throws IllegalArgumentException if the type is not supported.
     */
    public MessagePayload(String type, int id) {
        type = type.toLowerCase(Locale.ROOT);
        if (!Arrays.asList(allowedPayloadTypes).contains(type)) {
            throw new IllegalArgumentException("Message payload of type " + type + " not supported.");
        }
        this.type = type;
        this.id = id;
    }
}
