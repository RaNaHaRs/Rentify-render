package com.harsh.rentify.repository;

import com.harsh.rentify.entity.ReservationEntity;
import com.harsh.rentify.entity.ReservationStatus;
import com.harsh.rentify.entity.UserEntity;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {

    List<ReservationEntity> findByTenantOrderByCreatedAtDesc(UserEntity tenant);

    List<ReservationEntity> findByRoomHostOrderByCreatedAtDesc(UserEntity host);

    @Query("""
            select case when count(r) > 0 then true else false end
            from ReservationEntity r
            where r.room.id = :roomId
              and r.status in :statuses
              and r.checkInDate < :checkOut
              and r.checkOutDate > :checkIn
            """)
    boolean existsConflictingReservation(
            @Param("roomId") Long roomId,
            @Param("statuses") Collection<ReservationStatus> statuses,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut
    );
}
