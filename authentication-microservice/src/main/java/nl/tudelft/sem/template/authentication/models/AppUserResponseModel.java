package nl.tudelft.sem.template.authentication.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.user.EmployeeType;
import nl.tudelft.sem.template.authentication.domain.user.NetId;

/**
 * Model representing an AppUser response.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppUserResponseModel {
    private NetId netId;
    private EmployeeType role;
}