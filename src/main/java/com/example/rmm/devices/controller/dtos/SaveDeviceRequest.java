package com.example.rmm.devices.controller.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record SaveDeviceRequest(
        @NotBlank(message = "The System Name is required")
        String systemName,

        @NotNull(message = "The Type is required")
        Type type,

        @NotNull(message = "The Customer ID is required")
        @Positive(message = "The Customer ID is invalid")
        Long customerId
) {}
