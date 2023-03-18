package nl.tudelft.sem.sem26b.message.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Test class for PayloadService.
 */
public class PayloadServiceTest {

    private transient MessagePayloadMessageRepository mockRepository;

    private transient PayloadService payloadService;

    /**
     * Setup the dependencies.
     */
    @BeforeEach
    public void setup() {
        mockRepository = Mockito.mock(MessagePayloadMessageRepository.class);
        payloadService = new PayloadService(mockRepository);
    }

    /**
     * Test the attachMessagePayload method.
     */
    @Test
    public void attachMessagePayloadTest() {
        // Arrange
        Message message = new Message();
        List<MessagePayload> payloads = List.of(
            new MessagePayload(MessagePayload.CONTRACT_MESSAGE_PAYLOAD_TYPE, 123L),
            new MessagePayload(MessagePayload.CONTRACT_MESSAGE_PAYLOAD_TYPE, 456L));
        ArgumentCaptor<MessagePayloadMessage> captor = ArgumentCaptor.forClass(MessagePayloadMessage.class);

        // Act
        payloadService.attachMessagePayloadList(message, payloads);

        // Assert
        verify(mockRepository, times(2)).save(captor.capture());
        for (int i = 0; i < 2; i++) {
            assertThat(captor.getAllValues().get(i).getMessage()).isEqualTo(message);
            assertThat(captor.getAllValues().get(i).getPayloadId()).isEqualTo(payloads.get(i).getId());
            assertThat(captor.getAllValues().get(i).getType()).isEqualTo(payloads.get(i).getType());
        }
        verifyNoMoreInteractions(mockRepository);
    }

    /**
     * Test the getMessagePayload method.
     * Invalid payload is ignored.
     */
    @Test
    public void getMessagePayloadTest() {
        // Arrange
        Message message = new Message();
        MessagePayloadMessage payloadMessage1 = new MessagePayloadMessage();
        payloadMessage1.setMessage(message);
        payloadMessage1.setPayloadId(123L);
        payloadMessage1.setType(MessagePayload.CONTRACT_MESSAGE_PAYLOAD_TYPE);
        MessagePayloadMessage payloadMessage2 = new MessagePayloadMessage();
        payloadMessage2.setMessage(message);
        payloadMessage2.setPayloadId(456L);
        payloadMessage2.setType(MessagePayload.CONTRACT_MESSAGE_PAYLOAD_TYPE);
        MessagePayloadMessage payloadMessage3 = new MessagePayloadMessage();
        payloadMessage3.setMessage(message);
        payloadMessage3.setPayloadId(456L);
        payloadMessage3.setType("INVALID");
        List<MessagePayloadMessage> payloadMessages = List.of(payloadMessage1, payloadMessage2, payloadMessage3);
        when(mockRepository.findAllByMessage(message)).thenReturn(payloadMessages);

        // Act
        List<MessagePayload> result = payloadService.getMessagePayload(message);

        // Assert
        for (int i = 0; i < 2; i++) {
            assertThat(result.get(i).getId()).isEqualTo(payloadMessages.get(i).getPayloadId());
            assertThat(result.get(i).getType()).isEqualTo(payloadMessages.get(i).getType());
        }
        assertThat(result.size()).isEqualTo(2);
        verify(mockRepository, times(1)).findAllByMessage(message);
        verifyNoMoreInteractions(mockRepository);
    }

    /**
     * Test the getPayloadsForMessage method.
     */
    @Test
    public void getPayloadsForMessageTest() {
        // Arrange
        Message message = new Message();
        MessagePayloadMessage payloadMessage1 = new MessagePayloadMessage();
        payloadMessage1.setMessage(message);
        payloadMessage1.setPayloadId(123L);
        payloadMessage1.setType(MessagePayload.CONTRACT_MESSAGE_PAYLOAD_TYPE);
        MessagePayloadMessage payloadMessage2 = new MessagePayloadMessage();
        payloadMessage2.setMessage(message);
        payloadMessage2.setPayloadId(456L);
        payloadMessage2.setType(MessagePayload.SICK_LEAVE_MESSAGE_PAYLOAD_TYPE);
        when(mockRepository.findAllByMessage(message)).thenReturn(List.of(payloadMessage1, payloadMessage2));

        // Act
        List<MessagePayloadMessage> result = payloadService.getPayloadsForMessage(message);

        // Assert
        assertThat(result).isEqualTo(List.of(payloadMessage1, payloadMessage2));
        verify(mockRepository, times(1)).findAllByMessage(message);
        verifyNoMoreInteractions(mockRepository);
    }
}
