package com.harsh.rentify.repository;

import com.harsh.rentify.entity.ReviewEntity;
import com.harsh.rentify.entity.RoomEntity;
import com.harsh.rentify.entity.UserEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

    List<ReviewEntity> findByRoomOrderByCreatedAtDesc(RoomEntity room);

    boolean existsByRoomAndAuthor(RoomEntity room, UserEntity author);
}
