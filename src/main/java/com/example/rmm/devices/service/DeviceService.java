package com.example.rmm.devices.service;

import com.example.rmm.devices.controller.dtos.*;
import com.example.rmm.devices.repository.DeviceRepository;
import com.example.rmm.services.controller.dtos.FullServiceData;
import com.example.rmm.services.repository.ServiceRepository;
import com.example.rmm.services.service.ServiceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private static final Set<Long> DEFAULT_DEVICES_BASE_SERVICES_IDS = Set.of(1L);

    private final LocalCache cache;
    private final DeviceRepository deviceRepository;
    private final ServiceRepository serviceRepository;

    public SaveDeviceResponse save(final SaveDeviceRequest request) {
        // Get the base services for the device given its type
        final var baseServicesIds = getBaseServiceIdsByDeviceType(request.type());
        final var deviceBaseServices = this.serviceRepository.findAllById(baseServicesIds);

        // Map from DTO to JPA entity, and add the device base services
        final var device = DeviceMapper.saveDeviceRequestToDevice(request);
        device.addServices(deviceBaseServices);

        // Persist the device, map from JPA entity to the response DTO and return
        final var savedDevice = this.deviceRepository.save(device);

        // Update the cache asynchronously
        this.cache.put(
                savedDevice.getId(),
                () -> deviceBaseServices.stream().mapToDouble(service -> service.getPrice()).sum()
        );

        // Return mapped DTO
        return DeviceMapper.deviceToSaveDeviceResponse(savedDevice);
    }

    public void modifyDeviceServices(final Long id, final ModifyDeviceServicesRequest request) {
        final var device = this.deviceRepository.findWithId(id);
        final var services = this.serviceRepository.getAllById(request.servicesIds());
        switch (request.action()) {
            case ADD -> device.addServices(services);
            case REMOVE -> {
                // Exclude device base services from the list of services to be removed
                final var baseServicesIds = getBaseServiceIdsByDeviceType(Type.valueOf(device.getType()));
                final var filteredServices = services.stream()
                                                     .filter(service -> !baseServicesIds.contains(service.getId()))
                                                     .toList();
                // Remove services from the device
                device.removeServices(filteredServices);
            }
        }

        // Save device with updated services
        log.debug("Saving device - thread: " + Thread.currentThread().getName());
        this.deviceRepository.save(device);

        // Update the cache asynchronously
        this.cache.put(id, () -> device.getServices().stream().mapToDouble(service -> service.getPrice()).sum());
    }

    public FullDeviceData findById(final Long id) {
        // Find the device with the given ID
        final var device = this.deviceRepository.findWithId(id);

        // Map the device services to their DTOs
        final var services = device.getServices()
                                   .stream()
                                   .map(ServiceMapper::serviceToFullServiceData)
                                   .toList();

        // Get the device services calculated cost
        final var cost = getDeviceServicesCost(id, services);

        // Return the full device data
        return DeviceMapper.deviceToFullDeviceData(device, services, cost);
    }

    public List<BasicDeviceData> findAll() {
        return this.deviceRepository.findAll()
                                    .stream()
                                    .map(DeviceMapper::deviceToBasicDeviceData)
                                    .toList();
    }

    public void delete(final Long id) {
        this.deviceRepository.deleteById(id);
        this.cache.remove(id);
    }

    private double getDeviceServicesCost(final Long id, final List<FullServiceData> services) {
        return this.cache.get(id, () -> services.stream().mapToDouble(FullServiceData::price).sum());
    }

    private Set<Long> getBaseServiceIdsByDeviceType(final Type type) {
        return switch (type) {
            // In the future each device type can have its own base service(s)
            case WINDOWS_WORKSTATION, LINUX, MAC, WINDOWS_SERVER -> DEFAULT_DEVICES_BASE_SERVICES_IDS;
        };
    }
}
