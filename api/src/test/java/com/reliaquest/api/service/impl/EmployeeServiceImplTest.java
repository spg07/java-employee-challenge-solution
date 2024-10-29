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
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTest {

    private EmployeeServiceImpl employeeService;

    private MockWebServer mockWebServer;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {

        mockWebServer = new MockWebServer();
        WebClient webClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();
        employeeService = new EmployeeServiceImpl(webClient);
    }

    @AfterEach
    void tearDown() throws IOException{
        mockWebServer.shutdown();
    }

    @Test
    void testGetAllEmployees() {
        String response = "{\"data\":[{\"id\":\"9250abc9-d7ef-414b-8c85-168a91e0f8c8\",\"employee_name\":\"Caroline Yundt V\",\"employee_salary\":42711,\"employee_age\":51,\"employee_title\":\"Customer Government Developer\",\"employee_email\":\"teejay_thompson@company.com\"},{\"id\":\"b328f159-1841-4411-8032-78c8be1ea190\",\"employee_name\":\"Ariel Larkin\",\"employee_salary\":244740,\"employee_age\":46,\"employee_title\":\"Construction Officer\",\"employee_email\":\"domainer@company.com\"},{\"id\":\"3d6728bc-0a0a-490b-8739-c6ff4ad33072\",\"employee_name\":\"Miss Johna Farrell\",\"employee_salary\":347496,\"employee_age\":18,\"employee_title\":\"Administration Coordinator\",\"employee_email\":\"northernlightz@company.com\"},{\"id\":\"62272440-1381-45e5-9ebd-52edbb7357dc\",\"employee_name\":\"Cordie Cole\",\"employee_salary\":459227,\"employee_age\":69,\"employee_title\":\"Future Designer\",\"employee_email\":\"solarbreeze@company.com\"},{\"id\":\"ab2d1a0b-57b0-4bed-bbc8-c8ac4808f55d\",\"employee_name\":\"Delbert Olson\",\"employee_salary\":184973,\"employee_age\":29,\"employee_title\":\"Government Producer\",\"employee_email\":\"fat_kyle@company.com\"},{\"id\":\"b7cf341e-4b1b-4b53-abe4-0c2a2ed502ec\",\"employee_name\":\"Cory Rice\",\"employee_salary\":317790,\"employee_age\":50,\"employee_title\":\"Forward Design Architect\",\"employee_email\":\"fintone@company.com\"},{\"id\":\"fd9f73e1-f3bb-4ced-ae4a-d5793078bb23\",\"employee_name\":\"Enoch Thiel\",\"employee_salary\":76845,\"employee_age\":45,\"employee_title\":\"Government Director\",\"employee_email\":\"andalax@company.com\"},{\"id\":\"514d3589-500a-41de-946f-bb6d77d06dab\",\"employee_name\":\"Ezequiel Pfannerstill\",\"employee_salary\":172488,\"employee_age\":66,\"employee_title\":\"Human Coordinator\",\"employee_email\":\"northernlightz@company.com\"},{\"id\":\"f150bd7d-bb5f-4293-b926-68d1d0de1869\",\"employee_name\":\"Phil Beier\",\"employee_salary\":275169,\"employee_age\":54,\"employee_title\":\"Internal Mining Analyst\",\"employee_email\":\"imdahdude@company.com\"},{\"id\":\"d1864937-d414-4b16-9f30-5bbb51525f90\",\"employee_name\":\"Shizue Ferry\",\"employee_salary\":394944,\"employee_age\":33,\"employee_title\":\"Chief Legal Designer\",\"employee_email\":\"opela@company.com\"}],\"status\":\"Successfully processed request.\"}";

        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        List<Employee> employees = employeeService.getAllEmployees();
        assertNotNull(employees);
        assertEquals("Caroline Yundt V", employees.get(0).getName());
        assertEquals(10, employees.size());
    }

    @Test
    void testGetAllEmployeesEmptyResponse() throws JsonProcessingException {
        EmployeeResponse emptyResponse = new EmployeeResponse();
        emptyResponse.setEmployees(List.of());
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(emptyResponse))
                        .addHeader("Content-Type", "application/json"));

        assertThrows(EmployeeServiceException.class, () -> employeeService.getAllEmployees());
    }

    @Test
    void testGetAllEmployeesTooManyRequests() {
        for(int i =0; i < 5; i++){
            mockWebServer.enqueue(new MockResponse().setResponseCode(429));
        }

        assertThrows(EmployeeServiceException.class, () -> employeeService.getAllEmployees());
    }

    @Test
    void testGetAllEmployeesServerError() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"message\":\"Internal server error\"}")
                .addHeader("Content-Type", "application/json"));

        assertThrows(EmployeeServiceException.class, () -> employeeService.getAllEmployees());
    }

    @Test
    void testGetEmployeeByNameSearchSuccess() {
        String employeeName = "Sharvil";

        String response = "{\"data\":[{\"id\":\"9250abc9-d7ef-414b-8c85-168a91e0f8c8\",\"employee_name\":\"Sharvil Ghate\",\"employee_salary\":42711,\"employee_age\":51,\"employee_title\":\"Customer Government Developer\",\"employee_email\":\"teejay_thompson@company.com\"},{\"id\":\"b328f159-1841-4411-8032-78c8be1ea190\",\"employee_name\":\"Sharvil G\",\"employee_salary\":244740,\"employee_age\":46,\"employee_title\":\"Construction Officer\",\"employee_email\":\"domainer@company.com\"},{\"id\":\"3d6728bc-0a0a-490b-8739-c6ff4ad33072\",\"employee_name\":\"S Ghate\",\"employee_salary\":347496,\"employee_age\":18,\"employee_title\":\"Administration Coordinator\",\"employee_email\":\"northernlightz@company.com\"},{\"id\":\"62272440-1381-45e5-9ebd-52edbb7357dc\",\"employee_name\":\"Cordie Cole\",\"employee_salary\":459227,\"employee_age\":69,\"employee_title\":\"Future Designer\",\"employee_email\":\"solarbreeze@company.com\"},{\"id\":\"ab2d1a0b-57b0-4bed-bbc8-c8ac4808f55d\",\"employee_name\":\"Delbert Olson\",\"employee_salary\":184973,\"employee_age\":29,\"employee_title\":\"Government Producer\",\"employee_email\":\"fat_kyle@company.com\"},{\"id\":\"b7cf341e-4b1b-4b53-abe4-0c2a2ed502ec\",\"employee_name\":\"Cory Rice\",\"employee_salary\":317790,\"employee_age\":50,\"employee_title\":\"Forward Design Architect\",\"employee_email\":\"fintone@company.com\"},{\"id\":\"fd9f73e1-f3bb-4ced-ae4a-d5793078bb23\",\"employee_name\":\"Enoch Thiel\",\"employee_salary\":76845,\"employee_age\":45,\"employee_title\":\"Government Director\",\"employee_email\":\"andalax@company.com\"},{\"id\":\"514d3589-500a-41de-946f-bb6d77d06dab\",\"employee_name\":\"Ezequiel Pfannerstill\",\"employee_salary\":172488,\"employee_age\":66,\"employee_title\":\"Human Coordinator\",\"employee_email\":\"northernlightz@company.com\"},{\"id\":\"f150bd7d-bb5f-4293-b926-68d1d0de1869\",\"employee_name\":\"Phil Beier\",\"employee_salary\":275169,\"employee_age\":54,\"employee_title\":\"Internal Mining Analyst\",\"employee_email\":\"imdahdude@company.com\"},{\"id\":\"d1864937-d414-4b16-9f30-5bbb51525f90\",\"employee_name\":\"Shizue Ferry\",\"employee_salary\":394944,\"employee_age\":33,\"employee_title\":\"Chief Legal Designer\",\"employee_email\":\"opela@company.com\"}],\"status\":\"Successfully processed request.\"}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        List<Employee> employeeList = employeeService.getEmployeesByNameSearch(employeeName);

        assertNotNull(employeeList);
        assertEquals(employeeList.size(), 2);
        assertEquals(employeeList.get(0).getTitle(), "Customer Government Developer");
    }

    @Test
    void testGetEmployeeByIdSuccess() {
        String id = "ae51e4a0-d682-48b8-9451-3c9fad1e04af";
        String response = "{\"data\":{\"id\":\"ae51e4a0-d682-48b8-9451-3c9fad1e04af\",\"employee_name\":\"Louanne Nader V\",\"employee_salary\":385456,\"employee_age\":44,\"employee_title\":\"Customer Orchestrator\",\"employee_email\":\"konklux@company.com\"},\"status\":\"Successfully processed request.\"}";
        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        Employee employee = employeeService.getEmployeeById(id);

        assertNotNull(employee);
        assertEquals("Louanne Nader V", employee.getName());
        assertEquals(44, employee.getAge());
    }

    @Test
    void testGetEmployeeByIdNotFound() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"message\":\"Employee not found\"}")
                .addHeader("Content-Type", "application/json"));

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.getEmployeeById("bae5b1c2-7b35-4cf3-a1a6-39e13f5"));
    }

    @Test
    void testGetTop10HighestEarningEmployeeNamesSuccess() throws Exception {
        String response = "{\"data\":[{\"id\":\"9250abc9-d7ef-414b-8c85-168a91e0f8c8\",\"employee_name\":\"Caroline Yundt V\",\"employee_salary\":42711,\"employee_age\":51,\"employee_title\":\"Customer Government Developer\",\"employee_email\":\"teejay_thompson@company.com\"},{\"id\":\"b328f159-1841-4411-8032-78c8be1ea190\",\"employee_name\":\"Ariel Larkin\",\"employee_salary\":244740,\"employee_age\":46,\"employee_title\":\"Construction Officer\",\"employee_email\":\"domainer@company.com\"},{\"id\":\"3d6728bc-0a0a-490b-8739-c6ff4ad33072\",\"employee_name\":\"Miss Johna Farrell\",\"employee_salary\":347496,\"employee_age\":18,\"employee_title\":\"Administration Coordinator\",\"employee_email\":\"northernlightz@company.com\"},{\"id\":\"62272440-1381-45e5-9ebd-52edbb7357dc\",\"employee_name\":\"Cordie Cole\",\"employee_salary\":459227,\"employee_age\":69,\"employee_title\":\"Future Designer\",\"employee_email\":\"solarbreeze@company.com\"},{\"id\":\"ab2d1a0b-57b0-4bed-bbc8-c8ac4808f55d\",\"employee_name\":\"Delbert Olson\",\"employee_salary\":184973,\"employee_age\":29,\"employee_title\":\"Government Producer\",\"employee_email\":\"fat_kyle@company.com\"},{\"id\":\"b7cf341e-4b1b-4b53-abe4-0c2a2ed502ec\",\"employee_name\":\"Cory Rice\",\"employee_salary\":317790,\"employee_age\":50,\"employee_title\":\"Forward Design Architect\",\"employee_email\":\"fintone@company.com\"},{\"id\":\"fd9f73e1-f3bb-4ced-ae4a-d5793078bb23\",\"employee_name\":\"Enoch Thiel\",\"employee_salary\":76845,\"employee_age\":45,\"employee_title\":\"Government Director\",\"employee_email\":\"andalax@company.com\"},{\"id\":\"514d3589-500a-41de-946f-bb6d77d06dab\",\"employee_name\":\"Ezequiel Pfannerstill\",\"employee_salary\":172488,\"employee_age\":66,\"employee_title\":\"Human Coordinator\",\"employee_email\":\"northernlightz@company.com\"},{\"id\":\"f150bd7d-bb5f-4293-b926-68d1d0de1869\",\"employee_name\":\"Phil Beier\",\"employee_salary\":275169,\"employee_age\":54,\"employee_title\":\"Internal Mining Analyst\",\"employee_email\":\"imdahdude@company.com\"},{\"id\":\"d1864937-d414-4b16-9f30-5bbb51525f90\",\"employee_name\":\"Shizue Ferry\",\"employee_salary\":394944,\"employee_age\":33,\"employee_title\":\"Chief Legal Designer\",\"employee_email\":\"opela@company.com\"}],\"status\":\"Successfully processed request.\"}";

        mockWebServer.enqueue(new MockResponse()
                .setBody(response)
                .addHeader("Content-Type", "application/json"));

        List<Employee> topEarners = employeeService.getTop10HighestEarningEmployeeNames();

        assertEquals(10, topEarners.size());
        assertEquals("Cordie Cole", topEarners.get(0).getName());
    }

    @Test
    void testCreateEmployeeSuccess() {
        EmployeeInput employeeInput = new EmployeeInput("Sharvil Ghate", 100000, 26, "Software Developer");

        String employeeResponseString = "{\"data\":[{\"id\":\"cdf4bf72-7219-4b4f-86fc-d36915a7a20c\",\"employee_name\":\"Sharvil Ghate\",\"employee_salary\":100000,\"employee_age\":26,\"employee_title\":\"Software Developer\",\"employee_email\":\"testemail@email.com\"},{\"id\":\"b328f159-1841-4411-8032-78c8be1ea190\",\"employee_name\":\"Sharvil G\",\"employee_salary\":244740,\"employee_age\":46,\"employee_title\":\"Construction Officer\",\"employee_email\":\"domainer@company.com\"},{\"id\":\"3d6728bc-0a0a-490b-8739-c6ff4ad33072\",\"employee_name\":\"S Ghate\",\"employee_salary\":347496,\"employee_age\":18,\"employee_title\":\"Administration Coordinator\",\"employee_email\":\"northernlightz@company.com\"},{\"id\":\"62272440-1381-45e5-9ebd-52edbb7357dc\",\"employee_name\":\"Cordie Cole\",\"employee_salary\":459227,\"employee_age\":69,\"employee_title\":\"Future Designer\",\"employee_email\":\"solarbreeze@company.com\"},{\"id\":\"ab2d1a0b-57b0-4bed-bbc8-c8ac4808f55d\",\"employee_name\":\"Delbert Olson\",\"employee_salary\":184973,\"employee_age\":29,\"employee_title\":\"Government Producer\",\"employee_email\":\"fat_kyle@company.com\"},{\"id\":\"b7cf341e-4b1b-4b53-abe4-0c2a2ed502ec\",\"employee_name\":\"Cory Rice\",\"employee_salary\":317790,\"employee_age\":50,\"employee_title\":\"Forward Design Architect\",\"employee_email\":\"fintone@company.com\"},{\"id\":\"fd9f73e1-f3bb-4ced-ae4a-d5793078bb23\",\"employee_name\":\"Enoch Thiel\",\"employee_salary\":76845,\"employee_age\":45,\"employee_title\":\"Government Director\",\"employee_email\":\"andalax@company.com\"},{\"id\":\"514d3589-500a-41de-946f-bb6d77d06dab\",\"employee_name\":\"Ezequiel Pfannerstill\",\"employee_salary\":172488,\"employee_age\":66,\"employee_title\":\"Human Coordinator\",\"employee_email\":\"northernlightz@company.com\"},{\"id\":\"f150bd7d-bb5f-4293-b926-68d1d0de1869\",\"employee_name\":\"Phil Beier\",\"employee_salary\":275169,\"employee_age\":54,\"employee_title\":\"Internal Mining Analyst\",\"employee_email\":\"imdahdude@company.com\"},{\"id\":\"d1864937-d414-4b16-9f30-5bbb51525f90\",\"employee_name\":\"Shizue Ferry\",\"employee_salary\":394944,\"employee_age\":33,\"employee_title\":\"Chief Legal Designer\",\"employee_email\":\"opela@company.com\"}],\"status\":\"Successfully processed request.\"}";
//        Employee employeeResponse = new Employee("cdf4bf72-7219-4b4f-86fc-d36915a7a20c", "Sharvil Ghate", 100000, 26, "Software Developer", "testemail@email.com");
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(HttpStatus.CREATED.value()));

        mockWebServer.enqueue(new MockResponse()
                .setBody(employeeResponseString)
                .setHeader("Content-Type", "application/json"));

        Employee employee = employeeService.createEmployee(employeeInput);
        assertEquals(employee.getId(), "cdf4bf72-7219-4b4f-86fc-d36915a7a20c");
        assertEquals(employee.getEmail(), "testemail@email.com");
    }

    @Test
    void testCreateEmployeeRetryAndFail() {
        EmployeeInput employeeInput = new EmployeeInput("Sharvil Ghate", 100000, 26, "Software Developer");

        for (int i = 0; i < 6; i++) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.TOO_MANY_REQUESTS.value()));
        }

        EmployeeServiceException exception = assertThrows(EmployeeServiceException.class, () -> {
            employeeService.createEmployee(employeeInput);
        });

        assertTrue(exception.getMessage().contains("create employee : Service Unavailable"));
    }

    @Test
    void testCreateEmployeeServerErrorAndFail() {
        EmployeeInput employeeInput = new EmployeeInput("Sharvil Ghate", 100000, 26, "Software Developer");

        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        EmployeeCreationException exception = assertThrows(EmployeeCreationException.class, () -> {
            employeeService.createEmployee(employeeInput);
        });

        assertEquals("Failed to create employee", exception.getMessage());
    }

    @Test
    void testDeleteEmployeeSuccess() {
        String id = "ae51e4a0-d682-48b8-9451-3c9fad1e04af";
        String employeeResponseString = "{\"data\":{\"id\":\"ae51e4a0-d682-48b8-9451-3c9fad1e04af\",\"employee_name\":\"Louanne Nader V\",\"employee_salary\":385456,\"employee_age\":44,\"employee_title\":\"Customer Orchestrator\",\"employee_email\":\"konklux@company.com\"},\"status\":\"Successfully processed request.\"}";

        mockWebServer.enqueue(new MockResponse().setBody(employeeResponseString).addHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.NO_CONTENT.value()));

        String result = employeeService.deleteEmployeeById(id);

        assertEquals(result, "Successfully deleted employee: Louanne Nader V");
    }

    @Test
    void testDeleteEmployeeRetryAndFail() {
        String id = "ae51e4a0-d682-48b8-9451-3c9fad1e04af";

        for (int i = 0; i < 6; i++) {
            mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.TOO_MANY_REQUESTS.value()));
        }

        EmployeeServiceException exception = assertThrows(EmployeeServiceException.class, () -> {
            employeeService.deleteEmployeeById(id);
        });

        assertTrue(exception.getMessage().contains("delete employee by name : Error in getting employees, please try again after sometime"));
    }

    @Test
    void testDeleteEmployeeServerErrorAndFail() {
        String id = "ae51e4a0-d682-48b8-9451-3c9fad1e04af";
        String employeeResponseString = "{\"data\":{\"id\":\"ae51e4a0-d682-48b8-9451-3c9fad1e04af\",\"employee_name\":\"Louanne Nader V\",\"employee_salary\":385456,\"employee_age\":44,\"employee_title\":\"Customer Orchestrator\",\"employee_email\":\"konklux@company.com\"},\"status\":\"Successfully processed request.\"}";


        mockWebServer.enqueue(new MockResponse().setBody(employeeResponseString).setHeader("Content-Type", "application/json"));
        mockWebServer.enqueue(new MockResponse().setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()));

        EmployeeDeletionException exception = assertThrows(EmployeeDeletionException.class, () -> {
            employeeService.deleteEmployeeById(id);
        });

        assertEquals("Failed to delete employee", exception.getMessage());
    }
}

