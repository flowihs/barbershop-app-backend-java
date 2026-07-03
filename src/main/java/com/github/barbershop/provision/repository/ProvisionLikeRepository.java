package com.github.barbershop.provision.repository;

import com.github.barbershop.provision.entity.ProvisionLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProvisionLikeRepository extends JpaRepository<ProvisionLike, Long> {
    long countByProvisionId(Long provisionId);
    boolean existsByProvisionIdAndUserId(Long provisionId, Long userId);
    Optional<ProvisionLike> findByProvisionIdAndUserId(Long provisionId, Long userId);
}
