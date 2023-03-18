package nl.tudelft.sem.template.contract.handlers;

import java.util.Map;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;

public interface Handler {


    Handler nextHandler(Handler handler);

    boolean handle(Map<String, Object> data) throws ContractHandlerException;
}
