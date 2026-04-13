package com.bookstore.book.service;

import com.bookstore.book.dto.BookFilterRequest;
import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.exception.BookNotFoundException;
import com.bookstore.book.exception.ForbiddenException;
import com.bookstore.book.model.Book;
import com.bookstore.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    public List<Book> getBooks(BookFilterRequest filter) {
        return bookRepository.findWithFilters(filter);
    }

    public Book getBookById(String id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book not found: " + id));
    }

    public List<Book> getFeaturedBooks() {
        return bookRepository.findFeatured(12);
    }

    public Book createBook(BookRequest request, String sellerId, String role) {
        Book book = mapRequestToBook(request);
        book.setSellerId(sellerId);
        book.setStatus(Book.BookStatus.ACTIVE);
        book.setCreatedAt(Instant.now());
        book.setUpdatedAt(Instant.now());

        // Admins can mark featured books
        if (!"ADMIN".equals(role)) {
            book.setFeatured(false);
        }

        Book saved = bookRepository.save(book);
        log.info("Book created: {} by seller {}", saved.getId(), sellerId);
        return saved;
    }

    public Book updateBook(String id, BookRequest request, String requesterId, String role) {
        Book existing = getBookById(id);

        // Only the seller who owns it (or an admin) can update
        if (!"ADMIN".equals(role) && !existing.getSellerId().equals(requesterId)) {
            throw new ForbiddenException("You don't own this listing");
        }

        Book updated = mapRequestToBook(request);
        updated.setId(id);
        updated.setSellerId(existing.getSellerId());
        updated.setCreatedAt(existing.getCreatedAt());
        updated.setUpdatedAt(Instant.now());
        updated.setAverageRating(existing.getAverageRating());
        updated.setTotalRatings(existing.getTotalRatings());

        if (!"ADMIN".equals(role)) {
            updated.setFeatured(existing.isFeatured());
        }

        return bookRepository.save(updated);
    }

    public void deleteBook(String id, String requesterId, String role) {
        Book existing = getBookById(id);
        if (!"ADMIN".equals(role) && !existing.getSellerId().equals(requesterId)) {
            throw new ForbiddenException("You don't own this listing");
        }
        bookRepository.deleteById(id);
        log.info("Book deleted: {}", id);
    }

    private Book mapRequestToBook(BookRequest req) {
        return Book.builder()
            .title(req.getTitle())
            .author(req.getAuthor())
            .isbn(req.getIsbn())
            .description(req.getDescription())
            .price(req.getPrice())
            .originalPrice(req.getOriginalPrice())
            .stock(req.getStock())
            .publisher(req.getPublisher())
            .publishedYear(req.getPublishedYear())
            .language(req.getLanguage())
            .pages(req.getPages())
            .category(req.getCategory())
            .grade(req.getGrade())
            .subject(req.getSubject())
            .stream(req.getStream())
            .coverImageUrl(req.getCoverImageUrl())
            .additionalImages(req.getAdditionalImages())
            .featured(req.isFeatured())
            .comingSoon(req.isComingSoon())
            .status(req.isComingSoon() ? Book.BookStatus.COMING_SOON : Book.BookStatus.ACTIVE)
            .averageRating(0.0)
            .totalRatings(0)
            .build();
    }
}
