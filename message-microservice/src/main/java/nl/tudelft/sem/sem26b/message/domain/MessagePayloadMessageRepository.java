package nl.tudelft.sem.sem26b.message.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for MessagePayloadMessage.
 */
@Repository
public interface MessagePayloadMessageRepository extends JpaRepository<MessagePayloadMessage, Integer> {

    /**
     * Find message payload by message it is linked to.
     *
     * @param message the message the payload is linked to.
     * @return the message payload linked to the message.
     */
    List<MessagePayloadMessage> findAllByMessage(Message message);

}
