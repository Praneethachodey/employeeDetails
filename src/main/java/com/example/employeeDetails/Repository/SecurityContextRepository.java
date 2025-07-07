package com.example.employeeDetails.Repository;

import com.example.employeeDetails.Entity.SecurityContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SecurityContextRepository extends JpaRepository<SecurityContext, Long>
{

    Optional<SecurityContext> findBySessionId(String sessionId);

    List<SecurityContext> findByUserId(String userId);

    List<SecurityContext> findByActiveTrue();

    List<SecurityContext> findBySecurityLevel(String securityLevel);

    List<SecurityContext> findByAuditRequiredTrue();

    List<SecurityContext> findBySourceEJB(String sourceEJB);

    List<SecurityContext> findByComplianceLevel(String complianceLevel);

    List<SecurityContext> findByTransactionId(String transactionId);

    List<SecurityContext> findByIpAddress(String ipAddress);

    @Query("SELECT s FROM SecurityContext s WHERE s.expiryDate < :now")
    List<SecurityContext> findExpiredSessions(@Param("now") LocalDateTime now);

    @Query("SELECT s FROM SecurityContext s WHERE s.lockedUntil > :now")
    List<SecurityContext> findLockedSessions(@Param("now") LocalDateTime now);

    @Modifying
    @Transactional
    @Query("UPDATE SecurityContext s SET s.lastAccessed = :lastAccessed WHERE s.sessionId = :sessionId")
    void updateLastAccessed(@Param("sessionId") String sessionId, @Param("lastAccessed") LocalDateTime lastAccessed);

    @Modifying
    @Transactional
    @Query("UPDATE SecurityContext s SET s.lockedUntil = :lockUntil WHERE s.sessionId = :sessionId")
    void lockSession(@Param("sessionId") String sessionId, @Param("lockUntil") LocalDateTime lockUntil);

    @Modifying
    @Transactional
    @Query("UPDATE SecurityContext s SET s.lockedUntil = NULL, s.failedAttempts = 0 WHERE s.sessionId = :sessionId")
    void unlockSession(@Param("sessionId") String sessionId);
}