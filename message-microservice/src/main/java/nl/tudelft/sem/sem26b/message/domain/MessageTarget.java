package nl.tudelft.sem.sem26b.message.domain;

/**
 * Used to store the message target.
 * It can be either an AppUser or anyone from HR
 */
public class MessageTarget {

    private final transient String id;
    public static final String HR_TARGET_LABEL = "HR";

    public MessageTarget(String id) {
        this.id = id;
    }

    public String getNetId() {
        return id;
    }

    /**
     * Check, if the target is HR.
     *
     * @return true if the target is HR, false otherwise
     */
    public boolean isHr() {
        return HR_TARGET_LABEL.equals(id);
    }

    /**
     * Equals method.
     *
     * @param other object to compare against.
     * @return true if equal <i>other</i> is of type MessageTarget and contains same target , false otherwise.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        return other.getClass().equals(MessageTarget.class) && ((MessageTarget) other).getNetId().equals(id);
    }

    /**
     * Hashcode method.
     *
     * @return hashcode of the target.
     */
    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
