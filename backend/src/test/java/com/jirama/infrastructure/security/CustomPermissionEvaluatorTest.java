package com.jirama.infrastructure.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Direct unit test for {@link CustomPermissionEvaluator}. Tests both
 * {@code hasPermission} overloads in isolation — no Spring context required.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CustomPermissionEvaluator unit tests")
class CustomPermissionEvaluatorTest {

    private final CustomPermissionEvaluator evaluator = new CustomPermissionEvaluator();

    @Mock
    private Authentication authentication;

    // ──────────────────────────────────────────────
    // Precondition edge cases (shared by both overloads)
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("Precondition checks")
    class PreconditionChecks {

        @Test
        @DisplayName("null authentication → false")
        void nullAuthentication() {
            assertThat(evaluator.hasPermission(null, "INVOICE", "READ")).isFalse();
            assertThat(evaluator.hasPermission(null, UUID.randomUUID(), "INVOICE", "READ")).isFalse();
        }

        @Test
        @DisplayName("unauthenticated authentication → false")
        void unauthenticated() {
            when(authentication.isAuthenticated()).thenReturn(false);

            assertThat(evaluator.hasPermission(authentication, "INVOICE", "READ")).isFalse();
            assertThat(evaluator.hasPermission(authentication, UUID.randomUUID(), "INVOICE", "READ")).isFalse();
        }

        @Test
        @DisplayName("non-String targetDomainObject → false (first overload)")
        void nonStringTargetObject() {
            when(authentication.isAuthenticated()).thenReturn(true);
            assertThat(evaluator.hasPermission(authentication, 42, "READ")).isFalse();
        }

        @Test
        @DisplayName("non-String permission → false (first overload)")
        void nonStringPermissionFirstOverload() {
            when(authentication.isAuthenticated()).thenReturn(true);
            assertThat(evaluator.hasPermission(authentication, "INVOICE", 99)).isFalse();
        }

        @Test
        @DisplayName("non-String permission → false (second overload)")
        void nonStringPermissionSecondOverload() {
            when(authentication.isAuthenticated()).thenReturn(true);
            assertThat(evaluator.hasPermission(authentication, UUID.randomUUID(), "INVOICE", 99)).isFalse();
        }
    }

    // ──────────────────────────────────────────────
    // First overload: hasPermission(Auth, Object, Object)
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("hasPermission(Authentication, Object, Object)")
    class FirstOverload {

        @Test
        @DisplayName("SUBSCRIBER:READ — all allowed roles return true")
        void subscriberReadAllRoles() {
            for (String role : new String[]{"ROLE_CUSTOMER", "ROLE_AGENT", "ROLE_TECHNICIAN", "ROLE_ADMIN"}) {
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority(role)));

                assertThat(evaluator.hasPermission(authentication, "SUBSCRIBER", "READ"))
                        .as("SUBSCRIBER:READ should be allowed for %s", role)
                        .isTrue();
            }
        }

        @Test
        @DisplayName("SUBSCRIBER:WRITE — only ADMIN and AGENT return true")
        void subscriberWrite() {
            when(authentication.isAuthenticated()).thenReturn(true);

            for (String role : new String[]{"ROLE_ADMIN", "ROLE_AGENT"}) {
                when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority(role)));
                assertThat(evaluator.hasPermission(authentication, "SUBSCRIBER", "WRITE"))
                        .as("SUBSCRIBER:WRITE should be allowed for %s", role)
                        .isTrue();
            }

            for (String role : new String[]{"ROLE_CUSTOMER", "ROLE_TECHNICIAN"}) {
                when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority(role)));
                assertThat(evaluator.hasPermission(authentication, "SUBSCRIBER", "WRITE"))
                        .as("SUBSCRIBER:WRITE should be denied for %s", role)
                        .isFalse();
            }
        }

        @Test
        @DisplayName("INVOICE:PAY — only CUSTOMER and ADMIN return true")
        void invoicePay() {
            when(authentication.isAuthenticated()).thenReturn(true);

            for (String role : new String[]{"ROLE_CUSTOMER", "ROLE_ADMIN"}) {
                when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority(role)));
                assertThat(evaluator.hasPermission(authentication, "INVOICE", "PAY"))
                        .as("INVOICE:PAY should be allowed for %s", role)
                        .isTrue();
            }

            for (String role : new String[]{"ROLE_AGENT", "ROLE_TECHNICIAN"}) {
                when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority(role)));
                assertThat(evaluator.hasPermission(authentication, "INVOICE", "PAY"))
                        .as("INVOICE:PAY should be denied for %s", role)
                        .isFalse();
            }
        }

        @Test
        @DisplayName("INVOICE:WRITE — only ADMIN returns true")
        void invoiceWrite() {
            when(authentication.isAuthenticated()).thenReturn(true);

            when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            assertThat(evaluator.hasPermission(authentication, "INVOICE", "WRITE")).isTrue();

            when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
            assertThat(evaluator.hasPermission(authentication, "INVOICE", "WRITE")).isFalse();
        }

        @Test
        @DisplayName("ADMIN_PANEL:ACCESS — only ADMIN returns true")
        void adminPanelAccess() {
            when(authentication.isAuthenticated()).thenReturn(true);

            when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
            assertThat(evaluator.hasPermission(authentication, "ADMIN_PANEL", "ACCESS")).isTrue();

            when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
            assertThat(evaluator.hasPermission(authentication, "ADMIN_PANEL", "ACCESS")).isFalse();
        }

        @Test
        @DisplayName("INCIDENT:REPORT — only CUSTOMER and TECHNICIAN return true")
        void incidentReport() {
            when(authentication.isAuthenticated()).thenReturn(true);

            for (String role : new String[]{"ROLE_CUSTOMER", "ROLE_TECHNICIAN"}) {
                when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority(role)));
                assertThat(evaluator.hasPermission(authentication, "INCIDENT", "REPORT"))
                        .as("INCIDENT:REPORT should be allowed for %s", role)
                        .isTrue();
            }

            for (String role : new String[]{"ROLE_ADMIN", "ROLE_AGENT"}) {
                when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority(role)));
                assertThat(evaluator.hasPermission(authentication, "INCIDENT", "REPORT"))
                        .as("INCIDENT:REPORT should be denied for %s", role)
                        .isFalse();
            }
        }

        @Test
        @DisplayName("unknown target type → false")
        void unknownTargetType() {
            when(authentication.isAuthenticated()).thenReturn(true);
            // Note: no getAuthorities() stub needed — evaluator short-circuits on unknown target type
            // before checking authorities

            assertThat(evaluator.hasPermission(authentication, "BILLING", "READ")).isFalse();
            assertThat(evaluator.hasPermission(authentication, "UNKNOWN", "ANY")).isFalse();
        }

        @Test
        @DisplayName("unknown action → false")
        void unknownAction() {
            when(authentication.isAuthenticated()).thenReturn(true);
            // Note: no getAuthorities() stub needed — evaluator short-circuits on unknown action
            // before checking authorities

            assertThat(evaluator.hasPermission(authentication, "INVOICE", "DELETE")).isFalse();
            assertThat(evaluator.hasPermission(authentication, "SUBSCRIBER", "EXPORT")).isFalse();
        }
    }

    // ──────────────────────────────────────────────
    // Second overload: hasPermission(Auth, Serializable, String, Object)
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("hasPermission(Authentication, Serializable, String, Object)")
    class SecondOverload {

        private final UUID testId = UUID.randomUUID();

        @Test
        @DisplayName("SUBSCRIBER:READ — all allowed roles return true (by targetId)")
        void subscriberReadAllRoles() {
            for (String role : new String[]{"ROLE_CUSTOMER", "ROLE_AGENT", "ROLE_TECHNICIAN", "ROLE_ADMIN"}) {
                when(authentication.isAuthenticated()).thenReturn(true);
                when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority(role)));

                assertThat(evaluator.hasPermission(authentication, testId, "SUBSCRIBER", "READ"))
                        .as("SUBSCRIBER:READ should be allowed for %s", role)
                        .isTrue();
            }
        }

        @Test
        @DisplayName("unknown target type → false")
        void unknownTargetType() {
            when(authentication.isAuthenticated()).thenReturn(true);
            // Note: no getAuthorities() stub needed — evaluator short-circuits on unknown target type
            // before checking authorities

            assertThat(evaluator.hasPermission(authentication, testId, "BILLING", "READ")).isFalse();
        }
    }

    // ──────────────────────────────────────────────
    // Case insensitivity & role format
    // ──────────────────────────────────────────────

    @Nested
    @DisplayName("Case insensitivity and role format")
    class CaseInsensitivity {

        @Test
        @DisplayName("target type is case-insensitive")
        void targetTypeCaseInsensitive() {
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            assertThat(evaluator.hasPermission(authentication, "invoice", "PAY")).isTrue();
            assertThat(evaluator.hasPermission(authentication, "Invoice", "PAY")).isTrue();
            assertThat(evaluator.hasPermission(authentication, "INVOICE", "PAY")).isTrue();
        }

        @Test
        @DisplayName("action is case-insensitive")
        void actionCaseInsensitive() {
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            assertThat(evaluator.hasPermission(authentication, "ADMIN_PANEL", "access")).isTrue();
            assertThat(evaluator.hasPermission(authentication, "ADMIN_PANEL", "Access")).isTrue();
            assertThat(evaluator.hasPermission(authentication, "ADMIN_PANEL", "ACCESS")).isTrue();
        }

        @Test
        @DisplayName("authority with ROLE_ prefix matches correctly")
        void rolePrefixMatches() {
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

            assertThat(evaluator.hasPermission(authentication, "ADMIN_PANEL", "ACCESS")).isTrue();
        }

        @Test
        @DisplayName("authority without ROLE_ prefix also matches")
        void roleWithoutPrefixMatches() {
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority("ADMIN")));

            assertThat(evaluator.hasPermission(authentication, "ADMIN_PANEL", "ACCESS")).isTrue();
        }

        @Test
        @DisplayName("non-matching authority returns false")
        void nonMatchingAuthority() {
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(new SimpleGrantedAuthority("ROLE_USER")));

            assertThat(evaluator.hasPermission(authentication, "ADMIN_PANEL", "ACCESS")).isFalse();
        }

        @Test
        @DisplayName("user with multiple authorities matches on any allowed role")
        void multipleAuthorities() {
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(
                    new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                    new SimpleGrantedAuthority("ROLE_TECHNICIAN")
            ));

            assertThat(evaluator.hasPermission(authentication, "INCIDENT", "REPORT")).isTrue();
        }

        @Test
        @DisplayName("user with multiple authorities, none matching → false")
        void multipleAuthoritiesNoneMatch() {
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getAuthorities()).thenAnswer(ignored -> List.of(
                    new SimpleGrantedAuthority("ROLE_CUSTOMER"),
                    new SimpleGrantedAuthority("ROLE_TECHNICIAN")
            ));

            assertThat(evaluator.hasPermission(authentication, "ADMIN_PANEL", "ACCESS")).isFalse();
        }
    }
}
