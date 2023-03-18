package nl.tudelft.sem.template.contract.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Integer> {


    Optional<Contract> findById(Long id);

    Optional<Contract> findByCandidateNetId(Long id);

    @Override
    <S extends Contract> S save(S entity);

    @Override
    void delete(Contract entity);

}
