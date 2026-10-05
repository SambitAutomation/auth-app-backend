package com.auth.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = )
public class Role {
    
    private UUID id = UUID.randomUUID();
    private String name;
}
