package com.sentinel.common.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Pattern: Authenticator (L5_Auth-hash)
 * Represents a registered user in the system.
 * Password is hashed using Scrypt (as mandated by Prof. Tramontana).
 *
 * Pattern: RBAC (L3_RoleBasedAC)
 * The 'role' field maps the user to a predefined role (ADMIN, ANALYST).
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * AuthenticationInfo (L5_Auth-hash):
     * The hash is generated via SCryptUtil.scrypt(passwd, 32768, 8, 1)
     * and verified via SCryptUtil.check(passwd, passwordHash).
     * The salt is embedded within the hash output automatically.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Pattern: RBAC (L3_RoleBasedAC)
     * Predefined roles: ADMIN, ANALYST
     */
    @Column(name = "role", nullable = false, length = 20)
    private String role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public User(String username, String passwordHash, String role) {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    @PrePersist
    private void prePersist() {
        if (this.id == null) {
            this.id = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }
}
