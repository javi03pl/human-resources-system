package nl.tudelft.sem.sem26b.message.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Locale;
import org.junit.jupiter.api.Test;

/**
 * Test the behaviour of the MessagePayload class.
 */
public class MessagePayloadTest {

    /**
     * Test the correct use of the constructor of the MessagePayload class.
     */
    @Test
    public void testCorrectConstructor() {
        for (String type : MessagePayload.ALLOWED_PAYLOAD_TYPES) {
            MessagePayload messagePayload = new MessagePayload(type, 123);
            assertThat(messagePayload).isNotNull();
        }
    }

    /**
     * Test the incorrect use of the constructor of the MessagePayload class.
     */
    @Test
    public void testIncorrectConstructor() {
        assertThatThrownBy(() -> new MessagePayload("test12345", 123))
            .isInstanceOf(IllegalArgumentException.class);
    }

    /**
     * Test constructor is case-insensitive.
     */
    @Test
    public void testConstructorCaseInsensitive() {
        for (String type : MessagePayload.ALLOWED_PAYLOAD_TYPES) {
            MessagePayload messagePayloadLow = new MessagePayload(type.toLowerCase(Locale.ROOT), 1233);
            MessagePayload messagePayloadHigh = new MessagePayload(type.toUpperCase(Locale.ROOT), 11323);
            assertThat(messagePayloadLow).isNotNull();
            assertThat(messagePayloadHigh).isNotNull();
        }
    }

    /**
     * Test getter of the type field.
     * (Test case-insensitivity)
     */
    @Test
    public void testGetType() {
        for (String type : MessagePayload.ALLOWED_PAYLOAD_TYPES) {
            MessagePayload messagePayloadLow = new MessagePayload(type.toLowerCase(Locale.ROOT), 123);
            MessagePayload messagePayloadHigh = new MessagePayload(type.toUpperCase(Locale.ROOT), 456);
            assertThat(messagePayloadLow.getType()).isEqualTo(type);
            assertThat(messagePayloadHigh.getType()).isEqualTo(type);
        }
    }

    /**
     * Test getter of the id field.
     */
    @Test
    public void testGetId() {
        MessagePayload messagePayload = new MessagePayload(MessagePayload.ALLOWED_PAYLOAD_TYPES[0], 123);
        assertThat(messagePayload.getId()).isEqualTo(123);
    }

}
