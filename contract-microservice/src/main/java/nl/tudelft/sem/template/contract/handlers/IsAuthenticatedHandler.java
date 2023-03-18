package nl.tudelft.sem.template.contract.handlers;

import java.util.Map;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;
import org.springframework.security.core.context.SecurityContextHolder;

public class IsAuthenticatedHandler extends BaseHandler {
    /**
     * Handler for validating a jwt token.
     *
     * @param data data containing the token
     * @return true if token is valid
     * @throws ContractHandlerException if token is invalid or not in data
     */
    @Override
    public boolean handle(Map<String, Object> data) throws ContractHandlerException {
        if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
            return checkNextHandler(data);
        } else {
            throw new ContractHandlerException("JWT Token invalid");
        }
    }
}
