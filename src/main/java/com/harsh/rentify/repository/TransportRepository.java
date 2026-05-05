package com.harsh.rentify.repository;

import com.harsh.rentify.entity.TransportEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportRepository extends JpaRepository<TransportEntity, Long> {
}
