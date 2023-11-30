package com.example.rmm.devices.controller;

import com.example.rmm.devices.controller.dtos.*;
import com.example.rmm.devices.service.DeviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/devices")
public class DeviceController {

    private final DeviceService deviceService;

    @PostMapping
    public SaveDeviceResponse save(@Valid @RequestBody final SaveDeviceRequest request) {
        log.info("Save device - request received: {}", request);
        return this.deviceService.save(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final Long id) {
        log.info("Delete device - request received: {}", id);
        this.deviceService.delete(id);
    }

    @PostMapping("/{id}/services")
    public void saveDeviceServices(
            @PathVariable final Long id,
            @Valid @RequestBody final ModifyDeviceServicesRequest request
    ) {
        log.info("Save device services - request received: {}", request);
        this.deviceService.modifyDeviceServices(id, request);
    }

    @GetMapping("/{id}")
    public FullDeviceData findById(@PathVariable final Long id) {
        log.info("Find device with full data - request received: {}", id);
        return this.deviceService.findById(id);
    }

    @GetMapping
    public List<BasicDeviceData> findAll() {
        log.info("Find all devices with full data - request received");
        return this.deviceService.findAll();
    }
}
