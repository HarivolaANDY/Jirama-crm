package com.jirama.domain.user;

import com.jirama.domain.user.enums.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Output port for User persistence.
 */
public interface UserRepository {

    Optional<User> findById(UUID id);

    Optional<User> findByEmployeeNumber(String employeeNumber);

    Optional<User> findByEmail(String email);

    Optional<User> findByKeycloakId(String keycloakId);

    List<User> findByRole(UserRole role);

    List<User> findByRegionId(UUID regionId);

    List<User> findAll(int page, int size);

    long count();

    User save(User user);

    void deleteById(UUID id);
}
