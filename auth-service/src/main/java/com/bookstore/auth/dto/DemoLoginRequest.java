package com.bookstore.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DemoLoginRequest {

    /**
     * Desired demo role: BUYER, SELLER, or ADMIN (case-insensitive).
     */
    @NotBlank(message = "role is required")
    private String role;
}
