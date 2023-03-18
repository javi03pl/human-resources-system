package nl.tudelft.sem.template.contract.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;
import nl.tudelft.sem.template.contract.domain.Contract;
import nl.tudelft.sem.template.contract.domain.ContractAdditions;
import nl.tudelft.sem.template.contract.domain.SalaryInfo;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;


public class ContractTests {

    @Test
    public void testConstructor() {
        Date validStartDate = new Date(2022, Calendar.DECEMBER, 1);
        Date validEndDate = new Date(2023, Calendar.DECEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        Contract contract = new Contract(Contract.State.DRAFT,
            "netid124", validStartDate, validEndDate,
                salaryInfo, contractAdditions);

        // Check that the fields were initialized correctly
        assertEquals(Contract.State.DRAFT, contract.getState());
        assertEquals("netid124", contract.getCandidateNetId());

        assertEquals(validStartDate, contract.getStartDate());
        assertEquals(validEndDate, contract.getEndDate());
        assertEquals(salaryInfo, contract.getSalaryInfo());
        assertEquals(contractAdditions, contract.getContractAdditions());
    }

    @Test
    public void testIsValidStartDate() {
        Calendar c = Calendar.getInstance();

        // Test a date with day of month 1
        c.set(2022, Calendar.JANUARY, 1);
        Date date1 = c.getTime();
        assertTrue(Contract.isValidStartDate(date1));

        // Test a date with day of month 15
        c.set(2022, Calendar.JANUARY, 15);
        Date date2 = c.getTime();
        assertTrue(Contract.isValidStartDate(date2));

        // Test a date with day of month not 1 or 15
        c.set(2022, Calendar.JANUARY, 10);
        Date date3 = c.getTime();
        assertFalse(Contract.isValidStartDate(date3));
    }

    @Test
    public void testIsValidDuration() {
        // 5 years
        assertTrue(Contract.isValidDuration(1826));
        // 5 years and 1 day
        assertFalse(Contract.isValidDuration(1827));
        // 0 years
        assertFalse(Contract.isValidDuration(0));
        // negative duration
        assertFalse(Contract.isValidDuration(-1));
    }

    @Test
    public void testIsnEndDate() {
        Date startDate = new Date(2022, Calendar.NOVEMBER, 1);
        Date endDate = new Date(2023, Calendar.NOVEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        Contract contract = new Contract(Contract.State.DRAFT,
                "netid124", startDate, endDate,
                salaryInfo, contractAdditions);
        // Test end date after start date
        Date validEndDate = new Date(2025, Calendar.NOVEMBER, 1);
        assertTrue(contract.isValidEndDate(validEndDate));

        // Test end date before start date
        Date invalidEndDate = new Date(2020, Calendar.NOVEMBER, 1);
        assertFalse(contract.isValidEndDate(invalidEndDate));
    }

    @Test
    public void testInvalidStartDate() {
        Date startDate = new Date(2022, Calendar.NOVEMBER, 2);
        Date endDate = new Date(2023, Calendar.NOVEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        ThrowableAssert.ThrowingCallable action = () -> new Contract(Contract.State.ACCEPTED,
            "netid124", startDate, endDate, salaryInfo, contractAdditions);
        // Test invalid start date
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(action);

    }


    @Test
    public void testInvalidDuration() {
        Date startDate = new Date(2022, Calendar.NOVEMBER, 2);
        Date endDate = new Date(2027, Calendar.NOVEMBER, 3);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        ThrowableAssert.ThrowingCallable action = () -> new Contract(Contract.State.ACCEPTED,
            "netid124", startDate, endDate,  salaryInfo, contractAdditions);
        // Test invalid start date
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(action);

    }


    @Test
    public void testInvalidEndDate() {
        Date startDate = new Date(2022, Calendar.NOVEMBER, 1);
        Date endDate = new Date(2021, Calendar.NOVEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        ThrowableAssert.ThrowingCallable action = () -> new Contract(Contract.State.ACCEPTED,
            "netid124", startDate, endDate,  salaryInfo, contractAdditions);
        // Test end date after start date
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(action);

    }

    @Test
    public void testIsValidContract() {
        Date validStartDate = new Date(2022, Calendar.DECEMBER, 1);
        Date validEndDate = new Date(2023, Calendar.DECEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        Contract validContract = new Contract(Contract.State.DRAFT,
            "netid124", validStartDate, validEndDate,  salaryInfo, contractAdditions);
        System.out.println(validContract.toString());
        assertTrue(Contract.isValidContract(validContract));
    }

    @Test
    public void testIsValidContractNoEndDate() {
        Date validStartDate = new Date(2022, Calendar.DECEMBER, 1);
        Date validEndDate = null;
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        Contract validContract = new Contract(Contract.State.DRAFT,
            "netid124", validStartDate, validEndDate,  salaryInfo, contractAdditions);
        assertTrue(Contract.isValidContract(validContract));
    }


    @Test
    public void testSetStartDate() {
        Date startDate = new Date(2022, Calendar.JANUARY, 1);
        Date endDate = new Date(2022, Calendar.DECEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        Contract contract = new Contract(Contract.State.DRAFT,
            "netid124", startDate, endDate,  salaryInfo, contractAdditions);

        // Set valid start date
        Date validStartDate = new Date(2022, Calendar.JUNE, 15);
        contract.setStartDate(validStartDate);
        assertEquals(validStartDate, contract.getStartDate());

        // Try to set invalid start date
        Date invalidStartDate = new Date(2022, Calendar.JUNE, 16);
        assertThrows(IllegalArgumentException.class, () -> {
            contract.setStartDate(invalidStartDate);
        });
    }


    @Test
    public void testSetEndDate() {
        Date startDate = new Date(2022, Calendar.JANUARY, 1);
        Date endDate = new Date(2022, Calendar.DECEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        Contract contract = new Contract(Contract.State.DRAFT,
            "netid124", startDate, endDate,  salaryInfo, contractAdditions);

        // Set valid start date
        Date validStartDate = new Date(2022, Calendar.JUNE, 15);
        contract.setStartDate(validStartDate);
        assertEquals(validStartDate, contract.getStartDate());

        // Try to set invalid start date
        Date invalidEndDate = new Date(2027, Calendar.JUNE, 16);
        assertThrows(IllegalArgumentException.class, () -> {
            contract.setEndDate(invalidEndDate);
        });
    }

    @Test
    public void testSetHoursPerWeek() {
        Date validStartDate = new Date(2022, Calendar.DECEMBER, 1);
        Date validEndDate = new Date(2023, Calendar.DECEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        Contract contract = new Contract(Contract.State.DRAFT,
            "netid124", validStartDate, validEndDate,  salaryInfo, contractAdditions);
        assertThrows(IllegalArgumentException.class, () -> {
            contract.getSalaryInfo().setHoursPerWeek(0);
        });
        contract.getSalaryInfo().setHoursPerWeek(15);
        assertEquals(contract.getSalaryInfo().getHoursPerWeek(), 15);
    }

    @Test
    public void testSetVacationDays() {
        Date validStartDate = new Date(2022, Calendar.DECEMBER, 1);
        Date validEndDate = new Date(2023, Calendar.DECEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        // Check that an IllegalArgumentException is thrown when the number of vacation days is <= 0
        Contract contract = new Contract(Contract.State.DRAFT,
            "netid124", validStartDate, validEndDate,  salaryInfo, contractAdditions);
        assertThrows(IllegalArgumentException.class, () -> {
            contract.getSalaryInfo().setVacationDays(0);
        });

        // Check that the number of vacation days is correctly set when passed a valid value
        contract.getSalaryInfo().setVacationDays(10);
        assertEquals(10, contract.getSalaryInfo().getVacationDays());
    }

    @Test
    public void testSetSalaryScaleAndStep() {
        Date validStartDate = new Date(2022, Calendar.DECEMBER, 1);
        Date validEndDate = new Date(2023, Calendar.DECEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        Contract contract = new Contract(Contract.State.DRAFT,
            "netid124", validStartDate, validEndDate,  salaryInfo, contractAdditions);
        // IllegalArgumentException thrown when salary scale < 0
        assertThrows(IllegalArgumentException.class, () -> {
            contract.getSalaryInfo().setSalaryScale(-1);
        });

        // salary scale set when valid value
        contract.getSalaryInfo().setSalaryScale(100);
        assertEquals(100, salaryInfo.getSalaryScale());

        // IllegalArgumentException thrown when salary step < 0
        assertThrows(IllegalArgumentException.class, () -> {
            contract.getSalaryInfo().setSalaryStep(-1);
        });

        // salary step is correctly set when valid value
        contract.getSalaryInfo().setSalaryStep(3);
        assertEquals(3, contract.getSalaryInfo().getSalaryStep());
    }

    @Test
    public void testEquals() {
        Date validStartDate = new Date(2022, Calendar.DECEMBER, 1);
        Date validEndDate = new Date(2023, Calendar.DECEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits",
                "Pension Scheme", "position");
        Contract contract1 = new Contract(Contract.State.DRAFT,
            "netid124", validStartDate, validEndDate,  salaryInfo, contractAdditions);
        Contract contract2 = new Contract(Contract.State.DRAFT,
            "netid124", validStartDate, validEndDate,  salaryInfo, contractAdditions);
        assertEquals(contract1, contract1);
        // todo: update tests
    }

    @Test
    public void testGetDurationNullEndDate() {
        Date validStartDate = new Date(2022, Calendar.DECEMBER, 1);
        Date validEndDate = null;
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits", "Pension Scheme",
                "position");
        Contract contract1 = new Contract(Contract.State.DRAFT,
                "netid124", validStartDate, validEndDate, salaryInfo, contractAdditions);
        assertThat(contract1.getDuration()).isNull();
    }

    @Test
    public void testGetDurationNonNullEndDate() {
        Date validStartDate = new Date(2022, Calendar.DECEMBER, 1);
        Date validEndDate = new Date(2023, Calendar.DECEMBER, 1);
        SalaryInfo salaryInfo = new SalaryInfo(40, 25, 12, 1);
        ContractAdditions contractAdditions = new ContractAdditions("Benefits", "Pension Scheme",
                "position");
        Contract contract1 = new Contract(Contract.State.DRAFT,
                "netid124", validStartDate, validEndDate, salaryInfo, contractAdditions);
        assertThat(contract1.getDuration()).isEqualTo(365);
    }
}
