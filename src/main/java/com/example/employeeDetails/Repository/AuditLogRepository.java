package com.example.employeeDetails.Repository;

import com.example.employeeDetails.Entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long>
{

    List<AuditLog> findByEmployeeIdOrderByTimestampDesc(String employeeId);

    List<AuditLog> findByAction(String action);

    List<AuditLog> findByTimestampBetween(LocalDateTime startDate, LocalDateTime endDate);
}
