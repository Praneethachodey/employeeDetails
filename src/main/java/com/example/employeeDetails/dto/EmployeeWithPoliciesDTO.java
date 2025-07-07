package com.example.employeeDetails.dto;

import com.example.employeeDetails.Entity.AuditLog;
import com.example.employeeDetails.Entity.Employee;
import com.example.employeeDetails.Entity.HrPolicy;
import com.example.employeeDetails.Entity.SecurityContext;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class EmployeeWithPoliciesDTO {

    private Employee employee;
    private List<HrPolicy> policies;
    private LocalDateTime responseTimestamp = LocalDateTime.now();  // Default to current timestamp
    private boolean cached;

    // Complex nested response structure
    private SecurityValidationResult securityValidation;
    private BusinessRuleValidation businessRuleValidation;
    private AuditTrail auditTrail;
    private CrossReferenceData crossReferences;
    private ResponseMetadata metadata;
    private List<ValidationError> validationErrors;
    private Map<String, Object> dynamicFields = new HashMap<>();  // Initialize the dynamic fields map

    @Data
    public static class SecurityValidationResult {
        private boolean isAuthorized;
        private String securityLevel;
        private List<String> permissions;
        private SecurityContext context;
        private Map<String, Boolean> accessRights;
        private String validationChain;
    }

    @Data
    public static class BusinessRuleValidation {
        private boolean isCompliant;
        private List<String> complianceChecks;
        private Map<String, Object> businessRules;
        private String departmentValidation;
        private String policyCompliance;
        private List<String> warnings;
        private Map<String, Boolean> ruleResults;
    }

    @Data
    public static class AuditTrail {
        private List<AuditLog> auditLogs;
        private String processingChain;
        private Map<String, LocalDateTime> timestamps;
        private List<String> operations;
        private String transactionId;
        private Map<String, String> contextData;
    }

    @Data
    public static class CrossReferenceData {
        private Map<String, Employee> relatedEmployees;
        private Map<String, List<HrPolicy>> departmentPolicies;
        private Map<String, Object> externalData;
        private List<String> dependencies;
        private Map<String, String> references;
    }

    @Data
    public static class ResponseMetadata {
        private String version;
        private String source;
        private LocalDateTime generatedAt;
        private String processingTime;
        private Map<String, Object> configuration;
        private List<String> transformations;
    }

    @Data
    public static class ValidationError {
        private String field;
        private String message;
        private String severity;
        private String source;
    }

    // Helper methods for complex operations
    public void addDynamicField(String key, Object value) {
        this.dynamicFields.put(key, value);
    }

    public void addValidationError(String field, String message, String severity, String source) {
        if (this.validationErrors == null) {
            this.validationErrors = new java.util.ArrayList<>();
        }
        ValidationError error = new ValidationError();
        error.setField(field);
        error.setMessage(message);
        error.setSeverity(severity);
        error.setSource(source);
        this.validationErrors.add(error);
    }

    public void addTransformation(String transformation) {
        if (this.metadata == null) {
            this.metadata = new ResponseMetadata();
        }
        if (this.metadata.getTransformations() == null) {
            this.metadata.setTransformations(new java.util.ArrayList<>());
        }
        this.metadata.getTransformations().add(transformation);
    }
}
