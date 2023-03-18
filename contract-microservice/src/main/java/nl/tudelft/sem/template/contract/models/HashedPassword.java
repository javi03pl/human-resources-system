package nl.tudelft.sem.template.contract.models;

import lombok.EqualsAndHashCode;

/**
 * A DDD value object representing a hashed password in our domain.
 */
@EqualsAndHashCode
public class HashedPassword implements Cloneable {
    private final transient String hash;

    public HashedPassword(String hash) {
        // Validate input
        this.hash = hash;
    }

    @Override
    public String toString() {
        return hash;
    }

    @Override
    public HashedPassword clone() throws CloneNotSupportedException {
        return (HashedPassword) super.clone();
    }
}
