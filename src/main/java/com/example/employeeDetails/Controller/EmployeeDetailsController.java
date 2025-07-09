package com.example.employeeDetails.Controller;

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

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class EmployeeDetailsController
{

    private final SecurityService securityService;
    private final EmployeeDetailsService employeeDetailsService;

    @GetMapping("/employee-details")
    public String getEmployeeDetailsPage(Model model)
    {
        // Simply display the empty form on page load
        return "employee-details";  // Return the view name for the form
    }

    @PostMapping("/employee-details")
    public String handleEmployeeDetails(@RequestParam String employeeId, @RequestParam(required = false) String sessionId, @RequestParam(required = false) String action, @RequestParam(required = false) String name, @RequestParam(required = false) String department, @RequestParam(required = false) String email, @RequestParam(required = false) String phone, Model model)
    {

        if (employeeId == null || employeeId.trim().isEmpty())
        {
            model.addAttribute("error", "Employee ID is required");
            return "employee-details";
        }

        try
        {

            sessionId = UUID.randomUUID().toString();
            securityService.createSecurityContext("WEB_USER", "BASIC", sessionId);
            if ("update".equalsIgnoreCase(action))
            {
                employeeDetailsService.updateEmployee(employeeId, name, department, email, phone, sessionId);
                model.addAttribute("message", "Employee updated successfully!");
            }
            else if ("delete".equalsIgnoreCase(action))
            {
                employeeDetailsService.deleteEmployee(employeeId, sessionId);
                model.addAttribute("message", "Employee deleted successfully!");
                model.addAttribute("employee", null);
                model.addAttribute("policies", null);
                return "employee-details";
            }

            EmployeeWithPoliciesDTO dto = employeeDetailsService.getEmployeeWithPolicies(employeeId, sessionId);
            if (dto != null && dto.getEmployee() != null)
            {
                model.addAttribute("employee", dto.getEmployee());
                model.addAttribute("policies", dto.getPolicies());
            }
            else
            {
                model.addAttribute("error", "Employee not found: " + employeeId);
            }
        }
        catch (Exception e)
        {
            model.addAttribute("error", "Failed to process the request: " + e.getMessage());
        }

        return "employee-details"; // Return the view name
    }

    @PostMapping("/employee-details-client")
    public ResponseEntity<EmployeeWithPoliciesDTO> handleEmployeeDetailsClient(@RequestParam String employeeId, @RequestParam String sessionId, @RequestParam(required = false) String action, @RequestParam(required = false) String name, @RequestParam(required = false) String department, @RequestParam(required = false) String email, @RequestParam(required = false) String phone)
    {

        if (employeeId == null || employeeId.trim().isEmpty())
        {
            return ResponseEntity.badRequest().build();
        }
        try
        {
            if ("update".equalsIgnoreCase(action))
            {
                employeeDetailsService.updateEmployee(employeeId, name, department, email, phone, sessionId);
            }
            else if ("delete".equalsIgnoreCase(action))
            {
                employeeDetailsService.deleteEmployee(employeeId, sessionId);
                return ResponseEntity.ok(new EmployeeWithPoliciesDTO());
            }

            // Always fetch the employee data
            EmployeeWithPoliciesDTO dto = employeeDetailsService.getEmployeeWithPolicies(employeeId, sessionId);

            if (dto == null || dto.getEmployee() == null)
            {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok(dto);

        }
        catch (Exception e)
        {
            return ResponseEntity.internalServerError().build();
        }
    }
}

