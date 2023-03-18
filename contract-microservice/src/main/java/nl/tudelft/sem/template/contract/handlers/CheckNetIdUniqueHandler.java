package nl.tudelft.sem.template.contract.handlers;

import java.util.Map;
import nl.tudelft.sem.template.contract.exceptions.ContractHandlerException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

public class CheckNetIdUniqueHandler extends BaseHandler {
    private transient RestTemplate restTemplate = new RestTemplate();

    @Override
    public boolean handle(Map<String, Object> data) throws ContractHandlerException {
        System.out.println(data.toString());
        String checkNetIdUniqueUrl = "http://localhost:8083/authentication/users/checkNetIdUnique/" + data.get("candidateNetId");
        String jwt = data.get("jwt").toString();
        HttpHeaders header = new HttpHeaders();
        header.set("Authorization", jwt);
        try {
            ResponseEntity<Boolean> responseEntity = restTemplate.exchange(
                checkNetIdUniqueUrl,
                HttpMethod.GET,
                new HttpEntity<>(header),
                Boolean.class);
            if (responseEntity.getBody()) {
                return checkNextHandler(data);
            } else {
                throw new ContractHandlerException("NetId already in use");
            }
        } catch (HttpClientErrorException errorCode) {
            throw (new ContractHandlerException("HTTP Request failed" + errorCode.getResponseBodyAsString()));
        }
    }
}
