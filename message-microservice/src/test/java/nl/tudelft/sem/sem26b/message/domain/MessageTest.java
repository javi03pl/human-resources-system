package nl.tudelft.sem.sem26b.message.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Test the behaviour of the Message class.
 */
public class MessageTest {

    /**
     * Test the constructors of the Message class.
     * (noArgsConstructor and custom constructor)
     */
    @Test
    public void testConstructors() {
        Message message = new Message();
        assertThat(message).isNotNull();

        Message message2 = new Message(
            new MessageTarget("1"),
            new MessageTarget("2"),
            MessageType.CONTRACT_PROPOSE,
            "test",
            Instant.ofEpochSecond(123L)
        );
        assertThat(message2).isNotNull();
    }

    /**
     * Test the getters of the Message class and default values.
     */
    @Test
    public void testGetters() {
        Message message = new Message(
            new MessageTarget("1"),
            new MessageTarget(MessageTarget.HR_TARGET_LABEL),
            MessageType.CONTRACT_PROPOSE,
            "test",
            Instant.ofEpochSecond(123L)
        );
        assertThat(message.getId()).isNotNull();
        assertThat(message.getSender()).isEqualTo(new MessageTarget("1"));
        assertThat(message.getReceiver().isHr()).isTrue();
        assertThat(message.getMessageType()).isEqualTo(MessageType.CONTRACT_PROPOSE);
        assertThat(message.getContents()).isEqualTo("test");
        assertThat(message.getStatus().getSentAt()).isEqualTo(Instant.ofEpochSecond(123L));
        assertThat(message.getStatus().getReadAt()).isNull();
        assertThat(message.getStatus().isWasRead()).isFalse();
    }

    /**
     * Test the method responsible for marking the message as read.
     */
    @Test
    public void testMarkAsRead() {
        Message message = new Message(
            new MessageTarget("1"),
            new MessageTarget(MessageTarget.HR_TARGET_LABEL),
            MessageType.CONTRACT_PROPOSE,
            "test",
            Instant.ofEpochSecond(123L)
        );
        assertThat(message.getStatus().isWasRead()).isFalse();
        assertThat(message.getStatus().getReadAt()).isNull();
        message.readMessage(Instant.ofEpochSecond(456L));
        assertThat(message.getStatus().isWasRead()).isTrue();
        assertThat(message.getStatus().getReadAt()).isEqualTo(Instant.ofEpochSecond(456L));
        message.readMessage(Instant.ofEpochSecond(789L));
        assertThat(message.getStatus().isWasRead()).isTrue();
        assertThat(message.getStatus().getReadAt()).isEqualTo(Instant.ofEpochSecond(456L));
    }



}
