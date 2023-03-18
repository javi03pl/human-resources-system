package nl.tudelft.sem.sem26b.message.domain;

import java.util.Map;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Converter for the MessageType enum.
 */
@Converter
public class MessageTypeAttributeConverter implements AttributeConverter<MessageType, String> {

    /**
     * Helper map for the conversion.
     */
    private static final Map<MessageType, String> messageEnumToStringMap;

    static {
        messageEnumToStringMap = Map.of(
            MessageType.CONTRACT_PROPOSE, "contr-prop",
            MessageType.CONTRACT_APPROVE, "contr-appr",
            MessageType.CONTRACT_TERMINATE, "contr-term",
            MessageType.CONTRACT_TERMINATE_REQUEST, "contr-term-req",
            MessageType.DOCUMENT_REQUEST, "doc-req",
            MessageType.LEAVE_REQUEST, "leave-req",
            MessageType.LEAVE_APPROVE, "leave-appr",
            MessageType.OTHER, "other"
        );
    }

    /**
     * Converts a MessageType enum to a String.
     */
    @Override
    public String convertToDatabaseColumn(MessageType messageType) {
        return messageEnumToStringMap.get(messageType);
    }

    /**
     * Converts a String to a MessageType (case-insensitive).
     * If the String is not recognized, the default value is MessageType.OTHER.
     */
    @Override
    public MessageType convertToEntityAttribute(String dbData) {
        return messageEnumToStringMap
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue().equalsIgnoreCase(dbData))
            .map(Map.Entry::getKey)
            .findAny()
            .orElse(MessageType.OTHER);
    }
}





