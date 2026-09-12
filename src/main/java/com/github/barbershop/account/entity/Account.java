package com.github.barbershop.account.entity;

import com.github.barbershop.provision.entity.ProvisionBooking;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "telegram_id", nullable = false, unique = true)
    private Long telegramId;

    private String firstName;
    private String lastName;

    @Column(unique = true)
    private String username;

    private String description;

    private String photoUrl;

    @Column(unique = true)
    private String instagram;

    @Column(unique = true)
    private String tiktok;

    @Column(unique = true)
    private String number;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private UserRole role;

    @OneToMany(mappedBy = "account", fetch = FetchType.LAZY)
    private List<ProvisionBooking> bookings;

    private LocalDateTime createdAt;
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
