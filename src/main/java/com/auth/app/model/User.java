package com.auth.app.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private UUID id;
    @Column(name = "user_email", unique = true, length = 300)
    private String email;
    @Column(name = "user_name", length = 500)
    private String name;
    private String password;
    private  String image;
    private boolean enable = true;
    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
//    private String gender;
//    private Address address;

    @Enumerated(EnumType.STRING)
    private Provider provider = Provider.LOCAL;
    private Set<Role> roles = new HashSet<>();
}
