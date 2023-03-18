package nl.tudelft.sem.template.contract.handlers;

import java.util.Map;
import java.util.Optional;
import nl.tudelft.sem.template.contract.domain.Contract;
import nl.tudelft.sem.template.contract.domain.ContractRepository;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;


public class ValidateContractHandler extends BaseHandler {

    private final transient ContractRepository contractRepository;

    public ValidateContractHandler(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Override
    public boolean handle(Map<String, Object> data) throws ContractHandlerException {
        Contract contract = (Contract) data.get("contract");
        if (contract == null) {
            throw new ContractHandlerException("Contract is missing in request body");
        }
        if (contract.getId() == null) {
            // Proposal is for a new contract
            if (contract.getCandidateNetId() == null || contract.getCandidateNetId().isEmpty()) {
                // No Candidate Net ID
                throw new ContractHandlerException("No candidate ID");
            }
        } else {
            // Get existing contract from repository
            Optional<Contract> existingContractOptional = contractRepository.findById(contract.getId());
            Contract existingContract = existingContractOptional.orElseGet(Contract::new);

            // Check if contract is of type Draft
            if (!existingContract.getState().equals(Contract.State.DRAFT)) {
                throw new ContractHandlerException("Contract must be of type DRAFT to propose");
            }
        }


        try {
            Contract.isValidContract(contract);
        } catch (Exception e) {
            throw new ContractHandlerException("This contract is not valid");
        }


        return checkNextHandler(data);

    }
}
