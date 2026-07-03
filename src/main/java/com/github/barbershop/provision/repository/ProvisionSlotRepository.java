package com.github.barbershop.provision.repository;

import com.github.barbershop.provision.entity.ProvisionSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ProvisionSlotRepository extends JpaRepository<ProvisionSlot, Long> {
    boolean existsByProvisionIdAndStartTimeBetween(Long provisionId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT COUNT(s) > 0 FROM ProvisionSlot s " +
            "WHERE s.provision.id = :provisionId " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime")
    boolean existsOverlappingSlot(@Param("provisionId") Long provisionId,
                                  @Param("startTime") LocalDateTime startTime,
                                  @Param("endTime") LocalDateTime endTime);

}
