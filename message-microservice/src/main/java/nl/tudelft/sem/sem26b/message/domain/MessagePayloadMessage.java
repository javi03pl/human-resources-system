package nl.tudelft.sem.sem26b.message.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A DDD entity representing a connection between a message and a MessagePayload.
 * This could be implemented as a 3-part primary key, but it would take more time to implement,
 * if you have time, feel free to do so.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "message_payload_message")
public class MessagePayloadMessage {

    /**
     * ID of the message-payload-message.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private long id;

    /**
     * The reference to the message the payload is linked to.
     */
    @ManyToOne(targetEntity = Message.class, optional = false, fetch = FetchType.LAZY)
    private Message message;

    /**
     * The reference to the payload.
     */
    @Column(name = "payload_id", nullable = false)
    private long payloadId;

    /**
     * The type of the payload.
     */
    @Column(name = "type", nullable = false)
    private String type;


}

