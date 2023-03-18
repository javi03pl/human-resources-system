package nl.tudelft.sem.sem26b.message.domain;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@NoArgsConstructor
@Getter
public class MessageStatus {

    /**
     * Constructor for MessageStatus.
     *
     * @param sentAt the timestamp representing when the message was sent.
     */
    public MessageStatus(Instant sentAt) {
        this.sentAt = sentAt;
    }

    /**
     * Time the message was sent.
     */
    @Column(name = "sent_at", nullable = false)
    private Instant sentAt;

    /**
     * Time at which the message was read by the receiver.
     * Null if the message was not read.
     */
    @Column(name = "read_at", nullable = true)
    private Instant readAt = null;

    /**
     * Indicates whether the message was read by the receiver.
     */
    @Column(name = "was_read", nullable = false)
    private boolean wasRead = false;

    /**
     * Marks the message as read.
     *
     * @param time The time the message was read.
     */
    public void readMessage(Instant time) {
        if (!wasRead) {
            wasRead = true;
            readAt = time;
        }
    }
}
