package nl.tudelft.sem.template.authentication.domain.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * A DDD repository for querying and persisting user aggregate roots.
 */
@Repository
public interface UserRepository extends JpaRepository<AppUser, Integer> {
    /**
     * Find user by NetID.
     */
    Optional<AppUser> findByNetId(NetId netId);

    /**
     * Check if an existing user already uses a NetID.
     */
    boolean existsByNetId(NetId netId);
}
