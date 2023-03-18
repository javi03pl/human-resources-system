package nl.tudelft.sem.template.authentication.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.NoSuchElementException;
import java.util.Optional;
import javax.persistence.EntityNotFoundException;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.EmployeeType;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.exceptions.RoleAlreadySetException;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


public class AppUserServiceTests {

    private transient UserRepository userRepository;
    private transient AppUserService appUserService;
    private transient AppUser spyAppUser;
    private transient NetId netId;
    private transient NetId invalidNetId;
    private transient HashedPassword hashedPassword;
    private transient EmployeeType role;
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    /**
     * Set up for reading System.outs
     */
    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    /**
     * Set uo test suite.
      */
    @BeforeEach
    public void setup() {
        userRepository = mock(UserRepository.class);

        netId = new NetId("netId123");
        invalidNetId = new NetId("Invalid");
        hashedPassword = new HashedPassword("hashedPass123");
        role = EmployeeType.CANDIDATE;

        AppUser appUser = new AppUser(netId, hashedPassword, role);
        spyAppUser = spy(appUser);
        appUserService = new AppUserService(userRepository);

        when(userRepository.findByNetId(same(netId))).thenReturn(Optional.ofNullable(spyAppUser));
        when(userRepository.findByNetId(same(invalidNetId))).thenReturn(Optional.empty());
        when(userRepository.existsByNetId(same(netId))).thenReturn(true);
        when(userRepository.existsByNetId(same(invalidNetId))).thenReturn(false);
        when(userRepository.save(any())).then(returnsFirstArg());
        when(userRepository.getOne(0)).thenReturn(spyAppUser);
        when(userRepository.getOne(1)).thenThrow(EntityNotFoundException.class);
    }

    @Test
    void netIdExists() {
        boolean exists = appUserService.netIdExists(netId);
        verify(userRepository, times(1)).existsByNetId(netId);

        assertThat(exists).isTrue();
    }

    @Test
    void netIdNotExists() {
        boolean exists = appUserService.netIdExists(invalidNetId);
        verify(userRepository, times(1)).existsByNetId(invalidNetId);

        assertThat(exists).isFalse();
    }

    @Test
    void addUserTest() {
        //Act
        appUserService.addUser(spyAppUser);

        //Assert
        verify(userRepository, times(1)).save(spyAppUser);
    }

    @Test
    void createCandidateTest() {
        //Act
        appUserService.createCandidate(spyAppUser);

        //Assert
        verify(spyAppUser, times(1)).setRole(EmployeeType.CANDIDATE);
        verify(userRepository, times(1)).save(spyAppUser);
        assertThat("Account created.\n").isEqualTo(outContent.toString());
    }

    @Test
    void getUserByIdTest() {
        //Act
        AppUser expected = appUserService.getUserById(0);

        //Verify
        assertThat(expected).isEqualTo(spyAppUser);
    }

    @Test
    void getNonexistentUserByIdTest() {
        //Act
        ThrowableAssert.ThrowingCallable action = () -> appUserService.getUserById(1);

        //Verify
        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(action);
    }


    @Test
    void getUserRoleByIdTest() {
        //Act
        EmployeeType expected = appUserService.getRoleById(0);

        //Verify
        assertThat(expected).isEqualTo(spyAppUser.getRole());
    }


    @Test
    void getNonexistentUserRoleByIdTest() {
        //Act
        ThrowableAssert.ThrowingCallable action = () -> appUserService.getRoleById(1);

        //Verify
        assertThatExceptionOfType(EntityNotFoundException.class).isThrownBy(action);
    }


    @Test
    void getUserByNetIdTest() {
        //Act
        AppUser expected = appUserService.getUserByNetId(netId);

        //Verify
        assertThat(expected).isEqualTo(spyAppUser);
    }


    @Test
    void getNonexistentUserByNetIdTest() {
        //Act
        ThrowableAssert.ThrowingCallable action = () -> appUserService.getUserByNetId(invalidNetId);

        //Verify
        assertThatExceptionOfType(NoSuchElementException.class).isThrownBy(action);
    }

    @Test
    void updateExistingUserTest() {
        //Arrange
        EmployeeType newRole = EmployeeType.EMPLOYEE;
        AppUser newInfo = new AppUser(netId, hashedPassword, newRole);

        //Act
        AppUser actual = appUserService.updateUser(newInfo);

        //Verify
        assertThat(spyAppUser).isEqualTo(actual);
        verify(userRepository, times(1)).save(any());
        verify(spyAppUser, times(1)).setRole(any());
    }


    @Test
    void updateNonexistentUserTest() {
        //Arrange
        EmployeeType newRole = EmployeeType.EMPLOYEE;
        AppUser newInfo = new AppUser(invalidNetId, hashedPassword, newRole);

        //Act
        AppUser actual = appUserService.updateUser(newInfo);

        //Verify

        assertThat(actual).isNull();
        verify(userRepository, times(0)).save(any());
    }

    @Test
    void changeToSameRoleTest() {
        //Act
        ThrowableAssert.ThrowingCallable action = () -> appUserService.changeRole(spyAppUser, role);

        //Verify
        assertThatExceptionOfType(RoleAlreadySetException.class).isThrownBy(action);
    }

    @Test
    void changeToDifferentRoleTest() throws RoleAlreadySetException {
        //Act
        AppUser changedUser = appUserService.changeRole(spyAppUser, EmployeeType.EMPLOYEE);

        //Verify
        assertThat(changedUser.getRole()).isEqualTo(EmployeeType.EMPLOYEE);
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void changeRoleOfNonexistentUserTest() throws RoleAlreadySetException {
        EmployeeType newRole = EmployeeType.EMPLOYEE;
        AppUser newInfo = new AppUser(invalidNetId, hashedPassword, newRole);

        //Act
        Optional<AppUser> actual = Optional.ofNullable(appUserService.changeRole(newInfo, newRole));

        //Verify
        assertThat(actual.isPresent()).isFalse();
        verify(userRepository, times(0)).save(any());

    }

    @Test
    void deleteUser() {
        AppUser user = appUserService.deleteUser(0);
        assertThat(user).isEqualTo(spyAppUser);
        verify(userRepository, times(1)).deleteById(0);
    }

    @Test
    void deleteNonExistentUser() {
        Optional<AppUser> actual = Optional.ofNullable(appUserService.deleteUser(10));
        verify(userRepository, times(1)).deleteById(10);

        assertThat(actual.isPresent()).isFalse();
    }


}