package com.example.employeeDetails.Controller;

import com.example.employeeDetails.Entity.AuditLog;
import com.example.employeeDetails.Entity.Employee;
import com.example.employeeDetails.Entity.SecurityContext;
import com.example.employeeDetails.Repository.AuditLogRepository;
import com.example.employeeDetails.Repository.EmployeeRepository;
import com.example.employeeDetails.Repository.SecurityContextRepository;
import com.example.employeeDetails.Service.EmployeeDetailsService;
import com.example.employeeDetails.Service.SecurityService;
import com.example.employeeDetails.dto.EmployeeWithPoliciesDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class EmployeeDetailsController
{

    private final EmployeeRepository employeeRepository;
    private final AuditLogRepository auditLogRepository;
    private final SecurityContextRepository securityContextRepository;

    private final SecurityService securityService;
    private final EmployeeDetailsService employeeDetailsService;

    @GetMapping("/test-save")
    public ResponseEntity<String> testSaveEmployee()
    {
        try
        {
            // Create a dummy employee
            Employee employee = Employee.builder().employeeID("TEST-" + UUID.randomUUID().toString()).name("Test Employee").department("IT").email("test@example.com").phone("+919876543210").managerId("MGR001").status("ACTIVE").securityLevel("BASIC").salaryBand("B2").locationCode("BLR").costCenter("IT-001").permissions(Arrays.asList("READ", "WRITE")).build();

            // Save the employee
            Employee savedEmployee = employeeRepository.save(employee);

            return ResponseEntity.ok(String.format("""
                    Employee saved successfully!
                    ID: %s
                    Name: %s
                    Email: %s
                    Department: %s
                    Created Date: %s
                    """, savedEmployee.getEmployeeID(), savedEmployee.getName(), savedEmployee.getEmail(), savedEmployee.getDepartment(), savedEmployee.getCreatedDate()));

        }
        catch (Exception e)
        {
            return ResponseEntity.status(500).body("Failed to save employee: " + e.getMessage() + "\n\nStack trace: " + Arrays.toString(e.getStackTrace()));
        }
    }

    @GetMapping("/test")
    public AuditLog createTestAuditLog()
    {
        AuditLog auditLog = AuditLog.builder().employeeId("E12345").action("LOGIN_ATTEMPT").details("Test log for DB connection.").userId("admin").sessionId("sess-123").ipAddress("192.168.1.10").sourceEJB("LoginServiceEJB").transactionId("txn-001").securityLevel("BASIC").complianceRequired(false).auditLevel("BASIC").encryptedData(false).retentionDays(365).archived(false).build();

        return auditLogRepository.save(auditLog);
    }

    @GetMapping("/create-demo")
    public ResponseEntity<SecurityContext> createDemoSecurityContext()
    {
        SecurityContext securityContext = new SecurityContext();
        securityContext.setSessionId("demo-" + System.currentTimeMillis());
        securityContext.setUserId("test-user");
        securityContext.setSecurityLevel("ADMIN");
        securityContext.setIpAddress("127.0.0.1");
        securityContext.setUserAgent("SpringBoot-Test-Agent");
        securityContext.setCreatedDate(LocalDateTime.now());
        securityContext.setLastAccessed(LocalDateTime.now());
        securityContext.setActive(true);
        securityContext.setAuditRequired(true);
        securityContext.setComplianceLevel("HIGH");
        securityContext.setPermissions(List.of("READ", "WRITE", "DELETE"));
        securityContext.setRoles(List.of("ROLE_ADMIN"));
        securityContext.setAttribute("region", "IN");

        securityContext = securityContextRepository.save(securityContext);
        return ResponseEntity.ok(securityContext);
    }

    @GetMapping("/employee-details")
    public String getEmployeeDetailsPage(Model model)
    {
        // Simply display the empty form on page load
        return "employee-details";  // Return the view name for the form
    }

    @PostMapping("/employee-details")
    public String handleEmployeeDetails(
            @RequestParam String employeeId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone,
            Model model) {

        if (employeeId == null || employeeId.trim().isEmpty()) {
            model.addAttribute("error", "Employee ID is required");
            return "employee-details";
        }

        try {
            String sessionId = UUID.randomUUID().toString();
            securityService.createSecurityContext("WEB_USER", "BASIC", sessionId);

            if ("update".equalsIgnoreCase(action)) {
                employeeDetailsService.updateEmployee(employeeId, name, department, email, phone, sessionId);
                model.addAttribute("message", "Employee updated successfully!");
            } else if ("delete".equalsIgnoreCase(action)) {
                employeeDetailsService.deleteEmployee(employeeId, sessionId);
                model.addAttribute("message", "Employee deleted successfully!");
                model.addAttribute("employee", null);
                model.addAttribute("policies", null);
                return "employee-details";
            }

            EmployeeWithPoliciesDTO dto = employeeDetailsService.getEmployeeWithPolicies(employeeId, sessionId);
            if (dto != null && dto.getEmployee() != null) {
                model.addAttribute("employee", dto.getEmployee());
                model.addAttribute("policies", dto.getPolicies());
            } else {
                model.addAttribute("error", "Employee not found: " + employeeId);
            }
        } catch (Exception e) {
            model.addAttribute("error", "Failed to process the request: " + e.getMessage());
        }

        return "employee-details"; // Return the view name
    }
}

