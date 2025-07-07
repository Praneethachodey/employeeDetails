package com.example.employeeDetails.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HrPolicy
{

    private String policyId;
    private String description;
    private String category;
    private String status = "ACTIVE";
    private String requiredSecurityLevel = "BASIC";
    private String version = "1.0";
    private LocalDateTime effectiveDate;
    private LocalDateTime expiryDate;
    private String createdBy;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;
    private String approvalStatus = "PENDING";
    private List<String> applicableDepartments;
    private Map<String, String> conditions;
    private Integer priorityLevel = 1;
    private boolean complianceRequired = false;
    private Integer auditFrequencyDays = 30;

    // Transient fields
    private boolean isCached = false;
    private LocalDateTime lastAccessed;
    private int accessCount = 0;
}
