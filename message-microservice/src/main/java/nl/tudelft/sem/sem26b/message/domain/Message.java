package nl.tudelft.sem.sem26b.message.domain;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * DDD entity representing a message.
 */
@Entity
@Getter
@NoArgsConstructor
@Table(name = "message")
public class Message {

    /**
     * Identifier for the message.
     * Auto-generated with sequence strategy (1, 2, 3, ...).
     */
    @Id
    @GeneratedValue(generator = "message_id_seq", strategy = javax.persistence.GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private int id;

    /**
     * The sender of the message.
     * Refers to the AppUser's NetID, if the sender is HR, then the value is "HR".
     */
    @Column(name = "sender", nullable = false)
    @Convert(converter = MessageTargetAttributeConverter.class)
    private MessageTarget sender;

    /**
     * The receiver of the message.
     * Refers to the AppUser's NetID, if the receiver is HR, then the value is "HR".
     */
    @Column(name = "to", nullable = false)
    @Convert(converter = MessageTargetAttributeConverter.class)
    private MessageTarget receiver;

    /**
     * The type of the message.
     * (See: MessageType enum)
     */
    @Column(name = "type", nullable = false)
    @Convert(converter = MessageTypeAttributeConverter.class)
    private MessageType messageType;

    /**
     * Contents of the message (plain text, 2048 characters max).
     */
    @Column(name = "contents", length = 2048, nullable = true)
    private String contents;

    @Embedded
    private MessageStatus status;

    /**
     * Constructor for a message.
     *
     * @param sender      The sender of the message.
     * @param receiver    The receiver of the message.
     * @param messageType The type of the message.
     * @param contents    The contents of the message.
     * @param sentAt      The time the message was sent.
     */
    public Message(MessageTarget sender, MessageTarget receiver, MessageType messageType, String contents,
                   Instant sentAt) {
        this.sender = sender;
        this.receiver = receiver;
        this.messageType = messageType;
        this.contents = contents;
        this.status = new MessageStatus(sentAt);
    }

    /**
     * Marks the message as read.
     * <b> This function should be called only when the receiver opens the message </b>
     *
     * @param time The time the message was read.
     */
    public void readMessage(Instant time) {
        this.getStatus().readMessage(time);
    }
}
