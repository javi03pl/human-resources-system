package nl.tudelft.sem.sem26b.message.models;

import java.util.List;
import lombok.Data;
import nl.tudelft.sem.sem26b.message.domain.Message;

/**
 * Response model for a GET request to the <i>/message/inbox</i> or <i>/message/outbox</i> endpoints.
 */
@Data
public class GetInboxOrOutboxResponseModel {

    /**
     * Constructor for GetInboxOrOutboxResponseModel.
     *
     * @param messages the messages to include in the response.
     *                 The messages are spit into read and unread sets.
     * @param unreadMessagesCount the amount of unread messages.
     * @param allMessagesCount the amount of all messages.
     */
    public GetInboxOrOutboxResponseModel(List<Message> messages, int unreadMessagesCount, int allMessagesCount) {
        this.setUnreadMessagesCount(unreadMessagesCount);
        this.setAllMessagesCount(allMessagesCount);
        this.setReadMessages(
            messages.stream().filter(message -> message.getStatus().isWasRead()).mapToInt(Message::getId).toArray());
        this.setUnreadMessages(
            messages.stream().filter(message -> !message.getStatus().isWasRead()).mapToInt(Message::getId).toArray());
    }

    /**
     * IDs of unread messages in the current page.
     */
    private int[] unreadMessages;

    /**
     * Amount of all unread messages.
     */
    private int unreadMessagesCount;

    /**
     * IDs of read messages in the current page.
     */
    private int[] readMessages;

    /**
     * Amount of all messages.
     */
    private int allMessagesCount;
}
