package nl.tudelft.sem.sem26b.message.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test the behaviour of the MessageTypeAttributeConverter.
 */
public class MessageTypeAttributeConverterTest {

    private final MessageTypeAttributeConverter converter = new MessageTypeAttributeConverter();

    /**
     * Test the case-insensitive conversion of a MessageType to a String.
     */
    @Test
    public void testConvertingStringToEnum() {
        String[] input = {
            "doc-req",
            "leave-req",
            "LEAVE-appr",
            "other",
            "contr-PROP",
            "ConTr-appr",
            "contr-term",
            "contr-tERm-req",
        };
        MessageType[] expectedOutput = {
            MessageType.DOCUMENT_REQUEST,
            MessageType.LEAVE_REQUEST,
            MessageType.LEAVE_APPROVE,
            MessageType.OTHER,
            MessageType.CONTRACT_PROPOSE,
            MessageType.CONTRACT_APPROVE,
            MessageType.CONTRACT_TERMINATE,
            MessageType.CONTRACT_TERMINATE_REQUEST,
        };
        for (int i = 0; i < input.length; i++) {
            assertThat(converter.convertToEntityAttribute(input[i])).isEqualTo(expectedOutput[i]);
        }
    }

    /**
     * Test the conversion of a String to a MessageType.
     */
    @Test
    public void testConvertingEnumToString() {
        MessageType[] input = {
            MessageType.CONTRACT_APPROVE,
            MessageType.CONTRACT_TERMINATE_REQUEST,
            MessageType.CONTRACT_TERMINATE,
            MessageType.DOCUMENT_REQUEST,
            MessageType.LEAVE_REQUEST,
            MessageType.LEAVE_APPROVE,
            MessageType.OTHER,
            MessageType.CONTRACT_PROPOSE,
        };
        String[] expectedOutput = {
            "contr-appr",
            "contr-term-req",
            "contr-term",
            "doc-req",
            "leave-req",
            "leave-appr",
            "other",
            "contr-prop",
        };
        for (int i = 0; i < input.length; i++) {
            assertThat(converter.convertToDatabaseColumn(input[i])).isEqualTo(expectedOutput[i]);
        }
    }

    /**
     * Test, whether the converter defaults to OTHER when an invalid String is given.
     */
    @Test
    public void testIncorrectDbData() {
        String[] gibberish = {
            "ABIEUFB234",
            "Reasumując wszystkie aspekty kwintesencji tematu, dochodzę do fundamentalnej konkluzji - warto studiować",
            "TEXT-HERE",
            "w-szczebrzeszynie-chrząszcz-brzmi-w-trzcinnie",
        };
        for (String incorrectValue : gibberish) {
            assertThat(converter.convertToEntityAttribute(incorrectValue)).isEqualTo(MessageType.OTHER);
        }
    }

    /**
     * Go through all possible values of MessageType, convert them to String and back. Check, whether the result is the same.
     */
    @Test
    public void testDoubleConversion() {
        for (MessageType messageType : MessageType.values()) {
            assertThat(messageType)
                .isEqualTo(
                    converter.convertToEntityAttribute(
                        converter.convertToDatabaseColumn(messageType)
                    ));
        }
    }
}
