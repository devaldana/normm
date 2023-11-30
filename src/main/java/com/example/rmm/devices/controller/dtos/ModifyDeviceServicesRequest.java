package com.example.rmm.devices.controller.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public record ModifyDeviceServicesRequest(
        @NotNull(message = "The service(s) ID(s) must be provided")
        @NotEmpty(message = "At least one service ID must be provided")
        Set<@NotNull(message = "Null IDs are not allowed") @Positive(message = "Invalid ID(s)") Long> servicesIds,

        @NotNull(message = "The Action is required")
        Action action
) {
        public enum Action { ADD, REMOVE }
}
