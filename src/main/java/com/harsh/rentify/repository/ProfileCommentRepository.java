package com.harsh.rentify.repository;

import com.harsh.rentify.entity.ProfileCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileCommentRepository extends JpaRepository<ProfileCommentEntity, Long> {
}
