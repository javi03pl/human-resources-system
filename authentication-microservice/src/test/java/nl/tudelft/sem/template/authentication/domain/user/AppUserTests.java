package nl.tudelft.sem.template.authentication.domain.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.Objects;
import org.junit.jupiter.api.Test;


public class AppUserTests {

    @Test
    public void testEquals1() {
        AppUser user1 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        AppUser user2 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        assertEquals(user1, user2);
    }

    @Test
    public void testEquals2() {
        AppUser user1 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        AppUser user2 = null;
        assertNotEquals(user1, user2);
    }

    @Test
    public void testEquals3() {
        AppUser user1 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        Object user2 = new Object();
        assertNotEquals(user1, user2);
    }

    @Test
    public void testCreation() {
        AppUser user1 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        AppUser user2 = new AppUser(new NetId("wrongnet"), new HashedPassword("wrongpass"), EmployeeType.EMPLOYEE);
        user2.setNetId(new NetId("netId123"));
        user2.setPassword(new HashedPassword("pass123"));
        assertEquals(user1, user2);
    }

    @Test
    public void testSelf() {
        AppUser user1 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        assertEquals(user1, user1);
    }

    @Test
    public void testId() {
        AppUser user1 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        AppUser user2 = new AppUser(new NetId("different"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        user1.setId(1);
        user2.setId(1);
        assertEquals(user1, user2);
    }

    @Test
    public void testNotId() {
        AppUser user1 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        AppUser user2 = new AppUser(new NetId("different"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        user1.setId(1);
        user2.setId(2);
        assertNotEquals(user1, user2);
    }

    @Test
    public void setGetId() {
        AppUser user1 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        user1.setId(1);
        assertEquals(user1.getId(), 1);
    }

    @Test
    public void changePassword() {
        AppUser user1 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        AppUser user2 = new AppUser(new NetId("netId123"), new HashedPassword("newpass"), EmployeeType.EMPLOYEE);
        user2.changePassword(new HashedPassword("newpass"));
        assertEquals(user1.getPassword(), user2.getPassword());
    }

    @Test
    public void userToString() {
        AppUser user1 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        assertEquals(user1.toString(),
                "AppUser{"
                + "id=0"
                + ", netId=" + user1.getNetId()
                + ", password=" + user1.getPassword()
                + ", role=" + user1.getRole()
                + '}');
    }

    @Test
    public void testHash() {
        AppUser user1 = new AppUser(new NetId("netId123"), new HashedPassword("pass123"), EmployeeType.EMPLOYEE);
        int hash = user1.hashCode();
        assertEquals(hash, Objects.hash(user1.getNetId()));
    }


}