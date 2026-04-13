package com.bookstore.book.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Query parameters for filtering the book catalog.
 * All fields are optional; null means "no filter on this field".
 */
@Data
public class BookFilterRequest {

    private String     query;          // Free-text search (title, author, ISBN)
    private String     category;
    private String     grade;
    private String     subject;
    private String     stream;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Double     minRating;
    private Boolean    featured;
    private Boolean    comingSoon;
    private String     sellerId;

    // Pagination
    private int  page  = 0;
    private int  size  = 20;
    private String sortBy  = "createdAt";    // title | price | averageRating | createdAt
    private String sortDir = "desc";         // asc | desc
}
