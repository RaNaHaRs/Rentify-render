package com.harsh.rentify.repository;

import com.harsh.rentify.entity.Role;
import com.harsh.rentify.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    List<UserEntity> findByRoleOrderByCreatedAtDesc(Role role);

    List<UserEntity> findByRoleAndHostConfirmedFalseOrderByCreatedAtAsc(Role role);

    long countByRole(Role role);
}
