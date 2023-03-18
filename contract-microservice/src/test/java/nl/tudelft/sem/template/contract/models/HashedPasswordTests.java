package nl.tudelft.sem.template.contract.models;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;


public class HashedPasswordTests {

    @Test
    public void testClone() throws CloneNotSupportedException {
        //Arrange
        HashedPassword hashedPassword = new HashedPassword("random");

        //Act
        HashedPassword clonedHashedPassword = hashedPassword.clone();

        //Verify
        assertThat(hashedPassword).isEqualTo(clonedHashedPassword);
        assertThat(hashedPassword).isNotSameAs(clonedHashedPassword);
    }

    @Test
    public void toStringTest() {
        //Arrange
        String hash = "random";

        //Act
        String actual = new HashedPassword(hash).toString();

        //Verify
        assertThat(actual).isEqualTo(hash);
    }
}