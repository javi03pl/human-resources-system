package nl.tudelft.sem.template.contract.domain;

import javax.persistence.Embeddable;
import lombok.Getter;

@Embeddable
@Getter
public class SalaryInfo {
    private int hoursPerWeek;
    private int vacationDays;
    private int salaryScale;
    private int salaryStep;

    @SuppressWarnings("unused")
    SalaryInfo() {

    }

    /**
     * Constructor for SalaryInfo.
     *
     * @param hoursPerWeek  working hours per week
     * @param vacationDays  number of vacation days
     * @param salaryScale   the salary scale
     * @param salaryStep    salary step for next increase
     */
    public SalaryInfo(int hoursPerWeek, int vacationDays, int salaryScale, int salaryStep) {
        this.hoursPerWeek = hoursPerWeek;
        this.vacationDays = vacationDays;
        this.salaryScale = salaryScale;
        this.salaryStep = salaryStep;
    }

    /**
     * Setter for hoursPerWeek.
     *
     * @param hoursPerWeek New working hours per week of the employee.
     */
    public void setHoursPerWeek(int hoursPerWeek) {
        if (hoursPerWeek <= 0) {
            throw new IllegalArgumentException("Hours per week can't be <= 0");
        }
        this.hoursPerWeek = hoursPerWeek;
    }

    /**
     * Setter for vacation days.
     *
     * @param vacationDays New amount of vacation days allowed for the employee.
     */
    public void setVacationDays(int vacationDays) {
        if (vacationDays <= 0) {
            throw new IllegalArgumentException("Vacation days can't be <= 0");
        }
        this.vacationDays = vacationDays;
    }

    /**
     * Setter for salaryScale.
     *
     * @param salaryScale The new salary scale for the employee.
     */
    public void setSalaryScale(int salaryScale) {
        if (salaryScale < 0) {
            throw new IllegalArgumentException("Salary scale can't be negative");
        }
        this.salaryScale = salaryScale;
    }

    /**
     * Setter for salaryStep.
     *
     * @param salaryStep New salary step for the employee.
     */
    public void setSalaryStep(int salaryStep) {
        if (salaryStep < 0) {
            throw new IllegalArgumentException("Salary step can't be negative");
        }
        this.salaryStep = salaryStep;
    }
}
