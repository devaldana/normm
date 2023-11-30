package com.example.rmm.devices.controller.dtos;

import com.example.rmm.services.controller.dtos.FullServiceData;
import lombok.Builder;

import java.util.List;

@Builder
public record FullDeviceData(
        Long id,
        String systemName,
        String type,
        Long customerId,
        Double servicesCost,
        List<FullServiceData> services
) {}
