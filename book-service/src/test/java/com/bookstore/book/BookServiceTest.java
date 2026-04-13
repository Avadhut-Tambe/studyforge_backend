package com.bookstore.book;

import com.bookstore.book.dto.BookRequest;
import com.bookstore.book.exception.BookNotFoundException;
import com.bookstore.book.exception.ForbiddenException;
import com.bookstore.book.model.Book;
import com.bookstore.book.repository.BookRepository;
import com.bookstore.book.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookRepository bookRepository;

    @InjectMocks private BookService bookService;

    private Book sampleBook;
    private BookRequest sampleRequest;

    @BeforeEach
    void setUp() {
        sampleBook = Book.builder()
            .id("book-123")
            .title("NCERT Mathematics Class 10")
            .author("NCERT")
            .price(new BigDecimal("299.00"))
            .stock(50)
            .sellerId("seller-456")
            .status(Book.BookStatus.ACTIVE)
            .averageRating(4.5)
            .totalRatings(120)
            .build();

        sampleRequest = new BookRequest();
        sampleRequest.setTitle("New Book");
        sampleRequest.setAuthor("Author Name");
        sampleRequest.setPrice(new BigDecimal("199.00"));
        sampleRequest.setStock(30);
    }

    @Test
    void getBookById_returns_book_when_found() {
        when(bookRepository.findById("book-123")).thenReturn(Optional.of(sampleBook));

        Book result = bookService.getBookById("book-123");

        assertThat(result.getId()).isEqualTo("book-123");
        assertThat(result.getTitle()).isEqualTo("NCERT Mathematics Class 10");
    }

    @Test
    void getBookById_throws_when_not_found() {
        when(bookRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookById("missing"))
            .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void createBook_sets_sellerId_and_active_status() {
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Book created = bookService.createBook(sampleRequest, "seller-789", "SELLER");

        assertThat(created.getSellerId()).isEqualTo("seller-789");
        assertThat(created.getStatus()).isEqualTo(Book.BookStatus.ACTIVE);
        assertThat(created.isFeatured()).isFalse();  // sellers can't self-feature
    }

    @Test
    void createBook_seller_cannot_set_featured() {
        sampleRequest.setFeatured(true);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Book created = bookService.createBook(sampleRequest, "seller-789", "SELLER");

        assertThat(created.isFeatured()).isFalse();  // overridden for non-admin
    }

    @Test
    void createBook_admin_can_set_featured() {
        sampleRequest.setFeatured(true);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Book created = bookService.createBook(sampleRequest, "admin-1", "ADMIN");

        assertThat(created.isFeatured()).isTrue();
    }

    @Test
    void updateBook_allows_owner_to_update() {
        when(bookRepository.findById("book-123")).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Book updated = bookService.updateBook("book-123", sampleRequest, "seller-456", "SELLER");

        assertThat(updated.getTitle()).isEqualTo("New Book");
        verify(bookRepository).save(any());
    }

    @Test
    void updateBook_throws_for_non_owner() {
        when(bookRepository.findById("book-123")).thenReturn(Optional.of(sampleBook));

        assertThatThrownBy(() ->
            bookService.updateBook("book-123", sampleRequest, "other-seller", "SELLER")
        ).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void updateBook_allows_admin_to_update_any_book() {
        when(bookRepository.findById("book-123")).thenReturn(Optional.of(sampleBook));
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() ->
            bookService.updateBook("book-123", sampleRequest, "admin-1", "ADMIN")
        ).doesNotThrowAnyException();
    }

    @Test
    void deleteBook_throws_for_non_owner() {
        when(bookRepository.findById("book-123")).thenReturn(Optional.of(sampleBook));

        assertThatThrownBy(() ->
            bookService.deleteBook("book-123", "other-seller", "SELLER")
        ).isInstanceOf(ForbiddenException.class);

        verify(bookRepository, never()).deleteById(any());
    }

    @Test
    void deleteBook_succeeds_for_owner() {
        when(bookRepository.findById("book-123")).thenReturn(Optional.of(sampleBook));
        doNothing().when(bookRepository).deleteById("book-123");

        assertThatCode(() ->
            bookService.deleteBook("book-123", "seller-456", "SELLER")
        ).doesNotThrowAnyException();

        verify(bookRepository).deleteById("book-123");
    }

    @Test
    void comingSoon_book_gets_coming_soon_status() {
        sampleRequest.setComingSoon(true);
        when(bookRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Book created = bookService.createBook(sampleRequest, "seller-789", "SELLER");

        assertThat(created.getStatus()).isEqualTo(Book.BookStatus.COMING_SOON);
        assertThat(created.isComingSoon()).isTrue();
    }
}
