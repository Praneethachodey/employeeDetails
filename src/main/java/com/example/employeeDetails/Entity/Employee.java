package com.example.employeeDetails.Entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "employeeID")
public class Employee
{

    @Id
    @Column(name = "employeeID", nullable = false, updatable = false)
    private String employeeID;

    @Column(nullable = false)
    private String name;

    private String department;
    private String email;
    private String phone;
    private String managerId;

    @Column(nullable = false)
    private String status = "ACTIVE";

    @Column(nullable = false)
    private String securityLevel = "BASIC";

    @CreationTimestamp
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @UpdateTimestamp
    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Version
    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "access_count")
    private Integer accessCount = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "employee_permissions", joinColumns = @JoinColumn(name = "employee_id"))
    @Column(name = "permission")
    private List<String> permissions;

    @Column(name = "salary_band")
    private String salaryBand;

    @Column(name = "location_code")
    private String locationCode;

    @Column(name = "cost_center")
    private String costCenter;

    @Transient
    private boolean isLocked = false;

    @Transient
    private String sessionToken;

}
