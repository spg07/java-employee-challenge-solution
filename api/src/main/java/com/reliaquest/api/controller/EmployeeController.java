package com.reliaquest.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import com.reliaquest.api.service.IEmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employees")
@Tag(name = "Employee Management", description = "APIs for managing employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final IEmployeeService employeeService;

    @GetMapping
    @Operation(summary = "Get all employees", description = "Fetch all employees.")
    public List<Employee> getAllEmployees() throws JsonProcessingException {
        return employeeService.getAllEmployees();
    }


    @GetMapping("/search/{name}")
    @Operation(summary = "Search employees by name", description = "Fetch all employees whose name contains or matches the input string.")
    public List<Employee> getEmployeesByNameSearch(@PathVariable String name) throws JsonProcessingException {
        return employeeService.getEmployeesByNameSearch(name);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employee by ID", description = "Fetch an employee by their ID.")
    public Employee getEmployeeById(@PathVariable String id) throws JsonProcessingException {
        return employeeService.getEmployeeById(id);
    }


    @GetMapping("/highest-salary")
    @Operation(summary = "Get highest salary", description = "Fetch the highest salary among all employees.")
    public int getHighestSalaryOfEmployees() throws JsonProcessingException {
        return employeeService.getHighestSalaryOfEmployees();
    }

    @GetMapping("/top10")
    @Operation(summary = "Get top 10 highest earning employees", description = "Fetch the top 10 highest earning employees.")
    public List<Employee> getTop10HighestEarningEmployeeNames() throws JsonProcessingException {
        return employeeService.getTop10HighestEarningEmployeeNames();
    }

    @PostMapping
    @Operation(summary = "Create employee", description = "Create a new employee.")
    public ResponseEntity<Employee> createEmployee(@RequestBody EmployeeInput employeeInput) {
        Employee employee = employeeService.createEmployee(employeeInput);
        return ResponseEntity.ok(employee);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete employee by ID", description = "Delete an employee by their ID.")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        String response = employeeService.deleteEmployeeById(id);
        return ResponseEntity.ok(response);
    }

}
