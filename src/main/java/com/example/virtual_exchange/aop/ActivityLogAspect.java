package com.example.virtual_exchange.aop;

import com.example.virtual_exchange.annotation.LogActivity;
import com.example.virtual_exchange.config.auth.PrincipalDetails;
import com.example.virtual_exchange.service.ActivityLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class ActivityLogAspect {

    private final ActivityLogService activityLogService;

    @AfterReturning("@annotation(logActivity)")
    public void logActivity(LogActivity logActivity) {
        Long userId = extractUserId();
        String ip = extractIp();
        String description = logActivity.description().isEmpty()
                ? logActivity.activityType().name()
                : logActivity.description();

        activityLogService.saveActivityLog(userId, logActivity.activityType(), description, ip);
    }

    private Long extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof PrincipalDetails principal) {
            return principal.getUser().getId();
        }
        return null;
    }

    private String extractIp() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            return (forwarded != null && !forwarded.isBlank())
                    ? forwarded.split(",")[0].trim()
                    : request.getRemoteAddr();
        } catch (Exception e) {
            return "unknown";
        }
    }
}
