package nl.tudelft.sem.sem26b.message.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Simple test for the MessagePayloadMessage class.
 */
public class MessagePayloadMessageTest {

    /**
     * Test the (noArgs) constructor.
     */
    @Test
    public void testNoArgsConstructor() {
        MessagePayloadMessage messagePayloadMessage = new MessagePayloadMessage();
        assertThat(messagePayloadMessage).isNotNull();
    }

    /**
     * Test getters and setters.
     * Basic, no logic.
     */
    @Test
    public void testGettersAndSetters() {
        MessagePayloadMessage messagePayloadMessage = new MessagePayloadMessage();
        messagePayloadMessage.setMessage(new Message());
        assertThat(messagePayloadMessage.getMessage()).isNotNull();

        messagePayloadMessage.setPayloadId(1);
        assertThat(messagePayloadMessage.getPayloadId()).isEqualTo(1);

        messagePayloadMessage.setType("test");
        assertThat(messagePayloadMessage.getType()).isEqualTo("test");

        assertThat(messagePayloadMessage.getId()).isNotNull();
    }
}
