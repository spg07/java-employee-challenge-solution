package com.reliaquest.api.model;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeResponseDeserializer extends JsonDeserializer<EmployeeResponse> {
    //Custom deserializer for handling response for single as well as multiple employees from external api.
    @Override
    public EmployeeResponse deserialize(JsonParser jp, DeserializationContext context)
            throws IOException, JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = jp.getCodec().readTree(jp);

        EmployeeResponse response = new EmployeeResponse();
        response.setStatus(node.get("status").asText());

        JsonNode dataNode = node.get("data");
        if (dataNode.isArray()) {
            List<Employee> employees = new ArrayList<>();
            for (JsonNode employeeNode : dataNode) {
                Employee employee = mapper.treeToValue(employeeNode, Employee.class);
                employees.add(employee);
            }
            response.setEmployees(employees);
        } else {
            Employee employee = mapper.treeToValue(dataNode, Employee.class);
            response.setData(employee);
        }

        return response;
    }
}
