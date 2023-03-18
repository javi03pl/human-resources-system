package nl.tudelft.sem.template.apigateway.controllers;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

/**
 * API Gateway controller.
 * This controller is responsible for routing requests to the correct service.
 *
 */
@RestController
public class ApiGatewayController {

    private final transient RestTemplate restTemplate = new RestTemplate();

    private String getServiceUri(String requestUri) {
        if (requestUri.equals("")) {
            requestUri = "/";
        }
        return requestUri;
    }

    /**
     * RequestMapping to forward Http request to correct service.
     *
     * @param request Http Request to API gateway
     * @param service Service to forward request to
     * @return ResponseEntity containing service response
     */
    @RequestMapping(value = {"/{service}/**"})
    public ResponseEntity<Object> service(HttpServletRequest request, @PathVariable String service) {
        // Map routes to service urls
        Map<String, String> serviceUrls = new HashMap<>();
        serviceUrls.put("/contracts", "http://localhost:8082");
        serviceUrls.put("/authentication", "http://localhost:8081");
        serviceUrls.put("/messages", "http://localhost:8088");

        StringBuilder params = new StringBuilder("?");
        Enumeration<String> parameters = request.getParameterNames();
        while (parameters.hasMoreElements()) {
            String paramName = parameters.nextElement();
            params.append(paramName + "=" + request.getParameter(paramName) + "&");
        }
        params.deleteCharAt(params.length() - 1);
        // Get service url
        if (!serviceUrls.containsKey("/" + service)) {
            return ResponseEntity.badRequest().body("Unknown service");
        }

        String body;
        Enumeration<String> headers;
        try {
            // Parse request body
            body = request.getReader().lines().collect(Collectors.joining("\n")); //NOPMD
            headers = request.getHeaderNames(); //NOPMD
            request.getReader().close();
        } catch (IOException ioException) {
            return ResponseEntity.badRequest().body("Error reading HTTP body");
        }

        // Create request headers for Http request to the concerned service
        HttpHeaders requestHeaders = new HttpHeaders();
        while (headers.hasMoreElements()) {
            String headerName = headers.nextElement();
            requestHeaders.set(headerName, request.getHeader(headerName));
        }
        HttpEntity<String> entity = new HttpEntity<>(body, requestHeaders);
        try {
            // Create Http request to service
            ResponseEntity<String> result =
                restTemplate.exchange(
                    serviceUrls.get("/" + service)
                        + getServiceUri(request.getRequestURI().replaceFirst("/" + service, "")
                        + params),
                    Objects.requireNonNull(HttpMethod.resolve(request.getMethod())),
                    entity, String.class);
            HttpHeaders responseHeaders = new HttpHeaders();
            result.getHeaders()
                .forEach((String name, List<String> values) -> responseHeaders.set(name, String.join(" ", values)));
            return ResponseEntity.status(result.getStatusCode()).headers(responseHeaders)
                .body(String.valueOf(result.getBody()));
        } catch (HttpStatusCodeException e) {
            // In case of error (non 200 status code) respond with the original service response.
            HttpHeaders responseHeaders = new HttpHeaders();
            MediaType contentType = Objects.requireNonNull(e.getResponseHeaders()).getContentType();
            if (contentType == null) {
                contentType = MediaType.APPLICATION_JSON;
            }
            responseHeaders.setContentType(contentType);
            return ResponseEntity.status(e.getRawStatusCode()).headers(responseHeaders)
                .body(e.getResponseBodyAsString());
        }


    }


}
