package nl.tudelft.sem.template.authentication.application.user;

import nl.tudelft.sem.template.authentication.domain.user.UserLoggedInEvent;
import nl.tudelft.sem.template.authentication.domain.user.UserWasCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * This event listener is automatically called when a domain entity is saved
 * which has stored events of type: UserWasCreated.
 */
@Component
public class UserWasCreatedListener {
    /**
     * The name of the function indicates which event is listened to.
     * The format is onEVENTNAME.
     *
     * @param event The event to react to
     */
    @EventListener
    public void onAccountWasCreated(UserWasCreatedEvent event) {
        // Handler code here
        System.out.print("Account (" + event.getNetId().toString() + ") was created.\n");
    }

    /**
     * The name of the function indicates which event is listened to.
     * The format is on onEVENTNAME.
     *
     * @param event The event to react to
     */
    @EventListener
    public void onUserLogin(UserLoggedInEvent event) {
        // Handler code here
        System.out.print("Account (" + event.getNetId().toString() + ") was created.\n");
    }
}
