package nl.tudelft.sem.template.authentication.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nl.tudelft.sem.template.authentication.authentication.JwtTokenGenerator;
import nl.tudelft.sem.template.authentication.authentication.JwtUserDetailsService;
import nl.tudelft.sem.template.authentication.domain.user.EmployeeType;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.NetIdAlreadyInUseException;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.RegistrationService;
import nl.tudelft.sem.template.authentication.models.AuthenticationRequestModel;
import nl.tudelft.sem.template.authentication.models.AuthenticationResponseModel;
import nl.tudelft.sem.template.authentication.models.RegistrationRequestModel;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.server.ResponseStatusException;

class AuthenticationControllerTests {

    private AuthenticationController authenticationController;
    private AuthenticationManager authenticationManager;
    private JwtTokenGenerator jwtTokenGenerator;
    private JwtUserDetailsService jwtUserDetailsService;
    private RegistrationService registrationService;


    @BeforeEach
    public void setup() {
        authenticationManager = mock(AuthenticationManager.class);
        jwtTokenGenerator = mock(JwtTokenGenerator.class);
        jwtUserDetailsService = mock(JwtUserDetailsService.class);
        registrationService = mock(RegistrationService.class);

        authenticationController = new AuthenticationController(authenticationManager, jwtTokenGenerator,
                jwtUserDetailsService, registrationService);

    }

    @Test
    public void authenticateTestSuccess() throws Exception {
        //Arrange

        AuthenticationRequestModel user = new AuthenticationRequestModel();;
        user.setNetId("netId123");
        user.setPassword("password123");
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken(user.getNetId(), user.getPassword()));
        when(jwtTokenGenerator.generateToken(any())).thenReturn("token");

        //Act / Verify
        assertThat(authenticationController.authenticate(user)).isEqualTo(
                ResponseEntity.ok(new AuthenticationResponseModel("token"))
        );

    }

    @Test
    public void authenticateTestDisabled() {
        //Arrange

        AuthenticationRequestModel user = new AuthenticationRequestModel();;
        user.setNetId("netId123");
        user.setPassword("password123");
        when(authenticationManager.authenticate(any())).thenThrow(
                DisabledException.class
        );

        //Act
        ThrowableAssert.ThrowingCallable action = () -> authenticationController.authenticate(user);

        //Verify
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(action)
                .withMessageContaining(HttpStatus.UNAUTHORIZED + " \"USER_DISABLED\"");
        verify(jwtUserDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtTokenGenerator, never()).generateToken(any());

    }

    @Test
    public void authenticateTestInvalidCredentials() {
        //Arrange

        AuthenticationRequestModel user = new AuthenticationRequestModel();;
        user.setNetId("netId123");
        user.setPassword("password123");
        when(authenticationManager.authenticate(any())).thenThrow(
                BadCredentialsException.class
        );

        //Act
        ThrowableAssert.ThrowingCallable action = () -> authenticationController.authenticate(user);

        //Verify
        assertThatExceptionOfType(ResponseStatusException.class).isThrownBy(action)
                .withMessageContaining(HttpStatus.UNAUTHORIZED + " \"INVALID_CREDENTIALS\"");
        verify(jwtUserDetailsService, never()).loadUserByUsername(anyString());
        verify(jwtTokenGenerator, never()).generateToken(any());
    }

    @Test
    public void registerTestSuccess() throws NetIdAlreadyInUseException {
        //Arrange
        RegistrationRequestModel user = new RegistrationRequestModel();
        user.setNetId("netId123");
        user.setPassword("password123");
        user.setEmployeeType("EMPLOYEE");
        when(registrationService.registerUser(
                any(NetId.class), any(Password.class), eq(EmployeeType.EMPLOYEE))).thenReturn(null);

        //Act / Verify
        assertThat(authenticationController.register(user)).isEqualTo(
                ResponseEntity.ok().build()
        );
    }

    @Test
    public void registerTestFailUserAlreadyExists() throws NetIdAlreadyInUseException {
        //Arrange
        RegistrationRequestModel user = new RegistrationRequestModel();
        user.setNetId("netId123");
        user.setPassword("password123");
        user.setEmployeeType("EMPLOYEE");

        when(registrationService.registerUser(
                any(NetId.class), any(Password.class), eq(EmployeeType.EMPLOYEE)))
                .thenThrow(NetIdAlreadyInUseException.class);

        //Act
        ThrowableAssert.ThrowingCallable action = () -> authenticationController.register(user);

        //Verify
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(action).withMessageContaining(
                        HttpStatus.BAD_REQUEST + " \"User already exists with the given netID\"");
    }


    @Test
    public void registerTestFailUserCannotBeHr() throws NetIdAlreadyInUseException {
        //Arrange
        RegistrationRequestModel user = new RegistrationRequestModel();
        user.setNetId("HR");
        user.setPassword("password123");
        user.setEmployeeType("CANDIDATE");
        when(registrationService.registerUser(
                any(NetId.class), any(Password.class), eq(EmployeeType.EMPLOYEE)))
                .thenThrow(NetIdAlreadyInUseException.class);

        assertThat(authenticationController.register(user)).isEqualTo(
                ResponseEntity.status(401).body("Your NetId cannot be HR, sorry! \nPlease choose another NetId!")
        );
    }

    @Test
    public void registerAdminTestSuccess() throws NetIdAlreadyInUseException {
        //Arrange
        RegistrationRequestModel user = new RegistrationRequestModel();
        user.setNetId("netId123");
        user.setPassword("password123");
        user.setEmployeeType("HR");
        when(registrationService.registerUser(
                any(NetId.class), any(Password.class), eq(EmployeeType.HR))).thenReturn(null);

        //Act / Verify
        assertThat(authenticationController.register(user)).isEqualTo(
                ResponseEntity.ok().build()
        );
    }


    @Test
    public void registerAdminTestFailUserAlreadyExists() throws NetIdAlreadyInUseException {
        //Arrange
        RegistrationRequestModel user = new RegistrationRequestModel();
        user.setNetId("netId123");
        user.setPassword("password123");
        user.setEmployeeType("HR");
        when(registrationService.registerUser(
                any(NetId.class), any(Password.class), eq(EmployeeType.HR)))
                .thenThrow(NetIdAlreadyInUseException.class);

        //Act
        ThrowableAssert.ThrowingCallable action = () -> authenticationController.register(user);

        //Verify
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(action).withMessageContaining(
                        HttpStatus.BAD_REQUEST + " \"User already exists with the given netID\"");
    }


    @Test
    public void registerCandidateTestSuccess() throws NetIdAlreadyInUseException {
        //Arrange
        RegistrationRequestModel user = new RegistrationRequestModel();
        user.setNetId("netId123");
        user.setPassword("password123");
        user.setEmployeeType("CANDIDATE");
        when(registrationService.registerUser(
                any(NetId.class), any(Password.class), eq(EmployeeType.CANDIDATE))).thenReturn(null);

        //Act / Verify
        assertThat(authenticationController.register(user)).isEqualTo(
                ResponseEntity.ok().build()
        );
    }


    @Test
    public void registerCandidateTestFailUserAlreadyExists() throws NetIdAlreadyInUseException {
        //Arrange
        RegistrationRequestModel user = new RegistrationRequestModel();
        user.setNetId("netId123");
        user.setPassword("password123");
        user.setEmployeeType("CANDIDATE");
        when(registrationService.registerUser(
                any(NetId.class), any(Password.class), eq(EmployeeType.CANDIDATE)))
                .thenThrow(NetIdAlreadyInUseException.class);

        //Act
        ThrowableAssert.ThrowingCallable action = () -> authenticationController.register(user);

        //Verify
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(action).withMessageContaining(
                        HttpStatus.BAD_REQUEST + " \"User already exists with the given netID\"");
    }


    @Test
    public void registerTestInvalidEmployeeType() throws NetIdAlreadyInUseException {
        //Arrange
        RegistrationRequestModel user = new RegistrationRequestModel();
        user.setNetId("netId123");
        user.setPassword("password123");
        user.setEmployeeType("Other");

        when(registrationService.registerUser(
                any(NetId.class), any(Password.class), not(eq(EmployeeType.CANDIDATE))))
                .thenThrow(NetIdAlreadyInUseException.class);

        //Act
        ThrowableAssert.ThrowingCallable action = () -> authenticationController.register(user);

        //Verify
        assertThatExceptionOfType(ResponseStatusException.class)
                .isThrownBy(action).withMessageContaining(
                        HttpStatus.BAD_REQUEST
                                + " \"Nonexistent employee type " + "(CANDIDATE, HR, EMPLOYEE)!\"");


    }
}
