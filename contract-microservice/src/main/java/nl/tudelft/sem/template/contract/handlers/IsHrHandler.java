package nl.tudelft.sem.template.contract.handlers;

import java.util.Map;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;

public class IsHrHandler extends BaseHandler {

    @Override
    public boolean handle(Map<String, Object> data) throws ContractHandlerException {
        Object role = data.get("role");
        if (role != null && role.getClass().equals(String.class) && data.get("role").toString().contains("HR")) {
            return checkNextHandler(data);
        } else {
            throw new ContractHandlerException("User is not HR");
        }
    }
}
