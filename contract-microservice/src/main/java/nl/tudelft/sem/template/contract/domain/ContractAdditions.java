package nl.tudelft.sem.template.contract.domain;

import javax.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
public class ContractAdditions {
    @Setter private String additionalBenefits;
    @Setter private String pensionScheme;
    @Setter private String position;

    @SuppressWarnings("unused")
    ContractAdditions() {

    }

    /**
     * Constructor for ContractAdditions.
     *
     * @param additionalBenefits additional Benefits
     * @param pensionScheme Pensions
     * @param position position of user
     */
    public ContractAdditions(String additionalBenefits, String pensionScheme, String position) {
        this.additionalBenefits = additionalBenefits;
        this.pensionScheme = pensionScheme;
        this.position = position;
    }
}
