package com.github.barbershop.provision.repository;

import com.github.barbershop.provision.entity.Provision;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProvisionRepository extends JpaRepository<Provision, Long> {

}
