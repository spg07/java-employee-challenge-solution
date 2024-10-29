package com.reliaquest.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private String id;

    @JsonProperty("employee_name")
    private String name;

    @JsonProperty("employee_salary")
    private Integer salary;

    @JsonProperty("employee_age")
    private Integer age;

    @JsonProperty("employee_title")
    private String title;

    @JsonProperty("employee_email")
    private String email;

}
