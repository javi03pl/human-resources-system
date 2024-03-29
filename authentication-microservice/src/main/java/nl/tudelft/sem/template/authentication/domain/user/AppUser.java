package nl.tudelft.sem.template.authentication.domain.user;

import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.NoArgsConstructor;
import nl.tudelft.sem.template.authentication.domain.HasEvents;


/**
 * A DDD entity representing an application user in our domain.
 */
@Entity
@Table(name = "users")
@NoArgsConstructor
public class AppUser extends HasEvents {
    /**
     * Create new application user.
     *
     * @param netId The NetId for the new user
     * @param password The password for the new user
     */
    public AppUser(NetId netId, HashedPassword password, EmployeeType role) {
        this.netId = netId;
        this.password = password;
        this.role = role;
        this.recordThat(new UserWasCreatedEvent(netId));
    }
    /**
     * Identifier for the application user.
     */

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private int id;

    @Column(name = "net_id", nullable = false, unique = true)
    @Convert(converter = NetIdAttributeConverter.class)
    private NetId netId;

    @Column(name = "password_hash", nullable = false)
    @Convert(converter = HashedPasswordAttributeConverter.class)
    private HashedPassword password;

    @Column(name = "role", nullable = false)
    private EmployeeType role;


    public void changePassword(HashedPassword password) {
        this.password = password;
        this.recordThat(new PasswordWasChangedEvent(this));
    }

    public int getId() {
        return id;
    }

    public NetId getNetId() {
        return netId;
    }

    public HashedPassword getPassword() {
        return password;
    }

    public EmployeeType getRole() {
        return role;
    }

    /**
     * Equality is only based on the identifier.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AppUser appUser = (AppUser) o;
        return id == (appUser.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(netId);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNetId(NetId netId) {
        this.netId = netId;
    }

    public void setPassword(HashedPassword password) {
        this.password = password;
    }

    public void setRole(EmployeeType role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "AppUser{"
                + "id=" + id
                + ", netId=" + netId
                + ", password=" + password
                + ", role=" + role
                + '}';
    }
}
