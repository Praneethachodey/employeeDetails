package com.example.employeeDetails.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class AuditLog
{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "details", length = 1000)
    private String details;

    @CreationTimestamp
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "source_ejb")
    private String sourceEJB;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "security_level")
    private String securityLevel;

    @Column(name = "compliance_required")
    private boolean complianceRequired;

    @Column(name = "audit_level")
    private String auditLevel = "BASIC";

    @Column(name = "encrypted_data")
    private boolean encryptedData;

    @Column(name = "retention_days")
    private Integer retentionDays = 365;

    @Column(name = "archived")
    private boolean archived = false;

    @Column(name = "archive_date")
    private LocalDateTime archiveDate;
}
