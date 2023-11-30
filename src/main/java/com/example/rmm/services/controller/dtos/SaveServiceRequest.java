package com.example.rmm.services.controller.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaveServiceRequest(
        @NotBlank(message = "The Name is required")
        String name,

        @NotNull(message = "The Price is required")
        @Positive(message = "The Price should be greater than 0")
        Float price
) {}
