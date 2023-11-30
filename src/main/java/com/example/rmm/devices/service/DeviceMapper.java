package com.example.rmm.devices.service;

import com.example.rmm.devices.controller.dtos.BasicDeviceData;
import com.example.rmm.devices.controller.dtos.FullDeviceData;
import com.example.rmm.devices.controller.dtos.SaveDeviceRequest;
import com.example.rmm.devices.controller.dtos.SaveDeviceResponse;
import com.example.rmm.devices.repository.Device;
import com.example.rmm.services.controller.dtos.FullServiceData;

import java.util.List;

final class DeviceMapper {

    private DeviceMapper() {}

    public static Device saveDeviceRequestToDevice(final SaveDeviceRequest request) {
        return new Device(
                request.systemName(),
                request.type().name(),
                request.customerId()
        );
    }

    public static BasicDeviceData deviceToBasicDeviceData(final Device device) {
        return BasicDeviceData.builder()
                              .id(device.getId())
                              .systemName(device.getSystemName())
                              .type(device.getType())
                              .build();
    }

    public static FullDeviceData deviceToFullDeviceData(
            final Device device,
            final List<FullServiceData> services,
            final Double cost
    ) {
        return FullDeviceData.builder()
                             .id(device.getId())
                             .systemName(device.getSystemName())
                             .type(device.getType())
                             .customerId(device.getCustomerId())
                             .servicesCost(cost)
                             .services(services)
                             .build();
    }

    public static SaveDeviceResponse deviceToSaveDeviceResponse(final Device device) {
        return SaveDeviceResponse.builder().id(device.getId()).build();
    }
}
