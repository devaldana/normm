package com.example.rmm.services.controller;

import com.example.rmm.services.controller.dtos.FullServiceData;
import com.example.rmm.services.controller.dtos.SaveServiceRequest;
import com.example.rmm.services.controller.dtos.SaveServiceResponse;
import com.example.rmm.services.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/services")
public class ServiceController {

    private final ServiceService serviceService;

    @PostMapping
    public SaveServiceResponse save(@Valid @RequestBody final SaveServiceRequest request) {
        log.info("Save service - request received: {}", request);
        return this.serviceService.save(request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable final Long id) {
        log.info("Delete service - request received: {}", id);
        this.serviceService.delete(id);
    }

    @GetMapping
    public List<FullServiceData> findAll() {
        log.info("Find all services with full data - request received");
        return this.serviceService.findAll();
    }
}
