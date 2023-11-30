package com.example.rmm.devices.repository;

import com.example.rmm.common.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    default Device findWithId(final Long id){
        return findById(id).orElseThrow(() -> new NotFoundException("Device not found"));
    }
}
