package nl.tudelft.sem.template.contract.handlers;

import java.util.Locale;
import java.util.Map;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;

public class ValidateCandidateDetailsHandler extends BaseHandler {

    private static final String CANDIDATE_NETID_FIELD = "candidateNetId";
    
    @Override
    public boolean handle(Map<String, Object> data) throws ContractHandlerException {
        if (data.get(CANDIDATE_NETID_FIELD) == null || data.get("candidatePassword") == null) {
            throw new ContractHandlerException("NetId or password is null");
        } else if (data.get(CANDIDATE_NETID_FIELD).toString().length() > 50
            || data.get("candidatePassword").toString().length() > 50
            || data.get(CANDIDATE_NETID_FIELD).toString().length() < 3
            || data.get("candidatePassword").toString().length() < 3) {
            throw new ContractHandlerException("NetId and password must be between 3 and 50 chars long");
        } else if (data.get(CANDIDATE_NETID_FIELD).toString().toLowerCase(new Locale("en", "EN"))
                .contains("hr")) {
            throw new ContractHandlerException("NetId cannot be HR");
        } else {
            return checkNextHandler(data);
        }
    }
}

