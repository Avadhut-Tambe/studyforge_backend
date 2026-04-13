package com.bookstore.auth.repository;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.firebase.cloud.FirestoreClient;
import com.bookstore.auth.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Firestore-backed repository for User entities.
 * Each user document lives at: users/{userId}
 */
@Slf4j
@Repository
public class UserRepository {

    private static final String COLLECTION = "users";

    private CollectionReference usersCollection() {
        Firestore db = FirestoreClient.getFirestore();
        return db.collection(COLLECTION);
    }

    public User save(User user) {
        try {
            Map<String, Object> data = toMap(user);

            if (user.getId() == null) {
                // New user — let Firestore generate the ID
                var docRef = usersCollection().add(data).get();
                user.setId(docRef.getId());
                // Store the id inside the document too for convenience
                docRef.update("id", docRef.getId());
            } else {
                usersCollection().document(user.getId()).set(data).get();
            }
            return user;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to save user", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try {
            QuerySnapshot snapshot = usersCollection()
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .get();

            if (snapshot.isEmpty()) return Optional.empty();
            return Optional.of(fromMap(snapshot.getDocuments().get(0).getId(),
                                      snapshot.getDocuments().get(0).getData()));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to find user by email", e);
        }
    }

    public Optional<User> findById(String id) {
        try {
            var doc = usersCollection().document(id).get().get();
            if (!doc.exists()) return Optional.empty();
            return Optional.of(fromMap(doc.getId(), doc.getData()));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to find user by id", e);
        }
    }

    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    // -----------------------------------------------------------------------
    // Mapping helpers
    // -----------------------------------------------------------------------

    private Map<String, Object> toMap(User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("email",        user.getEmail());
        map.put("passwordHash", user.getPasswordHash());
        map.put("name",         user.getName());
        map.put("phone",        user.getPhone());
        map.put("role",         user.getRole().name());
        map.put("status",       user.getStatus().name());
        map.put("avatarUrl",    user.getAvatarUrl());
        map.put("createdAt",    user.getCreatedAt() != null ? user.getCreatedAt().toEpochMilli() : Instant.now().toEpochMilli());
        map.put("updatedAt",    Instant.now().toEpochMilli());
        return map;
    }

    @SuppressWarnings("unchecked")
    private User fromMap(String id, Map<String, Object> data) {
        return User.builder()
            .id(id)
            .email((String) data.get("email"))
            .passwordHash((String) data.get("passwordHash"))
            .name((String) data.get("name"))
            .phone((String) data.get("phone"))
            .role(User.Role.valueOf((String) data.get("role")))
            .status(User.Status.valueOf((String) data.get("status")))
            .avatarUrl((String) data.get("avatarUrl"))
            .createdAt(data.get("createdAt") != null
                ? Instant.ofEpochMilli((Long) data.get("createdAt")) : null)
            .updatedAt(data.get("updatedAt") != null
                ? Instant.ofEpochMilli((Long) data.get("updatedAt")) : null)
            .build();
    }
}
