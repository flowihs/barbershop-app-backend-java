package com.github.barbershop.provision.entity;

import com.github.barbershop.category.entity.Category;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "provisions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Provision {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private float rating;

    @ManyToOne
    private Category category;
}
