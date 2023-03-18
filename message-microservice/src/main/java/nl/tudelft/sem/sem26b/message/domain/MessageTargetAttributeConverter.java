package nl.tudelft.sem.sem26b.message.domain;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

/**
 * Used to convert the MessageTarget to a String and back.
 * (for the database)
 */
@Converter
public class MessageTargetAttributeConverter implements AttributeConverter<MessageTarget, String> {
    
    /**
     * Convert the MessageTarget to a String.
     *
     * @param attribute the MessageTarget to convert
     * @return String representation of the MessageTarget
     */
    @Override
    public String convertToDatabaseColumn(MessageTarget attribute) {
        return attribute.getNetId();
    }

    /**
     * Convert String to a MessageTarget.
     *
     * @param dbData the Integer to convert
     * @return the converted MessageTarget
     */
    @Override
    public MessageTarget convertToEntityAttribute(String dbData) {
        return new MessageTarget(dbData);
    }
}
