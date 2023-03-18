package nl.tudelft.sem.template.contract.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@Entity
@Table(name = "contracts")
@NoArgsConstructor
@Getter
public class Contract implements Cloneable {

    public enum State {
        ACCEPTED,
        DRAFT,
        TERMINATED
    }

    public enum Reviewer {
        EMPLOYER,
        CANDIDATE
    }

    private static final List<Integer> ALLOWED_DAYS_OF_MONTH_FOR_CONTRACT_START = List.of(1, 15);
    private static final int MAXIMUM_CONTRACT_DURATION_YEARS = (int) (365.25 * 5);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    @NonNull
    private Long id;

    @Setter private String candidateNetId;
    @Setter private State state;
    @Setter private Reviewer reviewer;
    private Date startDate;
    private Date endDate = null;   //Calculated in constructor
    @Embedded
    @Setter SalaryInfo salaryInfo;
    @Embedded
    @Setter ContractAdditions contractAdditions;

    /**
     * Constructor for Contract.
     *
     * @param state              The state enum of the contract (ACCEPTED, DRAFT or TERMINATED)
     * @param candidateNetId     The NetId of the candidate.
     * @param startDate          The start date of the contract.
     * @param endDate            The end date of the contract.
     * @param salaryInfo         All the salary details of the contract.
     * @param contractAdditions  Additional benefits for the employee.
     */
    public Contract(State state, String candidateNetId,
                    Date startDate, Date endDate, SalaryInfo salaryInfo, ContractAdditions contractAdditions) {
        this.state = state;
        this.candidateNetId = candidateNetId;
        this.salaryInfo = salaryInfo;
        this.contractAdditions = contractAdditions;
        checkIsValidStartDate(startDate);
        this.startDate = startDate;
        if (endDate != null) {
            if (isValidDuration(elapsedDays(startDate, endDate))) {
                this.endDate = endDate;
            } else {
                throw new IllegalArgumentException("Invalid end date: Temporary contracts can last a maximum of 5 years");
            }
        }
    }

    /**
     * Method that checks whether the contract is valid.
     *
     * @param contract The contract to be checked.
     * @return false if the contract has an invalid end date or a start date.
     *      true if the contract has a valid start date and either no end date or a valid duration.
     */
    public static boolean isValidContract(Contract contract) {
        if (isValidStartDate(contract.startDate)) {
            if (contract.endDate != null) {
                return isValidDuration((elapsedDays(contract.startDate, contract.endDate)));
            }
            return true;
        }
        return false;
    }

    /**
     * Method that checks whether the start date is valid.
     *
     * @param startDate The start date to be checked.
     * @throws IllegalArgumentException if the start date is not valid.
     */
    public static void checkIsValidStartDate(Date startDate) {
        if (!isValidStartDate(startDate)) {
            throw new IllegalArgumentException("Invalid start date");
        }
    }

    /**
     * Wrapper for elapsedDays(). Uses the start date and end date of this contract as parameters.
     *
     * @return the total number of says the contract will stay valid, or null if "this.endDate" is null.
     */
    public Integer getDuration() {
        if (endDate != null) {
            return elapsedDays(this.startDate, this.endDate);
        }
        return null;
    }

    /**
     * Method for finding out whether the end date is valid.
     * A contract can start either at the 1st or 15th day of the month.
     *
     * @param startDate The start date of the contract.
     * @return true only if the contract starts at the 1st or 15th day of the month,
     *      otherwise returns false.
     */
    public static boolean isValidStartDate(Date startDate) {
        Calendar c = Calendar.getInstance();
        c.setTime(startDate);
        int dayOfMonth = c.get(Calendar.DAY_OF_MONTH);
        return ALLOWED_DAYS_OF_MONTH_FOR_CONTRACT_START.contains(dayOfMonth);
    }

    /**
     * Method for finding out whether the end date is valid.
     *
     * @param endDate The end date of the contract.
     * @return false if endDate comes before the startDate,
     *      otherwise checks whether the contract duration is shorter than 5 years.
     */
    public boolean isValidEndDate(Date endDate) {
        if (endDate.before(startDate)) {
            return false;
        }
        return isValidDuration(elapsedDays(this.startDate, endDate));
    }

    /**
     * Method for finding whether the contract duration is valid. A contract can be valid for at most
     * MAXIMUM_CONTRACT_DURATION_YEARS years.
     *
     * @param duration The duration of the contract
     * @return false if the duration is negative or larger than the max years allowed,
     *      true otherwise
     */
    public static boolean isValidDuration(int duration) {
        if (duration <= 0) {
            return false;
        }
        return duration <= MAXIMUM_CONTRACT_DURATION_YEARS;   //Using average years time
    }

    /**
     * Method for finding the number of days the contract will stay valid.
     *
     * @param start The start date of the contract.
     * @param end   The end date of the contract.
     * @return The number of days the contract wil lstay valid.
     */
    public static int elapsedDays(Date start, Date end) {
        if (end.before(start)) {
            throw new IllegalArgumentException("End date can't happen before start date");
        }
        long elapsedTime = end.getTime() - start.getTime();
        return (int) (elapsedTime / (1000 * 60 * 60 * 24)); //todo: consider removing magic numbers
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        Contract contract = (Contract) o;
        return this.id.equals(contract.id);
    }

    /**
     * Setter for startDate.
     *
     * @param startDate New starting date of the contract.
     */
    public void setStartDate(Date startDate) {
        checkIsValidStartDate(startDate);
        this.startDate = startDate;
    }

    /**
     * Setter for endDate.
     *
     * @param endDate New ending date of the contract.
     */
    public void setEndDate(Date endDate) {
        if (isValidEndDate(endDate)) {
            this.endDate = endDate;
        } else {
            throw new IllegalArgumentException("Invalid End Date: Temporary contracts can last a maximum of 5 years");
        }
    }

    @Override
    public Contract clone() throws CloneNotSupportedException {
        return (Contract) super.clone();
    }

    @Override
    public String toString() {  // todo: consider replacing with @ToString lombok annotation
        // todo: (if this is used as a response, use a toJSON method)
        return "Contract{"
            + "id=" + id
            + ", candidateNetId='" + candidateNetId + '\''
            + ", state=" + state
            + ", reviewer=" + reviewer
            + ", startDate=" + startDate
            + ", endDate=" + endDate
            + ", hoursPerWeek=" + salaryInfo.getHoursPerWeek()
            + ", vacationDays=" + salaryInfo.getVacationDays()
            + ", salaryScale=" + salaryInfo.getSalaryScale()
            + ", salaryStep=" + salaryInfo.getSalaryStep()
            + ", additionalBenefits='" + contractAdditions.getAdditionalBenefits() + '\''
            + ", pensionScheme='" + contractAdditions.getPensionScheme() + '\''
            + ", position='" + contractAdditions.getPosition() + '\''
            + '}';
    }
}