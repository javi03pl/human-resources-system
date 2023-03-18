package nl.tudelft.sem.template.authentication.service;

import java.util.NoSuchElementException;
import java.util.Optional;
import nl.tudelft.sem.template.authentication.domain.user.AppUser;
import nl.tudelft.sem.template.authentication.domain.user.EmployeeType;
import nl.tudelft.sem.template.authentication.domain.user.NetId;
import nl.tudelft.sem.template.authentication.domain.user.UserRepository;
import nl.tudelft.sem.template.authentication.exceptions.RoleAlreadySetException;
import org.springframework.stereotype.Service;

@Service
public class AppUserService {
    private final transient UserRepository userRepository;

    public AppUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addUser(AppUser user) {
        userRepository.save(user);
    }

    /**
     * Creates a candidate.
     *
     * @param user The candidate to be created.
     */
    public void createCandidate(AppUser user) {
        user.setRole(EmployeeType.CANDIDATE);
        userRepository.save(user);
        System.out.print("Account created.\n");
    }

    public AppUser getUserById(int userId) {
        return userRepository.getOne(userId);
    }

    /**
     * Gets a user's details by netId.
     *
     * @param netId netId
     *
     * @return AppUser if found
     * @throws NoSuchElementException if the netId does not exist
     */
    public AppUser getUserByNetId(NetId netId) throws NoSuchElementException {
        Optional<AppUser> foundUser = userRepository.findByNetId(netId);
        if (foundUser.isPresent()) {
            return foundUser.get();
        } else {
            throw new NoSuchElementException();
        }
    }

    public EmployeeType getRoleById(int userId) {
        return userRepository.getOne(userId).getRole();
    }

    /**
     * Updates a user's details.
     *
     * @param user the new user
     *
     * @return returns updated user
     */
    public AppUser updateUser(AppUser user) {
        Optional<AppUser> existing = userRepository.findByNetId(user.getNetId());
        if (existing.isPresent()) {
            AppUser updatedEmployee = existing.get();
            updatedEmployee.setRole(user.getRole());
            //updatedEmployee.setContract(employee.getContract());
            return userRepository.save(updatedEmployee);
        }
        return null;
    }

    /**
     * Sets a given role to a user.
     *
     * @param user The user that will be promoted
     * @param role The new role
     * @return Returns the new user
     * @throws RoleAlreadySetException if role is already set
     */
    public AppUser changeRole(AppUser user, EmployeeType role) throws RoleAlreadySetException {
        Optional<AppUser> existing = userRepository.findByNetId(user.getNetId());
        if (existing.isPresent()) {
            AppUser updatedEmployee = existing.get();
            if (updatedEmployee.getRole() == role) {
                throw new RoleAlreadySetException();
            }
            updatedEmployee.setRole(role);
            return userRepository.save(updatedEmployee);
        }
        return null;
    }

    /**
     * Deletes user with userId.
     *
     * @param userId The user's userId
     *
     * @return Returns the deleted user
     */
    public AppUser deleteUser(int userId) {
        AppUser e = userRepository.getOne(userId);
        userRepository.deleteById(userId);
        return e;
    }

    public Boolean netIdExists(NetId netId) {
        return userRepository.existsByNetId(netId);
    }
}
