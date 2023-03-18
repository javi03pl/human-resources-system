package nl.tudelft.sem.template.contract.handlers;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import nl.tudelft.sem.template.contract.domain.Contract;
import nl.tudelft.sem.template.contract.domain.ContractRepository;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;


public class IsReviewingPartyHandler extends BaseHandler {

    private final transient ContractRepository contractRepository;

    public IsReviewingPartyHandler(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    @Override
    public boolean handle(Map<String, Object> data) throws ContractHandlerException {


        Contract contract = (Contract) data.get("contract");
        if (contract.getId() == null) {
            if (String.valueOf(data.get("role")).contains("HR")) {
                return checkNextHandler(data);
            } else {
                throw new ContractHandlerException("You are not allowed to create an initial proposal");
            }
        }



        Optional<Contract> existingContractOptional = contractRepository.findById(contract.getId());
        if (existingContractOptional.isEmpty()) {
            // Contract does not exist
            throw new ContractHandlerException("Contract does not exist");
        }

        Contract existingContract = existingContractOptional.get();

        String role = (String) data.get("role");


        if (existingContract.getReviewer().equals(Contract.Reviewer.EMPLOYER)
            && !role.contains("HR")) {
            // Contract is being reviewed by opposite party
            throw new ContractHandlerException("You are not the reviewing party of this contract");
        }
        if (existingContract.getReviewer().equals(Contract.Reviewer.CANDIDATE)
            && !Objects.equals(existingContract.getCandidateNetId(), data.get("netId"))) {
            // Contract is being reviewed by opposite party
            throw new ContractHandlerException("You are not the reviewing party of this contract");
        }
        return checkNextHandler(data);
    }
}
