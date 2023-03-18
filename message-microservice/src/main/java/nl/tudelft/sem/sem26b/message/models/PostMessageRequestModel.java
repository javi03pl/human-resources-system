package nl.tudelft.sem.sem26b.message.models;

import java.util.List;
import lombok.Data;
import nl.tudelft.sem.sem26b.message.domain.MessagePayload;

/**
 * Request model for a POST request to the <i>/message</i> endpoint.
 */
@Data
public class PostMessageRequestModel {

    /**
     * The recipient of the message.
     * (NetID if sent to the user, "HR" if sent to HR)
     */
    private String to;

    /**
     * The type of the message.
     */
    private String type;

    /**
     * The message itself.
     */
    private String contents;

    /**
     * The payload of the message.
     * ( for example, when sending a message about a contract, the payload would be the contract id and string "contract" )
     */
    private List<MessagePayload> payload;
}
