package nl.tudelft.sem.template.contract.models;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;

class MessagePayloadTests {

    private MessagePayload messagePayload;

    @Test
    public void messagePayloadTestContractMessage() {
        messagePayload = new MessagePayload(MessagePayload.CONTRACT_MESSAGE_PAYLOAD_TYPE, 1);
    }


    @Test
    public void messagePayloadTestSickLeaveMessage() {
        messagePayload = new MessagePayload(MessagePayload.SICK_LEAVE_MESSAGE_PAYLOAD_TYPE, 1);
    }

    @Test
    public void messagePayloadTestInvalidMessage() {
        ThrowableAssert.ThrowingCallable action = () ->
                new MessagePayload("OTHER_MESSAGE_PAYLOAD_TYPE", 1);
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(action);
    }

}