package com.reliaquest.api.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.exception.EmployeeCreationException;
import com.reliaquest.api.exception.EmployeeDeletionException;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.exception.EmployeeServiceException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.model.EmployeeResponse;
import com.reliaquest.api.service.IEmployeeService;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import reactor.util.retry.RetryBackoffSpec;

import java.time.Duration;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeServiceImpl implements IEmployeeService {

    @Autowired
    private WebClient webClient;

    @Value("${employee.api.base-url}")
    private String baseUrl;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public EmployeeServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    //TODO: Implement caching mechanism
    public List<Employee> getAllEmployees() {
        log.info("Request to fetch all employees from {}", baseUrl);
        try {
            String response = fetchApiData(baseUrl);
            EmployeeResponse employeeResponse = parseResponse(response, EmployeeResponse.class);
            log.info("Successfully fetched employees. Total count: {}", employeeResponse.getEmployees().size());
            return employeeResponse.getEmployees();
        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("Received 429 Too Many Requests. Retrying...");
            throw e;
        } catch (Exception e) {
            throw handleException(e, "retrieve all employees");
        }
    }

    public List<Employee> getEmployeesByNameSearch(String name) {
        log.info("Searching for employees with name containing '{}'", name);
        try {
            List<Employee> matchingEmployees = getAllEmployees().stream()
                    .filter(employee -> employee.getName().contains(name))
                    .collect(Collectors.toList());

            if (matchingEmployees.isEmpty()) {
                log.warn("No employees found matching name '{}'", name);
                throw new EmployeeNotFoundException("No employees found with name containing: " + name);
            }
            log.info("Found {} employees with name containing: {}", matchingEmployees.size(), name);
            return matchingEmployees;
        } catch (EmployeeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e, "search employees by name");
        }
    }

    public Employee getEmployeeById(String id) {
        log.info("Request to fetch employee with ID: {}", id);
        try {
            String responseString = fetchApiData(baseUrl + "/" + id);
            EmployeeResponse employeeResponse = parseResponse(responseString, EmployeeResponse.class);
            if (employeeResponse.getData() == null) {
                throw new EmployeeNotFoundException("Employee not found with ID: " + id);
            }
            return employeeResponse.getData();
        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("Received 429 Too Many Requests. Retrying...");
            throw e;
        } catch (EmployeeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw handleException(e, "retrieve employee by ID");
        }
    }

    @Override
    public int getHighestSalaryOfEmployees() {
        log.info("Calculating highest salary among employees");
        try {
            return getAllEmployees().stream()
                    .map(Employee::getSalary)
                    .max(Comparator.naturalOrder())
                    .orElseThrow(() -> new EmployeeServiceException("No employees found to calculate highest salary"));
        } catch (Exception e) {
            throw handleException(e, "calculate highest employee salary");
        }
    }

    @Override
    public List<Employee> getTop10HighestEarningEmployeeNames() {
        log.info("Fetching top 10 highest-earning employees");
        try {
            return getAllEmployees().stream()
                    .sorted(Comparator.comparingInt(Employee::getSalary).reversed())
                    .limit(10)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw handleException(e, "retrieve top 10 highest-earning employees");
        }
    }

    public Employee createEmployee(EmployeeInput employeeInput) {
        log.info("Requesting to create a new employee.");
        try {
            postEmployeeData(employeeInput);
            Employee employee = getEmployeesByNameSearch(employeeInput.getName())
                    .stream()
                    .filter(e -> e.getName().equals(employeeInput.getName()))
                    .toList()
                    .get(0);
            log.info("Successfully posted employee: {}", employee.getName());
            return employee;
        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("Received 429 Too Many Requests. Retrying...");
            throw e;
        } catch (Exception e) {
            throw handleException(e, "create employee");
        }
    }

    public String deleteEmployeeById(String id) {
        log.info("Requesting to delete employee : {}", id);
        try {
            Employee employee = getEmployeeById(id);
            deleteEmployeeData(employee.getName());
            log.info("Successfully deleted employee: {}", employee.getName());
            return "Successfully deleted employee: " + employee.getName();
        } catch (WebClientResponseException.TooManyRequests e) {
            log.warn("Received 429 Too Many Requests. Retrying...");
            throw e;
        } catch (Exception e) {
            throw handleException(e, "delete employee by name");
        }
    }

    private String fetchApiData(String url) {
        RetryBackoffSpec retrySpecs = getRetrySpecs();
        log.info("Getting data from url: {}", url);
        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response -> Mono.error(new EmployeeServiceException("Error in getting employees")))
                .onStatus(HttpStatusCode::is4xxClientError, response -> Mono.error(new EmployeeNotFoundException("Error in getting employees, please try again after sometime")))
                .bodyToMono(String.class)
                .retryWhen(retrySpecs)
                .block();
    }

    private RetryBackoffSpec getRetrySpecs() {
        return Retry.fixedDelay(5, Duration.ofSeconds(3))
                .doBeforeRetry(beforeRetry -> {
                    log.error("Error while connecting to service, message: {}", beforeRetry.failure().getMessage());
                })
                .filter(throwable -> throwable instanceof WebClientResponseException.TooManyRequests)
                .onRetryExhaustedThrow((retrySpecs, retrySignal) -> handleException(new EmployeeServiceException(retrySignal.failure().getMessage()), "Service Unavailable"));
    }

    private void postEmployeeData(EmployeeInput employee) {
        RetryBackoffSpec retrySpecs = getRetrySpecs();
        webClient.post()
                .uri(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(employee), EmployeeInput.class)
                .retrieve()
                .onStatus( HttpStatusCode::is5xxServerError, response -> Mono.error(new EmployeeCreationException("Failed to create employee")))
                .toBodilessEntity()
                .retryWhen(retrySpecs)
                .block();
    }

    private void deleteEmployeeData(String name) {
        RetryBackoffSpec retrySpecs = getRetrySpecs();
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", name);
        webClient.method(HttpMethod.DELETE)
                .uri(baseUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(requestBody), HashMap.class)
                .retrieve()
                .onStatus( HttpStatusCode::is5xxServerError, response -> Mono.error(new EmployeeDeletionException("Failed to delete employee")))
                .toBodilessEntity()
                .retryWhen(retrySpecs)
                .block();
    }

    private <T> T parseResponse(String response, Class<T> clas) throws JsonProcessingException {
        return objectMapper.readValue(response, clas);
    }

    private RuntimeException handleException(Exception e, String operation) {
        log.error("Error during operation '{}': {}", operation, e.getMessage());
        if (e instanceof EmployeeCreationException) return (EmployeeCreationException) e;
        if (e instanceof EmployeeDeletionException) return (EmployeeDeletionException) e;
        return new EmployeeServiceException( operation + " : " + e.getMessage());
    }
}
