package com.example.employeeDetails.Service;

import com.example.employeeDetails.Entity.SecurityContext;
import com.example.employeeDetails.Repository.SecurityContextRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SecurityService
{

    private final SecurityContextRepository securityContextRepository;

    private final ConcurrentHashMap<String, SecurityContext> activeSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> failedAttempts = new ConcurrentHashMap<>();

    public SecurityService(SecurityContextRepository securityContextRepository)
    {
        this.securityContextRepository = securityContextRepository;

    }

    // Create a new security context for a user and store it in the repository and active sessions map
    public SecurityContext createSecurityContext(String userId, String securityLevel, String sessionId)
    {
        try
        {
            SecurityContext context = new SecurityContext();
            context.setSessionId(sessionId);
            context.setUserId(userId);
            context.setSecurityLevel(securityLevel);
            context.setCreatedDate(LocalDateTime.now());
            context.setLastAccessed(LocalDateTime.now());
            context.setActive(true);
            context.setExpiryDate(LocalDateTime.now().plusHours(8));
            context.setPermissions(List.of("WRITE", "READ")); // demo permissions

            // Save the context to the database
            securityContextRepository.save(context);

            // Add it to the active sessions map
            activeSessions.put(sessionId, context);

            return context;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Security context creation failed", e);
        }
    }

    // Validate if a session is still active and has the required permissions
    public boolean validateSession(String sessionId, String requiredPermission)
    {
        try
        {
            SecurityContext context = activeSessions.computeIfAbsent(sessionId, sid -> securityContextRepository.findBySessionId(sid).orElse(null));

            if (context == null || !context.isActive())
                return false;

            // Check if the session is expired
            if (context.getExpiryDate() != null && context.getExpiryDate().isBefore(LocalDateTime.now()))
            {
                context.setActive(false);
                activeSessions.remove(sessionId);
                return false;
            }

            context.setLastAccessed(LocalDateTime.now());

            // Check permissions if required
            return requiredPermission == null || context.hasPermission(requiredPermission);
        }
        catch (Exception e)
        {
            return false;
        }
    }

    // Get the SecurityContext for a given sessionId
    public SecurityContext getSecurityContext(String sessionId)
    {
        try
        {
            SecurityContext context = activeSessions.computeIfAbsent(sessionId, sid -> securityContextRepository.findBySessionId(sid).orElse(null));

            if (context != null && context.isActive())
            {
                context.setLastAccessed(LocalDateTime.now());
            }

            return context;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    // Periodically clean up expired sessions
    @Scheduled(fixedRate = 1800000) // Every 30 minutes
    public void cleanupExpiredSessions()
    {
        try
        {
            LocalDateTime cutoff = LocalDateTime.now().minusHours(8);

            activeSessions.entrySet().removeIf(entry -> {
                SecurityContext context = entry.getValue();
                if (context.getLastAccessed().isBefore(cutoff))
                {
                    context.setActive(false);
                    securityContextRepository.save(context);
                    return true;
                }
                return false;
            });
        }
        catch (Exception e)
        {
            // Log error silently
        }
    }

    // Periodically check for suspicious activity, like multiple failed login attempts
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void checkSuspiciousActivity()
    {
        try
        {
            for (String userId : failedAttempts.keySet())
            {
                int attempts = failedAttempts.get(userId);
                if (attempts > 5)
                {
                    List<SecurityContext> sessions = securityContextRepository.findByUserId(userId);
                    for (SecurityContext session : sessions)
                    {
                        session.setActive(false);
                        securityContextRepository.save(session);
                    }
                }
            }
        }
        catch (Exception e)
        {
            // Log error silently
        }
    }
}
