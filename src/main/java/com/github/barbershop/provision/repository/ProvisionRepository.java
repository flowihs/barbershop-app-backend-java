package com.github.barbershop.provision.repository;

import com.github.barbershop.provision.entity.Provision;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProvisionRepository extends JpaRepository<Provision, Long> {
    @EntityGraph(attributePaths = {"slots", "provisionCategory", "user"})
    List<Provision> findAll();

    @EntityGraph(attributePaths = {"slots", "provisionCategory", "user"})
    Optional<Provision> findById(Long id);

    Optional<Provision> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT p.user.id FROM Provision p WHERE p.id = :provisionId")
    Long findUserIdById(@Param("provisionId") Long provisionId);

    List<Provision> findTop5ByUserIdOrderByRatingDesc(Long userId);
}
