package nl.tudelft.sem.template.authentication.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import nl.tudelft.sem.template.authentication.authentication.AuthManager;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.EmployeeType;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.RegistrationService;
import nl.tudelft.sem.template.authentication.models.AppUserRequestModel;
import nl.tudelft.sem.template.authentication.models.AppUserResponseModel;
import nl.tudelft.sem.template.authentication.service.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;




class AppUserControllerTests {

    private AppUserService appUserService;
    private AuthManager authManager;
    private AppUserRequestModel appUserRequestModel;
    private AppUserController appUserController;
    private RegistrationService registrationService;

    @BeforeEach
    public void setup() {
        appUserService = mock(AppUserService.class);
        authManager = mock(AuthManager.class);
        registrationService = mock(RegistrationService.class);

        this.appUserRequestModel = new AppUserRequestModel();
        appUserRequestModel.setNetId("netId123");
        appUserRequestModel.setPassword("password123");
        appUserRequestModel.setRole(EmployeeType.HR.toString());

        this.appUserController = new AppUserController(appUserService, authManager, registrationService);


    }

    //    @Test
    //    void createCandidate() {
    //        //Arrange
    //        when(authManager.getRole()).thenReturn("HR");
    //
    //        //Act
    //        ResponseEntity<AppUser> actual = appUserController.createCandidate(appUserRequestModel);
    //
    //        //Verify
    //        verify(appUserService, times(1)).createCandidate(any());
    //        assertThat(actual.getStatusCode() == HttpStatus.OK);
    //        assertThat(actual.getBody()).isEqualTo(AppUser);
    //    }


    @Test
    void getEmployeeById() {
        //Arrange
        when(authManager.getRole()).thenReturn("HR");
        when(appUserService.getUserById(anyInt())).thenReturn(
                new AppUser(new NetId(appUserRequestModel.getNetId()),
                        new HashedPassword(appUserRequestModel.getPassword()),
                        EmployeeType.EMPLOYEE));

        //Act
        ResponseEntity<Object> actual = appUserController.getEmployeeById(0);

        //Verify
        verify(appUserService, times(1)).getUserById(0);
        assertThat(actual).isEqualTo(ResponseEntity.ok(
                new AppUserResponseModel(new NetId(appUserRequestModel.getNetId()), EmployeeType.EMPLOYEE)));
    }

    @Test
    void getEmployeeByIdUnauthorized() {
        //Arrange
        when(authManager.getRole()).thenReturn("Candidate");

        //Act
        ResponseEntity<Object> actual = appUserController.getEmployeeById(0);

        //Verify
        verify(appUserService, never()).getUserById(anyInt());
        assertThat(actual).isEqualTo(ResponseEntity.status(401).body("Unauthorized"));
    }


    @Test
    void getEmployeeByNetId() {
        when(authManager.getRole()).thenReturn("HR");
        when(appUserService.getUserByNetId(new NetId(appUserRequestModel.getNetId()))).thenReturn(
                new AppUser(new NetId(appUserRequestModel.getNetId()),
                        new HashedPassword(appUserRequestModel.getPassword()),
                        EmployeeType.EMPLOYEE));

        //Act
        ResponseEntity<Object> actual = appUserController.getEmployeeByNetId(
                new NetId(appUserRequestModel.getNetId()));

        //Verify
        verify(appUserService, times(1)).getUserByNetId(
                new NetId(appUserRequestModel.getNetId()));
        assertThat(actual).isEqualTo(ResponseEntity.ok(
                new AppUserResponseModel(new NetId(appUserRequestModel.getNetId()),
                        EmployeeType.EMPLOYEE)));
    }

    @Test
    void getEmployeeByNetIdUnauthorized() {
        //Arrange
        when(authManager.getRole()).thenReturn("Candidate");

        //Act
        ResponseEntity<Object> actual = appUserController.getEmployeeByNetId(new NetId(appUserRequestModel.getNetId()));

        //Verify
        verify(appUserService, never()).getUserByNetId(any());
        assertThat(actual).isEqualTo(ResponseEntity.status(401).body("Unauthorized"));
    }


    @Test
    void deleteEmployee() {
        //Arrange
        when(authManager.getRole()).thenReturn("HR");
        when(appUserService.deleteUser(anyInt())).thenReturn(
                new AppUser(new NetId(appUserRequestModel.getNetId()),
                        new HashedPassword(appUserRequestModel.getPassword()),
                        EmployeeType.EMPLOYEE));

        //Act
        ResponseEntity<Object> actual = appUserController.deleteEmployee(0);

        //Verify
        verify(appUserService, times(1)).deleteUser(0);
        assertThat(actual).isEqualTo(ResponseEntity.ok(200));


    }


    @Test
    void deleteEmployeeUnauthorized() {
        //Arrange
        when(authManager.getRole()).thenReturn("Candidate");

        //Act
        ResponseEntity<Object> actual = appUserController.deleteEmployee(0);

        //Verify
        verify(appUserService, never()).deleteUser(anyInt());
        assertThat(actual).isEqualTo(ResponseEntity.status(401).body("Unauthorized"));
    }
}