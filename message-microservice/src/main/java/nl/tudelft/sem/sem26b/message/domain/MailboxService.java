package nl.tudelft.sem.sem26b.message.domain;

import java.util.List;
import nl.tudelft.sem.sem26b.message.models.GetInboxOrOutboxResponseModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class MailboxService {

    private final transient MessageRepository messageRepository;

    /**
     * Instantiates a new MailboxService.
     *
     * @param messageRepository the message repository.
     */
    @Autowired
    public MailboxService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }


    /**
     * Get the inbox page for a user.
     *
     * @param target the target (user/HR) to get the inbox for.
     * @param page   the page to get (1-indexed, lower values will return first page).
     * @return the inbox page for the target as a response model.
     */
    public GetInboxOrOutboxResponseModel getInbox(MessageTarget target, int page) {
        List<Message> messages = getInboxContents(target, page);
        int unreadMessagesCount = getUnreadMessagesAmount(target);
        int allMessagesCount = getInboxSize(target);
        return new GetInboxOrOutboxResponseModel(messages, unreadMessagesCount, allMessagesCount);
    }

    /**
     * Get the outbox page for a user.
     *
     * @param target the target (user/HR) to get the outbox for.
     * @param page   the page to get (1-indexed, lower values will return first page).
     * @return the outbox page for the target as a response model.
     */
    public GetInboxOrOutboxResponseModel getOutbox(MessageTarget target, int page) {
        List<Message> messages = getOutboxContents(target, page);
        int unreadMessagesCount = getUnreadOutboxMessagesAmount(target);
        int allMessagesCount = getOutboxSize(target);
        return new GetInboxOrOutboxResponseModel(messages, unreadMessagesCount, allMessagesCount);
    }

    /**
     * Get inbox for a user.
     * Paginated, 100 messages per page, sorted by whether they were read (primary) and then by when they were sent
     * (secondary).
     *
     * @param target The user/HR to get the inbox for.
     * @param page   The page number to get (1-indexed).
     * @return The user's inbox.
     */
    public List<Message> getInboxContents(MessageTarget target, int page) {
        page = Math.max(page - 1, 0);
        Pageable pageable =
            PageRequest.of(page, 100, Sort.by("status.wasRead").ascending().and(Sort.by("status.sentAt").descending()));
        return messageRepository.findAllByReceiver(target, pageable);
    }

    /**
     * Get outbox for a user.
     * Paginated, 100 messages per page, sorted by when they were sent.
     *
     * @param target The user/HR to get the outbox for.
     * @param page   The page number to get (1-indexed).
     * @return The user's outbox.
     */
    public List<Message> getOutboxContents(MessageTarget target, int page) {
        page = Math.max(page - 1, 0);
        Pageable pageable = PageRequest.of(page, 100, Sort.by("status.sentAt").descending());
        return messageRepository.findAllBySender(target, pageable);
    }

    /**
     * Get the inbox size for a user.
     *
     * @param target the user/HR to get the inbox size for.
     * @return the number of messages in the inbox.
     */
    public int getInboxSize(MessageTarget target) {
        return messageRepository.countByReceiver(target);
    }

    /**
     * Get the amount of unread messages in the inbox for a user.
     *
     * @param target the user/HR to get the unread inbox size for.
     * @return the number of unread messages in the inbox.
     */
    public int getUnreadMessagesAmount(MessageTarget target) {
        return messageRepository.countByReceiverAndStatus_WasRead(target, false);
    }

    /**
     * Get the outbox size for a user.
     *
     * @param target the user/HR to get the outbox size for.
     * @return the number of messages in the outbox.
     */
    public int getOutboxSize(MessageTarget target) {
        return messageRepository.countBySender(target);
    }

    /**
     * Get the amount of messages not read by the recipient in the outbox for a user.
     *
     * @param target the user/HR to get the unread outbox size for.
     * @return the number of messages not read by the recipient in the outbox.
     */
    public int getUnreadOutboxMessagesAmount(MessageTarget target) {
        return messageRepository.countBySenderAndStatus_WasRead(target, false);
    }
}
