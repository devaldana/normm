package com.example.rmm.services.service;

import com.example.rmm.services.controller.dtos.FullServiceData;
import com.example.rmm.services.controller.dtos.SaveServiceRequest;
import com.example.rmm.services.controller.dtos.SaveServiceResponse;
import com.example.rmm.services.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public SaveServiceResponse save(final SaveServiceRequest request) {
        final var service = ServiceMapper.saveServiceRequestToService(request);
        final var savedService = this.serviceRepository.save(service);
        return ServiceMapper.serviceToSaveServiceResponse(savedService);
    }

    public List<FullServiceData> findAll() {
        return this.serviceRepository.findAll()
                                     .stream()
                                     .map(ServiceMapper::serviceToFullServiceData)
                                     .toList();
    }

    public void delete(final Long id) {
        this.serviceRepository.deleteById(id);
    }
}
