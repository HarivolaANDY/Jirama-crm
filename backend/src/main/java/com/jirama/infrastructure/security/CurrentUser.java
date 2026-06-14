package com.jirama.infrastructure.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Meta-annotation for injecting the current authenticated user's JWT claims.
 * Usage: @CurrentUser Jwt jwt
 * Or use with a custom UserDetails object if needed.
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}
