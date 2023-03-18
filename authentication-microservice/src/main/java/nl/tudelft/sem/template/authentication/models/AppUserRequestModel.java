package nl.tudelft.sem.template.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.user.EmployeeType;
import nl.tudelft.sem.template.authentication.domain.user.HashedPassword;
import nl.tudelft.sem.template.authentication.domain.user.NetId;

/**
 * Model representing an AppUser request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUserRequestModel {
    private String netId;
    private String password;
    private String role;
}