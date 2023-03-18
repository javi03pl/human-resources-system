package nl.tudelft.sem.template.contract.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model representing an AppUser request.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AppUserRequestModel {
    private String netId;
    private String password;
    private String role;
}