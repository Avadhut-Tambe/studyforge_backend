package com.bookstore.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * User domain model stored in Firestore under the "users" collection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String id;           // Firestore document ID
    private String email;
    private String passwordHash;
    private String name;
    private String phone;
    private Role   role;
    private Status status;
    private String avatarUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public enum Role {
        BUYER, SELLER, ADMIN
    }

    public enum Status {
        ACTIVE, INACTIVE, PENDING_APPROVAL, SUSPENDED
    }
}
