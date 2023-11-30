package com.example.rmm.services.service;

import com.example.rmm.services.controller.dtos.FullServiceData;
import com.example.rmm.services.controller.dtos.SaveServiceRequest;
import com.example.rmm.services.controller.dtos.SaveServiceResponse;
import com.example.rmm.services.repository.Service;

// TODO remove public?
public final class ServiceMapper {

    private ServiceMapper() {}

    public static Service saveServiceRequestToService(final SaveServiceRequest request) {
        return new Service(request.name(), request.price());
    }

    public static SaveServiceResponse serviceToSaveServiceResponse(final Service service) {
        return SaveServiceResponse.builder().id(service.getId()).build();
    }

    public static FullServiceData serviceToFullServiceData(final Service service) {
        return FullServiceData.builder()
                              .id(service.getId())
                              .name(service.getName())
                              .price(service.getPrice())
                              .build();
    }
}
