package nl.tudelft.sem.sem26b.message.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for Message.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

    /**
     * Find message by id.
     */
    Optional<Message> findById(Integer id);

    /**
     * Find all messages sent by a user (outbox) paginated.
     *
     * @param sender   the sender of the messages.
     * @param pageable the page to return.
     * @return a list of messages paginated by the pageable (A page of the outbox).
     */
    List<Message> findAllBySender(MessageTarget sender, Pageable pageable);

    /**
     * Count all messages sent by a user (outbox).
     *
     * @param sender the sender of the messages.
     * @return the number of messages sent by the user (in the outbox).
     */
    int countBySender(MessageTarget sender);

    /**
     * Count all messages sent by a user (outbox), filtered by whether they were read.
     *
     * @param sender the sender of the messages.
     * @param wasRead whether the messages were read.
     * @return the number of messages sent by the user (in the outbox) that fit the constraints above.
     */
    int countBySenderAndStatus_WasRead(MessageTarget sender, boolean wasRead);

    /**
     * Count all messages received by a user (inbox).
     *
     * @param receiver the receiver of the messages.
     * @return the number of messages received by the user (in the inbox).
     */
    int countByReceiver(MessageTarget receiver);

    /**
     * Count all messages received by a user (inbox), filtered by whether they were read.
     *
     * @param receiver the receiver of the messages.
     * @param wasRead whether the messages were read.
     * @return the number of messages received by the user (in the inbox) that fit the constraints above.
     */
    int countByReceiverAndStatus_WasRead(MessageTarget receiver, boolean wasRead);

    /**
     * Find all messages sent to a user (inbox) paginated.
     *
     * @param receiver the receiver of the messages.
     * @param pageable the page to return.
     * @return a list of messages paginated by the pageable (A page of the inbox).
     */
    List<Message> findAllByReceiver(MessageTarget receiver, Pageable pageable);

    /**
     * Check if an existing message already uses an id.
     */
    boolean existsById(Integer id);
}
