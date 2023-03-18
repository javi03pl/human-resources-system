package nl.tudelft.sem.sem26b.message.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test for the MessageTargetAttributeConverter.
 */
public class MessageTargetAttributeConverterTest {

    private final MessageTargetAttributeConverter converter = new MessageTargetAttributeConverter();

    /**
     * Test the conversion of a String to a MessageTarget.
     */
    @Test
    public void testConvertingStringToObject() {
        String[] input = {
            "test",
            "test2",
            "test3",
            "TEST",
            MessageTarget.HR_TARGET_LABEL,
        };
        MessageTarget[] expectedOutput = {
            new MessageTarget("test"),
            new MessageTarget("test2"),
            new MessageTarget("test3"),
            new MessageTarget("TEST"),
            new MessageTarget(MessageTarget.HR_TARGET_LABEL),
        };
        for (int i = 0; i < input.length; i++) {
            assertThat(converter.convertToEntityAttribute(input[i])).isEqualTo(expectedOutput[i]);
            assertThat(converter.convertToEntityAttribute(input[i]).isHr()).isEqualTo(i == 4);
        }
    }


    /**
     * Test the conversion of a MessageTarget to a String.
     */
    @Test
    public void testConvertingObjectToString() {
        MessageTarget[] input = {
            new MessageTarget("test"),
            new MessageTarget("test2"),
            new MessageTarget("test3"),
            new MessageTarget("TEST"),
            new MessageTarget(MessageTarget.HR_TARGET_LABEL),
        };
        String[] expectedOutput = {
            "test",
            "test2",
            "test3",
            "TEST",
            MessageTarget.HR_TARGET_LABEL,
        };
        for (int i = 0; i < input.length; i++) {
            assertThat(converter.convertToDatabaseColumn(input[i])).isEqualTo(expectedOutput[i]);
        }
    }

}
