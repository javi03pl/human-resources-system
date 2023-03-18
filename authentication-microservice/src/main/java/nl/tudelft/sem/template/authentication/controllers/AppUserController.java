package nl.tudelft.sem.template.authentication.controllers;

import nl.tudelft.sem.template.authentication.authentication.AuthManager;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.EmployeeType;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.Password;
import nl.tudelft.sem.template.authentication.domain.user.RegistrationService;
import nl.tudelft.sem.template.authentication.models.AppUserRequestModel;
import nl.tudelft.sem.template.authentication.models.AppUserResponseModel;
import nl.tudelft.sem.template.authentication.service.AppUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/users")
public class AppUserController {
    private final transient AppUserService appUserService;
    private final transient AuthManager authManager;
    private final transient RegistrationService registrationService;
    private final transient String unauthorized = "Unauthorized";

    /**
     * Constructor for AppUserController.
     *
     * @param appUserService the AppUserService to be used.
     * @param authManager the AuthManager to be used.
     * @param registrationService the RegistrationService to be used.
     */
    @Autowired
    public AppUserController(AppUserService appUserService, AuthManager authManager,
                             RegistrationService registrationService) {
        this.appUserService = appUserService;
        this.authManager = authManager;
        this.registrationService = registrationService;
    }

    /*   /**
      * Adds a new user.
      *
      * @param appUser the userDetails to be added
      * @return a response
      *//*
    @PostMapping
    public ResponseEntity<Object> addEmployee(@RequestBody AppUserRequestModel appUser) {
        if (authManager.getRole().contains("HR")) {
            AppUser user = new AppUser(appUser.getNetId(), appUser.getPassword(), appUser.getRole());
            this.appUserService.addUser(user);
            return ResponseEntity.ok(new AppUserResponseModel(user.getNetId(), user.getRole()));
        } else {
            return ResponseEntity.status(401).body(unauthorized);
        }
    }
    */

    /**
     * Adds a new Candidate user.
     *
     * @param appUserRequestModel the user to be added as candidate
     * @return a response
     */
    @PostMapping(path = "/candidate", consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<String> createCandidate(@RequestBody AppUserRequestModel appUserRequestModel) {
        try {
            AppUser newCandidate = this.registrationService.registerUser(new NetId(appUserRequestModel.getNetId()),
                new Password(appUserRequestModel.getPassword()), EmployeeType.CANDIDATE);
            return ResponseEntity.ok(newCandidate.toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error while creating user");
        }
    }

    /**
     * Gets a user.
     *
     * @param id the user's id to be looked for
     * @return an AppUserResponseModel
     */
    @GetMapping("/{id}")
    public ResponseEntity<Object> getEmployeeById(@PathVariable int id) {
        if (authManager.getRole().contains("HR")) {
            AppUser user = this.appUserService.getUserById(id);
            return ResponseEntity.ok(new AppUserResponseModel(user.getNetId(), user.getRole()));
        } else {
            return ResponseEntity.status(401).body(unauthorized);
        }
    }

    /**
     * Gets a user by netId.
     *
     * @param netId the user's netId to be looked for
     * @return an AppUserResponseModel
     */
    @GetMapping("/netId/{netId}")
    public ResponseEntity<Object> getEmployeeByNetId(@PathVariable NetId netId) {
        if (authManager.getRole().contains("HR")) {
            AppUser user = this.appUserService.getUserByNetId(netId);
            return ResponseEntity.ok(new AppUserResponseModel(user.getNetId(), user.getRole()));
        } else {
            return ResponseEntity.status(401).body(unauthorized);
        }
    }

    @GetMapping("/checkNetIdUnique/{netId}")
    public ResponseEntity<Boolean> checkNetIdUnique(@PathVariable NetId netId) {
        return ResponseEntity.ok(!appUserService.netIdExists(netId));
    }

    //    /**
    //     * Updates a user.
    //     *
    //     * @param appUser the user to be updated
    //     * @return a response
    //     */
    //    @PutMapping
    //    public ResponseEntity<Object> updateEmployee(@RequestBody AppUserRequestModel appUser) {
    //        if (authManager.getRole().contains("HR")) {
    //            AppUser user = new AppUser(new NetId(appUser.getNetId()), new HashedPassword(appUser.getPassword()), );
    //            AppUser updatedUser = this.appUserService.updateUser(user);
    //            return ResponseEntity.ok(new AppUserResponseModel(updatedUser.getNetId(), updatedUser.getRole()));
    //        } else {
    //            return ResponseEntity.status(401).body(unauthorized);
    //        }
    //    }

    /**
     * Deletes a user.
     *
     * @param id the user's id to be deleted
     * @return a response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteEmployee(@PathVariable int id) {
        if (authManager.getRole().contains("HR")) {
            this.appUserService.deleteUser(id);
            return ResponseEntity.ok(200);
        } else {
            return ResponseEntity.status(401).body(unauthorized);
        }
    }

    /**
     * Shows a hello message and the netId of user.
     *
     * @return the message.
     */
    @GetMapping("/hello")
    public ResponseEntity<String> helloWorld() {
        return ResponseEntity.ok("Hello " + authManager.getNetId());

    }
}
