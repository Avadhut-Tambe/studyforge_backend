package com.bookstore.book.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    private String  id;
    private String  bookId;
    private String  userId;
    private String  userName;
    private int     rating;     // 1-5
    private String  comment;
    private Instant createdAt;
}
