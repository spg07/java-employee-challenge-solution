package com.reliaquest.api.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
@JsonDeserialize(using = EmployeeResponseDeserializer.class)
public class EmployeeResponse {
    private Employee data; // For single employee
    private List<Employee> employees; // For list of employees
    private String status;
}
