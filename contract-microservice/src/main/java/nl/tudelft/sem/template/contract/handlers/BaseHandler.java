package nl.tudelft.sem.template.contract.handlers;

import java.util.Map;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;

public abstract class BaseHandler implements Handler {

    private transient Handler nextHandlerMethod;

    public Handler nextHandler(Handler h) {
        this.nextHandlerMethod = h;
        return h;
    }

    /**
     * Checks next handler.
     *
     * @param data Map containing the required to run the handler
     * @return Boolean whether the handler succeeded
     * @throws ContractHandlerException throws exception when handler fails
     */
    public boolean checkNextHandler(Map<String, Object> data) throws ContractHandlerException {
        if (nextHandlerMethod == null) {
            return true;
        }
        return nextHandlerMethod.handle(data);
    }


}
