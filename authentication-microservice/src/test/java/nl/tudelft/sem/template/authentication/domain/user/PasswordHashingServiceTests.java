package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;



public class PasswordHashingServiceTests {

    @Mock
    PasswordEncoder mockEncoder = mock(PasswordEncoder.class);

    @Test
    void hashTest() {
        //Arrange
        String expected = "EncodedPass123";
        when(mockEncoder.encode(anyString())).thenReturn("EncodedPass123");
        PasswordHashingService passwordHashingService = new PasswordHashingService(mockEncoder);

        //Act
        HashedPassword actual = passwordHashingService.hash(new Password("Pass123"));

        //Assert
        assertThat(expected).isEqualTo(actual.toString());
        verify(mockEncoder, times(1)).encode("Pass123");
    }
}