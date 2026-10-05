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

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles" ,
             joinColumns = @JoinColumn( name = "user_id"),
              inverseJoinColumns = @JoinColumn( name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();


    //Entity Life Cycle

    @PrePersist
    protected void onCreate(){
        if(createdAt == null) createdAt = Instant.now();
    }

    @PreUpdate
    protected  void onUpdate(){
        updatedAt = Instant.now();
    }
}
