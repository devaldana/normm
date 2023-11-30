package com.example.rmm.devices.controller.dtos;

import lombok.Builder;

@Builder
public record BasicDeviceData(Long id, String systemName, String type) {}
