package com.github.barbershop.provision.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.barbershop.account.entity.Account;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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

    @Column
    private String avatar;

    @Column 
    private List<String> images;

    @ManyToOne
    private ProvisionCategory provisionCategory;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Account user;

    @OneToMany(mappedBy = "provision", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<ProvisionSlot> slots;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
