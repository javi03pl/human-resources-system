package nl.tudelft.sem.template.authentication.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest
// activate profiles to have spring use mocks during auto-injection of certain beans.
@ActiveProfiles({"test", "mockPasswordEncoder"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class RegistrationServiceTests {

    @Autowired
    private transient RegistrationService registrationService;

    @Autowired
    private transient PasswordHashingService mockPasswordEncoder;

    @Autowired
    private transient UserRepository userRepository;

    @Test
    public void createUser_withValidData_worksCorrectly() throws Exception {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final Password testPassword = new Password("password123");
        final HashedPassword testHashedPassword = new HashedPassword("hashedTestPassword");
        when(mockPasswordEncoder.hash(testPassword)).thenReturn(testHashedPassword);

        // Act
        registrationService.registerUser(testUser, testPassword, EmployeeType.HR);

        // Assert
        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword);
    }


    @Test
    public void createUser_duplicateNetId_throwsException() throws Exception {
        // Arrange
        final NetId testUser1 = new NetId("SomeUser");
        final NetId testUser2 = new NetId("SomeUser");
        final Password testPassword1 = new Password("password123");
        final Password testPassword2 = new Password("password456");
        final HashedPassword testHashedPassword1 = new HashedPassword("hashedTestPassword");
        final HashedPassword testHashedPassword2 = new HashedPassword("hashedTestPassword2");

        when(mockPasswordEncoder.hash(testPassword1)).thenReturn(testHashedPassword1);
        when(mockPasswordEncoder.hash(testPassword2)).thenReturn(testHashedPassword2);

        // Act
        registrationService.registerUser(testUser1, testPassword1, EmployeeType.HR);
        ThrowingCallable action = () -> registrationService.registerUser(testUser2, testPassword2, EmployeeType.HR);

        // Assert
        assertThatExceptionOfType(NetIdAlreadyInUseException.class)
                .isThrownBy(action);

        AppUser savedUser = userRepository.findByNetId(testUser1).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser1);
        assertThat(savedUser.getPassword()).isEqualTo(testHashedPassword1);
    }

    @Test
    public void createUser_withExistingUser_throwsException() {
        // Arrange
        final NetId testUser = new NetId("SomeUser");
        final HashedPassword existingTestPassword = new HashedPassword("password123");
        final Password newTestPassword = new Password("password456");

        AppUser existingAppUser = new AppUser(testUser, existingTestPassword, EmployeeType.HR);
        userRepository.save(existingAppUser);

        // Act
        ThrowingCallable action = () -> registrationService.registerUser(testUser, newTestPassword, EmployeeType.HR);

        // Assert
        assertThatExceptionOfType(Exception.class)
                .isThrownBy(action);

        AppUser savedUser = userRepository.findByNetId(testUser).orElseThrow();

        assertThat(savedUser.getNetId()).isEqualTo(testUser);
        assertThat(savedUser.getPassword()).isEqualTo(existingTestPassword);
    }
}
