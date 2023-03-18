package nl.tudelft.sem.sem26b.message.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import nl.tudelft.sem.sem26b.message.models.GetInboxOrOutboxResponseModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

/**
 * Tests for the MailboxService.
 */
public class MailboxServiceTest {

    private transient MailboxService mailboxService;

    private transient MessageRepository mockMessageRepository;

    /**
     * Set up for each test.
     * Mock and inject dependencies.
     */
    @BeforeEach
    public void setUp() {
        mockMessageRepository = Mockito.mock(MessageRepository.class);
        mailboxService = new MailboxService(mockMessageRepository);
    }

    /**
     * Test the getInbox method.
     */
    @Test
    public void getInboxTest() {
        // Arrange
        MessageTarget target = new MessageTarget("1234");
        List<Message> returnList = messageProvider(5);
        PageRequest inboxRequest =
            PageRequest.of(3, 100, Sort.by("status.wasRead").ascending().and(Sort.by("status.sentAt").descending()));
        returnList.get(0).readMessage(Instant.ofEpochSecond(3L));
        when(mockMessageRepository.findAllByReceiver(target, inboxRequest)).thenReturn(returnList);
        when(mockMessageRepository.countByReceiverAndStatus_WasRead(target, false)).thenReturn(9);
        when(mockMessageRepository.countByReceiver(target)).thenReturn(10);

        // Act
        GetInboxOrOutboxResponseModel response = mailboxService.getInbox(target, 4);

        // Assert
        assertThat(response.getAllMessagesCount()).isEqualTo(10);
        assertThat(response.getUnreadMessagesCount()).isEqualTo(9);
        assertThat(response.getReadMessages()).hasSize(1);
        assertThat(response.getUnreadMessages()).hasSize(4);
        verify(mockMessageRepository, times(1)).findAllByReceiver(target, inboxRequest);
        verify(mockMessageRepository, times(1)).countByReceiverAndStatus_WasRead(target, false);
        verify(mockMessageRepository, times(1)).countByReceiver(target);
        verifyNoMoreInteractions(mockMessageRepository);
    }

    /**
     * Test the getOutbox method.
     */
    @Test
    public void getOutboxTest() {
        // Arrange
        MessageTarget target = new MessageTarget("12345");
        List<Message> returnList = messageProvider(2);
        returnList.get(0).readMessage(Instant.ofEpochSecond(3L));
        PageRequest outboxRequest =
            PageRequest.of(33, 100, Sort.by("status.sentAt").descending());
        when(mockMessageRepository.findAllBySender(target, outboxRequest)).thenReturn(returnList);
        when(mockMessageRepository.countBySenderAndStatus_WasRead(target, false)).thenReturn(99);
        when(mockMessageRepository.countBySender(target)).thenReturn(109);

        // Act
        GetInboxOrOutboxResponseModel response = mailboxService.getOutbox(target, 34);

        // Assert
        assertThat(response.getAllMessagesCount()).isEqualTo(109);
        assertThat(response.getUnreadMessagesCount()).isEqualTo(99); // 0
        assertThat(response.getReadMessages()).hasSize(1);
        assertThat(response.getUnreadMessages()).hasSize(1);
        verify(mockMessageRepository, times(1)).findAllBySender(target, outboxRequest);
        verify(mockMessageRepository, times(1)).countBySender(target);
        verify(mockMessageRepository, times(1)).countBySenderAndStatus_WasRead(target, false);
        verifyNoMoreInteractions(mockMessageRepository);
    }

    /**
     * Test for getInboxContents, correct usage, page >= 1.
     */
    @Test
    public void testGetInboxContents() {
        // Arrange
        MessageTarget target = new MessageTarget("qweerty");
        int page = 112;
        PageRequest pageRequest =
            PageRequest.of(111, 100, Sort.by("status.wasRead").ascending().and(Sort.by("status.sentAt").descending()));
        List<Message> expected = messageProvider(3);
        when(mockMessageRepository.findAllByReceiver(target, pageRequest)).thenReturn(expected);

        // Act&Assert
        assertThat(mailboxService.getInboxContents(target, page)).isEqualTo(expected);
        verify(mockMessageRepository, times(1))
            .findAllByReceiver(target, PageRequest.of(111, 100, pageRequest.getSort()));
        verifyNoMoreInteractions(mockMessageRepository);
    }

    /**
     * Test for getInboxContents, incorrect usage, page < 1. Should return default to first page.
     */
    @Test
    public void testGetInboxContentsPageTooLow() {
        // Arrange
        MessageTarget target = new MessageTarget("qweerty");
        int page = -112;
        PageRequest pageRequest =
            PageRequest.of(0, 100, Sort.by("status.wasRead").ascending().and(Sort.by("status.sentAt").descending()));
        List<Message> expected = messageProvider(3);
        when(mockMessageRepository.findAllByReceiver(target, pageRequest)).thenReturn(expected);

        // Act&Assert
        assertThat(mailboxService.getInboxContents(target, page)).isEqualTo(expected);
        verify(mockMessageRepository, times(1))
            .findAllByReceiver(target, PageRequest.of(0, 100, pageRequest.getSort()));
        verifyNoMoreInteractions(mockMessageRepository);
    }

    /**
     * Test for getOutboxContents, correct usage, page >= 1.
     */
    @Test
    public void testGetOutboxContents() {
        // Arrange
        MessageTarget target = new MessageTarget("ID");
        int page = 12;
        List<Message> expected = messageProvider(2);
        when(mockMessageRepository.findAllBySender(target, PageRequest.of(11, 100, Sort.by("status.sentAt").descending())))
            .thenReturn(expected);

        // Act&Assert
        assertThat(mailboxService.getOutboxContents(target, page)).isEqualTo(expected);
        verify(mockMessageRepository, times(1))
            .findAllBySender(target, PageRequest.of(11, 100, Sort.by("status.sentAt").descending()));
        verifyNoMoreInteractions(mockMessageRepository);
    }

    /**
     * Test for getOutboxContents, incorrect usage, page < 1. Should return default to first page.
     */
    @Test
    public void testGetOutboxContentsPageTooLow() {
        // Arrange
        MessageTarget target = new MessageTarget("IDD");
        int page = -1;
        List<Message> expected = messageProvider(1);
        when(mockMessageRepository.findAllBySender(target, PageRequest.of(0, 100, Sort.by("status.sentAt").descending())))
            .thenReturn(expected);

        // Act&Assert
        assertThat(mailboxService.getOutboxContents(target, page)).isEqualTo(expected);
        verify(mockMessageRepository, times(1))
            .findAllBySender(target, PageRequest.of(0, 100, Sort.by("status.sentAt").descending()));
        verifyNoMoreInteractions(mockMessageRepository);
    }

    /**
     * Test for getInboxSize.
     */
    @Test
    public void getInboxSizeTest() {
        when(mockMessageRepository.countByReceiver(new MessageTarget("abc"))).thenReturn(5);
        assertThat(mailboxService.getInboxSize(new MessageTarget("abc"))).isEqualTo(5);
        verify(mockMessageRepository, times(1)).countByReceiver(new MessageTarget("abc"));
        verifyNoMoreInteractions(mockMessageRepository);
    }

    /**
     * Test for getUnreadMessagesAmount.
     */
    @Test
    public void getUnreadMessagesAmountTest() {
        when(mockMessageRepository.countByReceiverAndStatus_WasRead(new MessageTarget("qwerty"), false)).thenReturn(15);
        assertThat(mailboxService.getUnreadMessagesAmount(new MessageTarget("qwerty"))).isEqualTo(15);
        verify(mockMessageRepository, times(1)).countByReceiverAndStatus_WasRead(new MessageTarget("qwerty"), false);
        verifyNoMoreInteractions(mockMessageRepository);
    }

    /**
     * Test for getOutboxSize.
     */
    @Test
    public void getOutboxSizeTest() {
        when(mockMessageRepository.countBySender(new MessageTarget("AAA"))).thenReturn(9);
        assertThat(mailboxService.getOutboxSize(new MessageTarget("AAA"))).isEqualTo(9);
        verify(mockMessageRepository, times(1)).countBySender(new MessageTarget("AAA"));
        verifyNoMoreInteractions(mockMessageRepository);
    }

    /**
     * Test for getUnreadOutboxMessagesAmount.
     */
    @Test
    public void getUnreadOutboxMessagesAmountTest() {
        when(mockMessageRepository.countBySenderAndStatus_WasRead(new MessageTarget("BBB"), false)).thenReturn(3);
        assertThat(mailboxService.getUnreadOutboxMessagesAmount(new MessageTarget("BBB"))).isEqualTo(3);
        verify(mockMessageRepository, times(1)).countBySenderAndStatus_WasRead(new MessageTarget("BBB"), false);
        verifyNoMoreInteractions(mockMessageRepository);
    }

    /**
     * Generate a list of messages with the given amount.
     * Used for testing.
     *
     * @param amount The amount of messages to generate.
     * @return The list of messages.
     */
    private List<Message> messageProvider(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Amount cannot be negative.");
        }
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            messages.add(
                new Message(new MessageTarget("1234"), new MessageTarget("1234"), MessageType.CONTRACT_APPROVE, "test",
                    Instant.ofEpochSecond(i)));
        }
        return messages;
    }

}
