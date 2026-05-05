package com.harsh.rentify.repository;

import com.harsh.rentify.entity.ProfileEntity;
import com.harsh.rentify.entity.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

    Optional<ProfileEntity> findByUser(UserEntity user);

    Optional<ProfileEntity> findByUserUsername(String username);
}
