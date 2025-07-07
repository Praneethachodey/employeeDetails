package com.example.employeeDetails.Service;

import com.example.employeeDetails.Entity.Employee;
import com.example.employeeDetails.Entity.HrPolicy;
import com.example.employeeDetails.Entity.SecurityContext;
import com.example.employeeDetails.Repository.EmployeeRepository;
import com.example.employeeDetails.dto.EmployeeWithPoliciesDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmployeeDetailsService
{

    private final HrPolicyService hrPolicyService;
    private final AuditService auditService;
    private final SecurityService securityService;
    private final EmployeeRepository employeeRepository;

    private final ConcurrentHashMap<String, EmployeeWithPoliciesDTO> responseCache = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> accessCounters = new ConcurrentHashMap<>();

    public EmployeeDetailsService(HrPolicyService hrPolicyService, AuditService auditService, SecurityService securityService, EmployeeRepository employeeRepository)
    {
        this.hrPolicyService = hrPolicyService;
        this.auditService = auditService;
        this.securityService = securityService;
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public EmployeeWithPoliciesDTO getEmployeeWithPolicies(String employeeId, String sessionId)
    {
        try
        {
            // Validate security context
            if (!securityService.validateSession(sessionId, "READ"))
            {
                auditService.logAuditEvent("SYSTEM", "UNAUTHORIZED_ACCESS", "Attempted to access employee: " + employeeId, "EmployeeDetailsService", sessionId);
                throw new SecurityException("Unauthorized access to employee details");
            }

            SecurityContext context = securityService.getSecurityContext(sessionId);
            if (context == null)
            {
                throw new SecurityException("Invalid security context");
            }

            // Check cache first
            String cacheKey = employeeId + "_" + context.getSecurityLevel();
            EmployeeWithPoliciesDTO cachedResult = responseCache.get(cacheKey);
            if (cachedResult != null)
            {
                accessCounters.merge(employeeId, 1, Integer::sum);
                return cachedResult;
            }

            // Get employee details
            Optional<Employee> optionalEmployee = employeeRepository.findById(employeeId);
            Employee employee = optionalEmployee.orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

            // Check security level for employee access
            if ("ADMIN".equals(employee.getSecurityLevel()) && !context.canAccessSensitiveData())
            {
                auditService.logAuditEvent(context.getUserId(), "SECURITY_VIOLATION", "Attempted to access employee with insufficient security level: " + employeeId, "EmployeeDetailsService", sessionId);
                throw new SecurityException("Insufficient security level for employee access");
            }

            // Get policies for employee's department
            List<HrPolicy> policies = hrPolicyService.getPoliciesByDepartment(employee.getDepartment(), sessionId);

            // Create complex response object
            EmployeeWithPoliciesDTO result = new EmployeeWithPoliciesDTO();
            result.setEmployee(employee);
            result.setPolicies(policies);
            result.setResponseTimestamp(LocalDateTime.now());
            result.setCached(false);

            // MULTI-LAYER PROCESSING: Security Validation Layer
            EmployeeWithPoliciesDTO.SecurityValidationResult securityResult = new EmployeeWithPoliciesDTO.SecurityValidationResult();
            securityResult.setAuthorized(true);
            securityResult.setSecurityLevel(employee.getSecurityLevel());
            securityResult.setValidationChain("EmployeeDetailsService -> SecurityService -> HrPolicyService");
            securityResult.setContext(context);

            Map<String, Boolean> accessRights = new HashMap<>();
            accessRights.put("READ_EMPLOYEE", true);
            accessRights.put("READ_POLICIES", policies.size() > 0);
            accessRights.put("UPDATE_EMPLOYEE", context.canAccessSensitiveData());
            accessRights.put("DELETE_EMPLOYEE", context.canAccessSensitiveData());
            securityResult.setAccessRights(accessRights);

            List<String> permissions = new ArrayList<>();
            permissions.add("EMPLOYEE_READ");
            permissions.add("POLICY_READ");
            if (context.canAccessSensitiveData())
            {
                permissions.add("EMPLOYEE_WRITE");
                permissions.add("EMPLOYEE_DELETE");
            }
            securityResult.setPermissions(permissions);

            result.setSecurityValidation(securityResult);

            // MULTI-LAYER PROCESSING: Business Rule Validation Layer
            EmployeeWithPoliciesDTO.BusinessRuleValidation businessValidation = new EmployeeWithPoliciesDTO.BusinessRuleValidation();
            businessValidation.setCompliant("ACTIVE".equals(employee.getStatus()) && policies.stream().anyMatch(p -> "MANDATORY".equals(p.getCategory())));
            businessValidation.setDepartmentValidation(employee.getDepartment() + "_VALIDATED");
            businessValidation.setPolicyCompliance("COMPLIANT_" + policies.size() + "_POLICIES");

            Map<String, Object> businessRules = new HashMap<>();
            businessRules.put("EMPLOYEE_ACTIVE", "ACTIVE".equals(employee.getStatus()));
            businessRules.put("DEPARTMENT_VALID", !employee.getDepartment().isEmpty());
            businessRules.put("POLICIES_REQUIRED", policies.size() > 0);
            businessRules.put("SECURITY_LEVEL_APPROPRIATE", "BASIC".equals(employee.getSecurityLevel()) || context.canAccessSensitiveData());
            businessValidation.setBusinessRules(businessRules);

            List<String> complianceChecks = new ArrayList<>();
            complianceChecks.add("EMPLOYEE_STATUS_CHECK");
            complianceChecks.add("DEPARTMENT_POLICY_CHECK");
            complianceChecks.add("SECURITY_LEVEL_CHECK");
            businessValidation.setComplianceChecks(complianceChecks);

            Map<String, Boolean> ruleResults = new HashMap<>();
            ruleResults.put("EMPLOYEE_EXISTS", true);
            ruleResults.put("DEPARTMENT_VALID", true);
            ruleResults.put("POLICIES_AVAILABLE", policies.size() > 0);
            ruleResults.put("SECURITY_COMPLIANT", securityResult.isAuthorized());
            businessValidation.setRuleResults(ruleResults);

            result.setBusinessRuleValidation(businessValidation);

            // MULTI-LAYER PROCESSING: Audit Trail Layer
            EmployeeWithPoliciesDTO.AuditTrail auditTrail = new EmployeeWithPoliciesDTO.AuditTrail();
            auditTrail.setTransactionId(UUID.randomUUID().toString());
            auditTrail.setProcessingChain("EmployeeDetailsService -> HrPolicyService -> SecurityService -> AuditService");

            Map<String, LocalDateTime> timestamps = new HashMap<>();
            timestamps.put("START", LocalDateTime.now().minusSeconds(3));
            timestamps.put("EMPLOYEE_FETCH", LocalDateTime.now().minusSeconds(2));
            timestamps.put("POLICY_FETCH", LocalDateTime.now().minusSeconds(1));
            timestamps.put("COMPLETE", LocalDateTime.now());
            auditTrail.setTimestamps(timestamps);

            List<String> operations = new ArrayList<>();
            operations.add("SECURITY_VALIDATION");
            operations.add("EMPLOYEE_FETCH");
            operations.add("POLICY_RETRIEVAL");
            operations.add("BUSINESS_RULE_CHECK");
            operations.add("AUDIT_LOGGING");
            auditTrail.setOperations(operations);

            Map<String, String> contextData = new HashMap<>();
            contextData.put("SESSION_ID", sessionId);
            contextData.put("EMPLOYEE_ID", employeeId);
            contextData.put("DEPARTMENT", employee.getDepartment());
            contextData.put("SECURITY_LEVEL", employee.getSecurityLevel());
            contextData.put("POLICY_COUNT", String.valueOf(policies.size()));
            auditTrail.setContextData(contextData);

            result.setAuditTrail(auditTrail);

            // MULTI-LAYER PROCESSING: Cross Reference Data Layer
            EmployeeWithPoliciesDTO.CrossReferenceData crossRefs = new EmployeeWithPoliciesDTO.CrossReferenceData();
            crossRefs.setDependencies(new ArrayList<>());
            crossRefs.getDependencies().add("HrPolicyService");
            crossRefs.getDependencies().add("SecurityService");
            crossRefs.getDependencies().add("AuditService");
            crossRefs.getDependencies().add("EmployeeService");

            Map<String, String> references = new HashMap<>();
            references.put("EMPLOYEE_SOURCE", "EmployeeService");
            references.put("POLICY_SOURCE", "HrPolicyService");
            references.put("SECURITY_SOURCE", "SecurityService");
            references.put("AUDIT_SOURCE", "AuditService");
            crossRefs.setReferences(references);

            result.setCrossReferences(crossRefs);

            // MULTI-LAYER PROCESSING: Response Metadata Layer
            EmployeeWithPoliciesDTO.ResponseMetadata metadata = new EmployeeWithPoliciesDTO.ResponseMetadata();
            metadata.setVersion("2.0");
            metadata.setSource("EmployeeDetailsService");
            metadata.setGeneratedAt(LocalDateTime.now());
            metadata.setProcessingTime("3 seconds");

            Map<String, Object> configuration = new HashMap<>();
            configuration.put("CACHE_ENABLED", true);
            configuration.put("SECURITY_ENABLED", true);
            configuration.put("AUDIT_ENABLED", true);
            configuration.put("COMPLIANCE_CHECK_ENABLED", true);
            metadata.setConfiguration(configuration);

            List<String> transformations = new ArrayList<>();
            transformations.add("SECURITY_VALIDATION");
            transformations.add("BUSINESS_RULE_VALIDATION");
            transformations.add("AUDIT_TRAIL_GENERATION");
            transformations.add("CROSS_REFERENCE_POPULATION");
            transformations.add("METADATA_ENRICHMENT");
            metadata.setTransformations(transformations);

            result.setMetadata(metadata);

            // CONDITIONAL LOGIC: Add validation errors based on business rules
            if (!"ACTIVE".equals(employee.getStatus()))
            {
                result.addValidationError("EMPLOYEE_STATUS", "Employee status is not active", "ERROR", "EmployeeDetailsService");
            }
            if (policies.isEmpty())
            {
                result.addValidationError("POLICIES", "No policies found for department", "WARNING", "HrPolicyService");
            }
            if (!securityResult.isAuthorized())
            {
                result.addValidationError("SECURITY", "Insufficient security level", "ERROR", "SecurityService");
            }

            // Add dynamic fields based on conditional logic
            result.addDynamicField("COMPLEXITY_LEVEL", "HIGH");
            result.addDynamicField("INTERDEPENDENCY_COUNT", 3);
            result.addDynamicField("LAYER_COUNT", 5);
            result.addDynamicField("PROCESSING_CHAIN", "EmployeeDetailsService -> HrPolicyService -> SecurityService -> AuditService");

            // Cache the result
            responseCache.put(cacheKey, result);
            accessCounters.merge(employeeId, 1, Integer::sum);

            // Log audit event
            auditService.logAuditEvent(context.getUserId(), "EMPLOYEE_ACCESS", "Accessed employee details: " + employeeId + " with " + policies.size() + " policies", "EmployeeDetailsService", sessionId);

            return result;

        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to get employee with policies: " + e.getMessage(), e);
        }
    }

    // Cache cleanup method (scheduled to run every 300 seconds)
    @Scheduled(fixedRate = 300_000)
    public void cleanupExpiredCache()
    {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        responseCache.entrySet().removeIf(entry -> entry.getValue().getResponseTimestamp().isBefore(cutoff));
    }

    public void updateEmployee(String employeeId, String name, String department, String email, String phone, String sessionId) {
        // Security check
        if (!securityService.validateSession(sessionId, "WRITE")) {
            throw new SecurityException("Unauthorized update attempt");
        }

        // Find the employee
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        // Update employee fields
        employee.setName(name);
        employee.setDepartment(department);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setLastModified(LocalDateTime.now());

        // Save or update the employee in the database
        employeeRepository.save(employee);  // Using save, which handles both insert and update

        // Log audit event (optional)
        auditService.logAuditEvent("SYSTEM", "EMPLOYEE_UPDATED", "Employee updated: " + employeeId, "EmployeeService",sessionId);

        // Optionally, refresh the cache (if applicable)
        refreshEmployeeCache(employeeId);
    }

    // Delete Employee Method
    public void deleteEmployee(String employeeId, String sessionId) {
        // Security check
        if (!securityService.validateSession(sessionId, "WRITE")) {
            throw new SecurityException("Unauthorized delete attempt");
        }

        // Find the employee
        Employee employee = employeeRepository.findById(employeeId).orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        // Delete the employee
        employeeRepository.delete(employee);

        // Log audit event
        auditService.logAuditEvent("SYSTEM", "EMPLOYEE_DELETED", "Employee deleted: " + employeeId, "EmployeeService", sessionId);

        // Optionally, refresh the cache (if applicable)
        refreshEmployeeCache(employeeId);
    }

    public void refreshEmployeeCache(String employeeId) {
        try {
            // Remove from cache
            responseCache.entrySet().removeIf(entry -> entry.getKey().startsWith(employeeId + "_"));
        } catch (Exception e) {
            // Log error silently for cache operations
        }
    }

    // Reset access counters (scheduled to run every hour)
    @Scheduled(fixedRate = 3_600_000)
    public void resetAccessCounters()
    {
        accessCounters.clear();
    }

}
