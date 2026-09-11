package com.github.barbershop.provision.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provisions_reviews")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProvisionReview {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String description;
    private float rating;
}
