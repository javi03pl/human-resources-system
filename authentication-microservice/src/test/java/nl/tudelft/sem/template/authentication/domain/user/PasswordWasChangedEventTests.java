package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PasswordWasChangedEventTests {

    @Mock
    AppUser mockAppUser;

    @Test
    void getUserTest() {
        //Act
        PasswordWasChangedEvent passwordWasChangedEvent = new PasswordWasChangedEvent(mockAppUser);

        //Verify
        assertThat(passwordWasChangedEvent.getUser()).isEqualTo(mockAppUser);
    }
}