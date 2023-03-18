package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;


public class PasswordTests {

    @Test
    void testToString() {
        //Arrange
        String expected = "Pass123";
        Password password = new Password(expected);

        //Act
        String actual = password.toString();

        //Assert
        assertThat(expected).isEqualTo(actual);
    }
}