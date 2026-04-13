package com.bookstore.book.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BookRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    private String isbn;

    @Size(max = 2000, message = "Description too long")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stock;

    private String publisher;
    private String publishedYear;
    private String language;
    private int    pages;

    private String category;
    private String grade;
    private String subject;
    private String stream;

    private String       coverImageUrl;
    private List<String> additionalImages;

    private boolean featured;
    private boolean comingSoon;
}
