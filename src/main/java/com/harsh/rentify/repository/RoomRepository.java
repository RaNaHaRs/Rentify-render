package com.harsh.rentify.repository;

import com.harsh.rentify.entity.RoomEntity;
import com.harsh.rentify.entity.UserEntity;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RoomRepository extends JpaRepository<RoomEntity, Long>, JpaSpecificationExecutor<RoomEntity> {

    List<RoomEntity> findTop6ByOrderByCreatedAtDesc();

    List<RoomEntity> findByHostOrderByCreatedAtDesc(UserEntity host);

    List<RoomEntity> findByHostUsernameAndTitleIn(String username, Collection<String> titles);
}
