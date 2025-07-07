package com.example.employeeDetails.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "security_contexts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityContext {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", unique = true)
    private String sessionId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "security_level")
    private String securityLevel;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent")
    private String userAgent;

    @Column(name = "created_date")
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "last_accessed")
    private LocalDateTime lastAccessed = LocalDateTime.now();

    @Column(name = "expiry_date")
    private LocalDateTime expiryDate;

    @Column(name = "active")
    private boolean active = true;

    @Column(name = "encryption_key")
    private String encryptionKey;

    @Column(name = "signature")
    private String signature;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "security_permissions", joinColumns = @JoinColumn(name = "security_context_id"))
    @Column(name = "permission")
    private List<String> permissions = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "security_roles", joinColumns = @JoinColumn(name = "security_context_id"))
    @Column(name = "role")
    private List<String> roles = new ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "security_attributes", joinColumns = @JoinColumn(name = "security_context_id"))
    @MapKeyColumn(name = "attribute_key")
    @Column(name = "attribute_value")
    private Map<String, String> attributes = new HashMap<>();

    @Column(name = "multi_factor_enabled")
    private boolean multiFactorEnabled = false;

    @Column(name = "mfa_token")
    private String mfaToken;

    @Column(name = "failed_attempts")
    private Integer failedAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "compliance_level")
    private String complianceLevel = "BASIC";

    @Column(name = "audit_required")
    private boolean auditRequired = false;

    @Column(name = "source_ejb")
    private String sourceEJB;

    @Column(name = "transaction_id")
    private String transactionId;


    public boolean hasPermission(String permission) {
        return permissions != null && permissions.contains(permission);
    }


    public boolean canAccessSensitiveData() {
        return "ADMIN".equals(securityLevel) || "MANAGER".equals(securityLevel);
    }


    public void setAttribute(String key, String value) {
        attributes.put(key, value);
    }

}
