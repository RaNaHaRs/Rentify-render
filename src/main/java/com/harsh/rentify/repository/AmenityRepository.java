package com.harsh.rentify.repository;

import com.harsh.rentify.entity.AmenityEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<AmenityEntity, Long> {
}
