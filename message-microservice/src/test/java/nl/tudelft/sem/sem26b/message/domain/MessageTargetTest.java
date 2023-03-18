package nl.tudelft.sem.sem26b.message.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Test the behaviour of MessageTarget class.
 */
public class MessageTargetTest {

    /**
     * Test setting the target to an NetID.
     */
    @Test
    public void testMessageTargetNetId() {
        String netId = "sampleNetId";
        MessageTarget target = new MessageTarget(netId);
        assertThat(target.getNetId()).isEqualTo(netId);
        assertThat(target.isHr()).isFalse();
    }

    /**
     * Test setting the target to HR.
     */
    @Test
    public void testMessageTargetHr() {
        MessageTarget target = new MessageTarget(MessageTarget.HR_TARGET_LABEL);
        assertThat(target.getNetId()).isEqualTo(MessageTarget.HR_TARGET_LABEL);
        assertThat(target.isHr()).isTrue();
    }

    /**
     * Test the equals method with a MessageTarget.
     */
    @Test
    public void testEqualsWithMessageTarget() {
        MessageTarget target = new MessageTarget("sampleNetId");
        MessageTarget target2 = new MessageTarget("sampleNetId");
        MessageTarget target3 = new MessageTarget("SAMPLENetId");
        MessageTarget target4 = new MessageTarget(MessageTarget.HR_TARGET_LABEL);
        MessageTarget target5 = new MessageTarget(MessageTarget.HR_TARGET_LABEL);
        assertThat(target.equals(target2)).isTrue();
        assertThat(target.equals(target3)).isFalse();
        assertThat(target.equals(target4)).isFalse();
        assertThat(target4.equals(target5)).isTrue();
    }

    /**
     * Test the equals method with an object of another type or null.
     */
    @Test
    public void testEqualsWithOther() {
        MessageTarget target = new MessageTarget("sampleNetId");
        assertThat(target.equals(null)).isFalse();
        assertThat(target.equals("sampleNetId")).isFalse();
    }

    /**
     * Test the HashCode method.
     */
    @Test
    public void testHashCode() {
        MessageTarget target = new MessageTarget("sampleNetId");
        MessageTarget target2 = new MessageTarget("sampleNetId");
        MessageTarget target3 = new MessageTarget("SAMPLENetId");
        MessageTarget target4 = new MessageTarget(MessageTarget.HR_TARGET_LABEL);
        MessageTarget target5 = new MessageTarget(MessageTarget.HR_TARGET_LABEL);
        assertThat(target.hashCode()).isEqualTo(target2.hashCode());
        assertThat(target.hashCode()).isNotEqualTo(target3.hashCode());
        assertThat(target.hashCode()).isNotEqualTo(target4.hashCode());
        assertThat(target4.hashCode()).isEqualTo(target5.hashCode());
    }


}
