package com.harsh.rentify.repository;

import com.harsh.rentify.entity.RoomTypeEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomTypeEntity, Long> {

    Optional<RoomTypeEntity> findBySlug(String slug);
}
