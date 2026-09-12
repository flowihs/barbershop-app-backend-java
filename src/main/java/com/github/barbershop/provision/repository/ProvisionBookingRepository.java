package com.github.barbershop.provision.repository;

import com.github.barbershop.provision.entity.ProvisionBooking;
import com.github.barbershop.provision.entity.ProvisionSlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProvisionBookingRepository extends JpaRepository<ProvisionBooking, Long> {
    Optional<ProvisionBooking> findByProvisionSlot(ProvisionSlot provisionSlot);
}
