package nl.tudelft.sem.template.contract.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.contract.domain.Contract;

/**
 * Model representing the request body of a createCandidate request.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCandidateRequestModel {
    private Contract contract;
    private String candidateNetId;
    private String candidatePassword;
}
