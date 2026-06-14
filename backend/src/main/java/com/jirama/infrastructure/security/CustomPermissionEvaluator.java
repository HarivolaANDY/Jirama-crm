package com.jirama.infrastructure.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Custom {@link PermissionEvaluator} for use with {@code @PreAuthorize("hasPermission(...)")}.
 * <p>
 * Evaluates permissions based on the user's granted authorities. Each permission
 * entry maps a {@code targetType:permission} pair to the set of roles allowed to
 * perform that action.
 * <p>
 * Example usage:
 * <pre>{@code
 * @PreAuthorize("hasPermission('INVOICE', 'PAY')")
 * }</pre>
 */
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private static final String ROLE_PREFIX = "ROLE_";

    /**
     * Permission matrix: targetDomainObject (or targetType) + permission → allowed roles.
     * Roles are stored without the "ROLE_" prefix and are compared case-insensitively.
     */
    private static final Map<String, Map<String, Set<String>>> PERMISSIONS = Map.of(
            "SUBSCRIBER", Map.of(
                    "READ",  Set.of("CUSTOMER", "AGENT", "TECHNICIAN", "ADMIN"),
                    "WRITE", Set.of("ADMIN", "AGENT")
            ),
            "INVOICE", Map.of(
                    "READ",  Set.of("CUSTOMER", "AGENT", "TECHNICIAN", "ADMIN"),
                    "PAY",   Set.of("CUSTOMER", "ADMIN"),
                    "WRITE", Set.of("ADMIN")
            ),
            "INCIDENT", Map.of(
                    "REPORT", Set.of("CUSTOMER", "TECHNICIAN"),
                    "READ",   Set.of("CUSTOMER", "TECHNICIAN", "AGENT", "ADMIN"),
                    "WRITE",  Set.of("TECHNICIAN", "ADMIN")
            ),
            "ADMIN_PANEL", Map.of(
                    "ACCESS", Set.of("ADMIN")
            )
    );

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Object targetDomainObject,
                                 Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (!(targetDomainObject instanceof String targetType)
                || !(permission instanceof String action)) {
            return false;
        }
        return evaluate(targetType.toUpperCase(), action.toUpperCase(), authentication);
    }

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Serializable targetId,
                                 String targetType,
                                 Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if (!(permission instanceof String action)) {
            return false;
        }
        return evaluate(targetType.toUpperCase(), action.toUpperCase(), authentication);
    }

    private boolean evaluate(String targetType, String action, Authentication auth) {
        var targetPermissions = PERMISSIONS.get(targetType);
        if (targetPermissions == null) {
            return false;
        }
        var allowedRoles = targetPermissions.get(action);
        if (allowedRoles == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> {
                    String role = authority.startsWith(ROLE_PREFIX)
                            ? authority.substring(ROLE_PREFIX.length())
                            : authority;
                    return allowedRoles.contains(role);
                });
    }
}
