package com.harsh.rentify.repository;

import com.harsh.rentify.entity.PaymentEntity;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {

    boolean existsByBookingId(Long bookingId);

    Optional<PaymentEntity> findByBookingId(Long bookingId);

    List<PaymentEntity> findAllByOrderByPaymentDateAsc();

    @Modifying(flushAutomatically = true)
    @Query("""
            delete from PaymentEntity p
            where p.booking.room.id = :roomId
            """)
    void deleteByBookingRoomId(@Param("roomId") Long roomId);

    @Query("""
            select sum(p.amount)
            from PaymentEntity p
            where p.booking.tenant.id = :tenantId
            """)
    BigDecimal sumAmountByTenantId(@Param("tenantId") Long tenantId);

    @Query("""
            select sum(p.amount)
            from PaymentEntity p
            where p.booking.room.host.id = :landlordId
            """)
    BigDecimal sumAmountByLandlordId(@Param("landlordId") Long landlordId);

    @Query("""
            select sum(p.amount)
            from PaymentEntity p
            """)
    BigDecimal sumAllAmounts();
}
