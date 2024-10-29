package com.reliaquest.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.reliaquest.api.model.Employee;
import com.reliaquest.api.model.EmployeeInput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface IEmployeeService {
    List<Employee> getAllEmployees() throws JsonProcessingException;
    List<Employee> getEmployeesByNameSearch(String nameFragment) throws JsonProcessingException;
    Employee getEmployeeById(String id) throws JsonProcessingException;
    int getHighestSalaryOfEmployees() throws JsonProcessingException;
    List<Employee> getTop10HighestEarningEmployeeNames() throws JsonProcessingException;
    Employee createEmployee(EmployeeInput employee);
    String deleteEmployeeById(String id);
}
