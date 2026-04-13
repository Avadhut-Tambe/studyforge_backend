package com.bookstore.book.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Book domain model.
 * Stored in Firestore under the "books" collection.
 * Supports rich filtering: grade, subject, stream, price range, rating.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    private String      id;
    private String      title;
    private String      author;
    private String      isbn;
    private String      description;
    private BigDecimal  price;
    private BigDecimal  originalPrice;  // for showing discount
    private int         stock;
    private String      publisher;
    private String      publishedYear;
    private String      language;
    private int         pages;

    // Classification
    private String      category;       // e.g., "Textbook", "Reference", "Novel"
    private String      grade;          // e.g., "Class 10", "Class 12", "College"
    private String      subject;        // e.g., "Mathematics", "Physics"
    private String      stream;         // e.g., "Science", "Commerce", "Arts"

    // Media
    private String      coverImageUrl;
    private List<String> additionalImages;

    // Ratings
    private double      averageRating;
    private int         totalRatings;

    // Metadata
    private String      sellerId;
    private BookStatus  status;
    private boolean     featured;
    private boolean     comingSoon;

    private Instant     createdAt;
    private Instant     updatedAt;

    public enum BookStatus {
        ACTIVE, INACTIVE, OUT_OF_STOCK, COMING_SOON
    }
}
