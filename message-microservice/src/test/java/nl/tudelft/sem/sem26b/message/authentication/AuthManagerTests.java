package nl.tudelft.sem.sem26b.message.authentication;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;


/**
 * Tests by Berken and Javier.
 */
public class AuthManagerTests {
    private transient AuthManager authManager;


    @BeforeEach
    public void setup() {
        authManager = new AuthManager();
    }

    @Test
    public void getNetIdTest() {
        // Arrange
        String expected = "user123";
        var authenticationToken = new UsernamePasswordAuthenticationToken(
            expected,
            null, List.of() // no credentials and no authorities
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // Act
        String actual = authManager.getNetId();

        // Assert
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    public void getRoleTest() {
        // Arrange
        List<SimpleGrantedAuthority> expectedRole =
            Collections.singletonList(new SimpleGrantedAuthority("CANDIDATE"));
        var authenticationToken = new UsernamePasswordAuthenticationToken(
            "123",
            null, expectedRole
        );
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

        // Act
        String actual = authManager.getRole();

        // Assert
        assertThat(actual).isEqualTo(expectedRole.toString());
    }
}
