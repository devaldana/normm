package com.example.rmm.services.controller.dtos;

import lombok.Builder;

@Builder
public record FullServiceData(
        Long id,
        String name,
        Float price
) {}
