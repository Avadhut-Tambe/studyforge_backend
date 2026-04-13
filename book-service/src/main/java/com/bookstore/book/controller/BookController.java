package com.bookstore.book.controller;

import com.bookstore.book.dto.BookFilterRequest;
import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.model.Book;
import com.bookstore.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Book REST controller.
 * Identity headers (X-User-Id, X-User-Role) are injected by the API Gateway after JWT validation.
 */
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    // -----------------------------------------------------------------------
    // Public endpoints
    // -----------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<List<Book>> getBooks(
        @RequestParam(required = false) String query,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String grade,
        @RequestParam(required = false) String subject,
        @RequestParam(required = false) String stream,
        @RequestParam(required = false) BigDecimal minPrice,
        @RequestParam(required = false) BigDecimal maxPrice,
        @RequestParam(required = false) Double minRating,
        @RequestParam(required = false) Boolean featured,
        @RequestParam(required = false) Boolean comingSoon,
        @RequestParam(defaultValue = "0")  int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy,
        @RequestParam(defaultValue = "desc")       String sortDir
    ) {
        BookFilterRequest filter = new BookFilterRequest();
        filter.setQuery(query);
        filter.setCategory(category);
        filter.setGrade(grade);
        filter.setSubject(subject);
        filter.setStream(stream);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setMinRating(minRating);
        filter.setFeatured(featured);
        filter.setComingSoon(comingSoon);
        filter.setPage(page);
        filter.setSize(size);
        filter.setSortBy(sortBy);
        filter.setSortDir(sortDir);

        return ResponseEntity.ok(bookService.getBooks(filter));
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Book>> getFeatured() {
        return ResponseEntity.ok(bookService.getFeaturedBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Book> getBook(@PathVariable String id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    // -----------------------------------------------------------------------
    // Seller / Admin endpoints — protected by gateway JWT filter
    // -----------------------------------------------------------------------

    @PostMapping
    public ResponseEntity<Book> createBook(
        @Valid @RequestBody BookRequest request,
        @RequestHeader("X-User-Id")   String userId,
        @RequestHeader("X-User-Role") String role
    ) {
        Book created = bookService.createBook(request, userId, role);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(
        @PathVariable String id,
        @Valid @RequestBody BookRequest request,
        @RequestHeader("X-User-Id")   String userId,
        @RequestHeader("X-User-Role") String role
    ) {
        return ResponseEntity.ok(bookService.updateBook(id, request, userId, role));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBook(
        @PathVariable String id,
        @RequestHeader("X-User-Id")   String userId,
        @RequestHeader("X-User-Role") String role
    ) {
        bookService.deleteBook(id, userId, role);
        return ResponseEntity.ok(Map.of("message", "Book deleted successfully"));
    }

    // Seller's own books
    @GetMapping("/seller/my-books")
    public ResponseEntity<List<Book>> getMyBooks(
        @RequestHeader("X-User-Id") String userId
    ) {
        BookFilterRequest filter = new BookFilterRequest();
        filter.setSellerId(userId);
        filter.setSize(100);
        return ResponseEntity.ok(bookService.getBooks(filter));
    }
}
