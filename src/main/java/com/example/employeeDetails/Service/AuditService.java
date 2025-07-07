package com.example.employeeDetails.Service;

import com.example.employeeDetails.Entity.AuditLog;
import com.example.employeeDetails.Repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuditService
{

    private final AuditLogRepository auditLogRepository;

    private final ConcurrentHashMap<String, AuditLog> pendingAudits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> auditCounters = new ConcurrentHashMap<>();


    public AuditService(AuditLogRepository auditLogRepository)
    {
        this.auditLogRepository = auditLogRepository;
    }

    @Scheduled(fixedRate = 3600000) // 1 hour
    public void processPendingAudits()
    {
        try
        {
            if (pendingAudits.isEmpty())
            {
                return;
            }
            // Process up to 100 pending audits
            int processed = 0;
            for (AuditLog auditLog : pendingAudits.values())
            {
                if (processed >= 100)
                    break;
                auditLogRepository.save(auditLog);  // Saving to DB
                processed++;
            }
            // Clear processed audits
            pendingAudits.clear();
        }
        catch (Exception e)
        {
            // Log error silently for audit processing
        }
    }

    public void logAuditEvent(String employeeId, String action, String details, String source, String transactionId)
    {
        try
        {
            AuditLog auditLog = new AuditLog();
            auditLog.setEmployeeId(employeeId);
            auditLog.setAction(action);
            auditLog.setDetails(details);
            auditLog.setTimestamp(LocalDateTime.now());
            auditLog.setSourceEJB(source);
            auditLog.setTransactionId(transactionId);

            // Check if this is a high priority audit
            if (isHighPriorityAudit(action))
            {
                processAuditImmediately(auditLog);
            }
            else
            {
                // Add to pending audits
                String auditKey = UUID.randomUUID().toString();
                pendingAudits.put(auditKey, auditLog);
            }

            // Increment audit counter
            auditCounters.merge(action, 1, Integer::sum);
        }
        catch (Exception e)
        {
            // Log error silently for audit operations
        }
    }


    public void clearAuditCounters()
    {
        auditCounters.clear();
    }

    private boolean isHighPriorityAudit(String action)
    {
        return "SECURITY_VIOLATION".equals(action) || "UNAUTHORIZED_ACCESS".equals(action) || "COMPLIANCE_VIOLATION".equals(action);
    }

    private void processAuditImmediately(AuditLog auditLog)
    {
        try
        {
            auditLogRepository.save(auditLog);  // Save immediately
        }
        catch (Exception e)
        {
            // Log error silently for audit processing
        }
    }

}
