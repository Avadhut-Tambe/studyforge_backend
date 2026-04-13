package com.bookstore.user.controller;

import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * User profile management: addresses, wishlist, seller approval (admin).
 * Identity is extracted from gateway headers.
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    private CollectionReference usersCol() {
        return FirestoreClient.getFirestore().collection("users");
    }

    private CollectionReference wishlistCol() {
        return FirestoreClient.getFirestore().collection("wishlists");
    }

    // -----------------------------------------------------------------------
    // Profile
    // -----------------------------------------------------------------------

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMyProfile(
        @RequestHeader("X-User-Id") String userId
    ) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = usersCol().document(userId).get().get();
        if (!doc.exists()) return ResponseEntity.notFound().build();
        Map<String, Object> data = sanitizeUser(doc.getData());
        return ResponseEntity.ok(data);
    }

    @PutMapping("/me")
    public ResponseEntity<Map<String, Object>> updateProfile(
        @RequestHeader("X-User-Id") String userId,
        @RequestBody Map<String, Object> updates
    ) throws ExecutionException, InterruptedException {
        // Only allow safe fields to be updated
        Map<String, Object> safe = new HashMap<>();
        List.of("name", "phone", "avatarUrl").forEach(field -> {
            if (updates.containsKey(field)) safe.put(field, updates.get(field));
        });
        safe.put("updatedAt", Instant.now().toEpochMilli());
        usersCol().document(userId).update(safe).get();
        return ResponseEntity.ok(Map.of("message", "Profile updated"));
    }

    // -----------------------------------------------------------------------
    // Wishlist
    // -----------------------------------------------------------------------

    @GetMapping("/me/wishlist")
    public ResponseEntity<List<String>> getWishlist(
        @RequestHeader("X-User-Id") String userId
    ) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = wishlistCol().document(userId).get().get();
        if (!doc.exists()) return ResponseEntity.ok(List.of());
        @SuppressWarnings("unchecked")
        List<String> bookIds = (List<String>) doc.getData().getOrDefault("bookIds", List.of());
        return ResponseEntity.ok(bookIds);
    }

    @PostMapping("/me/wishlist/{bookId}")
    public ResponseEntity<Map<String, String>> addToWishlist(
        @RequestHeader("X-User-Id") String userId,
        @PathVariable String bookId
    ) throws ExecutionException, InterruptedException {
        DocumentReference ref = wishlistCol().document(userId);
        DocumentSnapshot doc  = ref.get().get();

        List<String> bookIds = new ArrayList<>();
        if (doc.exists()) {
            @SuppressWarnings("unchecked")
            List<String> existing = (List<String>) doc.getData().getOrDefault("bookIds", List.of());
            bookIds.addAll(existing);
        }
        if (!bookIds.contains(bookId)) bookIds.add(bookId);
        ref.set(Map.of("userId", userId, "bookIds", bookIds,
                        "updatedAt", Instant.now().toEpochMilli())).get();
        return ResponseEntity.ok(Map.of("message", "Added to wishlist"));
    }

    @DeleteMapping("/me/wishlist/{bookId}")
    public ResponseEntity<Map<String, String>> removeFromWishlist(
        @RequestHeader("X-User-Id") String userId,
        @PathVariable String bookId
    ) throws ExecutionException, InterruptedException {
        DocumentReference ref = wishlistCol().document(userId);
        DocumentSnapshot doc  = ref.get().get();
        if (!doc.exists()) return ResponseEntity.ok(Map.of("message", "Not in wishlist"));

        @SuppressWarnings("unchecked")
        List<String> bookIds = new ArrayList<>((List<String>) doc.getData().getOrDefault("bookIds", List.of()));
        bookIds.remove(bookId);
        ref.update("bookIds", bookIds).get();
        return ResponseEntity.ok(Map.of("message", "Removed from wishlist"));
    }

    // -----------------------------------------------------------------------
    // Admin endpoints
    // -----------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(
        @RequestHeader("X-User-Role") String role
    ) throws ExecutionException, InterruptedException {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).build();
        QuerySnapshot snap = usersCol().get().get();
        List<Map<String, Object>> users = snap.getDocuments().stream()
            .map(d -> { Map<String, Object> m = sanitizeUser(d.getData()); m.put("id", d.getId()); return m; })
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/{userId}/status")
    public ResponseEntity<Map<String, String>> updateUserStatus(
        @PathVariable String userId,
        @RequestBody Map<String, String> body,
        @RequestHeader("X-User-Role") String role
    ) throws ExecutionException, InterruptedException {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(403).build();
        String status = body.get("status");
        usersCol().document(userId).update("status", status, "updatedAt", Instant.now().toEpochMilli()).get();
        return ResponseEntity.ok(Map.of("message", "Status updated to " + status));
    }

    // Remove sensitive fields before sending to client
    private Map<String, Object> sanitizeUser(Map<String, Object> data) {
        if (data == null) return new HashMap<>();
        Map<String, Object> safe = new HashMap<>(data);
        safe.remove("passwordHash");
        return safe;
    }
}
