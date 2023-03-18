package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;


class UserLoggedInEventTests {

    @Test
    void getNetIdTest() {
        //Arrange
        NetId expected = new NetId("Example");

        //Act
        UserLoggedInEvent userLoggedInEvent = new UserLoggedInEvent(expected);

        //Verify
        assertThat(userLoggedInEvent.getNetId()).isEqualTo(expected);
    }
}