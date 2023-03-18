package nl.tudelft.sem.sem26b.message.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.sem26b.message.domain.MessagePayload;

/**
 * Response model for a GET request to the <i>/message</i> endpoint.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetMessageResponseModel {

    /**
     * The message's id.
     */
    private int messageId;

    /**
     * The 'sender' of the message.
     */
    private String sender;

    /**
     * The recipient of the message.
     */
    private String receiver;

    /**
     * The type of the message.
     */
    private String messageType;

    /**
     * The message itself.
     */
    private String contents;

    /**
     * The payload of the message.
     * ( for example, when sending a message about a contract, the payload would be the contract id and string "contract" )
     */
    private MessagePayload[] payload;

    /**
     * The timestamp representing when the message was sent.
     */
    private String sentAt;
}
