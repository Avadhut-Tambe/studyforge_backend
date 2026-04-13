package com.bookstore.book.repository;

import com.bookstore.book.dto.BookFilterRequest;
import com.bookstore.book.model.Book;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class BookRepository {

    private static final String COLLECTION = "books";

    private CollectionReference col() {
        return FirestoreClient.getFirestore().collection(COLLECTION);
    }

    public Book save(Book book) {
        try {
            Map<String, Object> data = toMap(book);
            if (book.getId() == null) {
                DocumentReference ref = col().add(data).get();
                book.setId(ref.getId());
                ref.update("id", ref.getId());
            } else {
                col().document(book.getId()).set(data).get();
            }
            return book;
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to save book", e);
        }
    }

    public Optional<Book> findById(String id) {
        try {
            DocumentSnapshot doc = col().document(id).get().get();
            if (!doc.exists()) return Optional.empty();
            return Optional.of(fromMap(doc.getId(), doc.getData()));
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to find book", e);
        }
    }

    public void deleteById(String id) {
        try {
            col().document(id).delete().get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to delete book", e);
        }
    }

    /**
     * Filtered book search.
     * Firestore has limited compound query support, so we apply server-side
     * filters where possible and in-memory filters for the rest.
     * For production scale, migrate search to Algolia or Elasticsearch.
     */
    public List<Book> findWithFilters(BookFilterRequest filter) {
        try {
            Query query = col().whereEqualTo("status", Book.BookStatus.ACTIVE.name());

            // Firestore supports equality/range on ONE field at a time without index magic.
            // We filter by category/grade/stream server-side and do the rest in memory.
            if (filter.getCategory()   != null) query = query.whereEqualTo("category", filter.getCategory());
            if (filter.getGrade()      != null) query = query.whereEqualTo("grade",    filter.getGrade());
            if (filter.getStream()     != null) query = query.whereEqualTo("stream",   filter.getStream());
            if (filter.getSellerId()   != null) query = query.whereEqualTo("sellerId", filter.getSellerId());
            if (Boolean.TRUE.equals(filter.getFeatured()))   query = query.whereEqualTo("featured",   true);
            if (Boolean.TRUE.equals(filter.getComingSoon())) query = query.whereEqualTo("comingSoon", true);

            QuerySnapshot snapshot = query.get().get();
            List<Book> books = snapshot.getDocuments().stream()
                .map(d -> fromMap(d.getId(), d.getData()))
                .collect(Collectors.toList());

            // In-memory filters
            return books.stream()
                .filter(b -> matchesTextQuery(b, filter.getQuery()))
                .filter(b -> matchesPriceRange(b, filter.getMinPrice(), filter.getMaxPrice()))
                .filter(b -> matchesRating(b, filter.getMinRating()))
                .filter(b -> filter.getSubject() == null || filter.getSubject().equalsIgnoreCase(b.getSubject()))
                .sorted(buildComparator(filter.getSortBy(), filter.getSortDir()))
                .skip((long) filter.getPage() * filter.getSize())
                .limit(filter.getSize())
                .collect(Collectors.toList());

        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to query books", e);
        }
    }

    public List<Book> findFeatured(int limit) {
        try {
            QuerySnapshot snap = col()
                .whereEqualTo("featured", true)
                .whereEqualTo("status", Book.BookStatus.ACTIVE.name())
                .limit(limit)
                .get().get();
            return snap.getDocuments().stream()
                .map(d -> fromMap(d.getId(), d.getData()))
                .collect(Collectors.toList());
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Failed to find featured books", e);
        }
    }

    // -----------------------------------------------------------------------
    // Filter helpers
    // -----------------------------------------------------------------------

    private boolean matchesTextQuery(Book book, String query) {
        if (query == null || query.isBlank()) return true;
        String q = query.toLowerCase();
        return (book.getTitle()  != null && book.getTitle().toLowerCase().contains(q))
            || (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(q))
            || (book.getIsbn()   != null && book.getIsbn().toLowerCase().contains(q));
    }

    private boolean matchesPriceRange(Book book, BigDecimal min, BigDecimal max) {
        if (min != null && book.getPrice().compareTo(min) < 0) return false;
        if (max != null && book.getPrice().compareTo(max) > 0) return false;
        return true;
    }

    private boolean matchesRating(Book book, Double minRating) {
        if (minRating == null) return true;
        return book.getAverageRating() >= minRating;
    }

    private Comparator<Book> buildComparator(String sortBy, String sortDir) {
        Comparator<Book> comparator = switch (sortBy) {
            case "price"         -> Comparator.comparing(Book::getPrice);
            case "averageRating" -> Comparator.comparingDouble(Book::getAverageRating);
            case "title"         -> Comparator.comparing(b -> b.getTitle().toLowerCase());
            default              -> Comparator.comparing(Book::getCreatedAt,
                                       Comparator.nullsLast(Comparator.naturalOrder()));
        };
        return "asc".equalsIgnoreCase(sortDir) ? comparator : comparator.reversed();
    }

    // -----------------------------------------------------------------------
    // Mapping
    // -----------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Map<String, Object> toMap(Book b) {
        Map<String, Object> m = new HashMap<>();
        m.put("title",            b.getTitle());
        m.put("author",           b.getAuthor());
        m.put("isbn",             b.getIsbn());
        m.put("description",      b.getDescription());
        m.put("price",            b.getPrice()         != null ? b.getPrice().doubleValue()         : 0.0);
        m.put("originalPrice",    b.getOriginalPrice() != null ? b.getOriginalPrice().doubleValue() : 0.0);
        m.put("stock",            b.getStock());
        m.put("publisher",        b.getPublisher());
        m.put("publishedYear",    b.getPublishedYear());
        m.put("language",         b.getLanguage());
        m.put("pages",            b.getPages());
        m.put("category",         b.getCategory());
        m.put("grade",            b.getGrade());
        m.put("subject",          b.getSubject());
        m.put("stream",           b.getStream());
        m.put("coverImageUrl",    b.getCoverImageUrl());
        m.put("additionalImages", b.getAdditionalImages());
        m.put("averageRating",    b.getAverageRating());
        m.put("totalRatings",     b.getTotalRatings());
        m.put("sellerId",         b.getSellerId());
        m.put("status",           b.getStatus() != null ? b.getStatus().name() : Book.BookStatus.ACTIVE.name());
        m.put("featured",         b.isFeatured());
        m.put("comingSoon",       b.isComingSoon());
        m.put("createdAt",        b.getCreatedAt() != null ? b.getCreatedAt().toEpochMilli() : Instant.now().toEpochMilli());
        m.put("updatedAt",        Instant.now().toEpochMilli());
        return m;
    }

    @SuppressWarnings("unchecked")
    private Book fromMap(String id, Map<String, Object> m) {
        return Book.builder()
            .id(id)
            .title((String) m.get("title"))
            .author((String) m.get("author"))
            .isbn((String) m.get("isbn"))
            .description((String) m.get("description"))
            .price(m.get("price") != null ? BigDecimal.valueOf(((Number) m.get("price")).doubleValue()) : BigDecimal.ZERO)
            .originalPrice(m.get("originalPrice") != null ? BigDecimal.valueOf(((Number) m.get("originalPrice")).doubleValue()) : null)
            .stock(m.get("stock") != null ? ((Number) m.get("stock")).intValue() : 0)
            .publisher((String) m.get("publisher"))
            .publishedYear((String) m.get("publishedYear"))
            .language((String) m.get("language"))
            .pages(m.get("pages") != null ? ((Number) m.get("pages")).intValue() : 0)
            .category((String) m.get("category"))
            .grade((String) m.get("grade"))
            .subject((String) m.get("subject"))
            .stream((String) m.get("stream"))
            .coverImageUrl((String) m.get("coverImageUrl"))
            .additionalImages((List<String>) m.get("additionalImages"))
            .averageRating(m.get("averageRating") != null ? ((Number) m.get("averageRating")).doubleValue() : 0.0)
            .totalRatings(m.get("totalRatings") != null ? ((Number) m.get("totalRatings")).intValue() : 0)
            .sellerId((String) m.get("sellerId"))
            .status(m.get("status") != null ? Book.BookStatus.valueOf((String) m.get("status")) : Book.BookStatus.ACTIVE)
            .featured(Boolean.TRUE.equals(m.get("featured")))
            .comingSoon(Boolean.TRUE.equals(m.get("comingSoon")))
            .createdAt(m.get("createdAt") != null ? Instant.ofEpochMilli(((Number) m.get("createdAt")).longValue()) : null)
            .updatedAt(m.get("updatedAt") != null ? Instant.ofEpochMilli(((Number) m.get("updatedAt")).longValue()) : null)
            .build();
    }
}
