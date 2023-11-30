package com.example.rmm.services.repository;

import com.example.rmm.common.exceptions.NotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {

    default List<Service> getAllById(final Iterable<Long> ids) {
        final var services = findAllById(ids);
        if (services.isEmpty()) throw new NotFoundException("Service(s) with the given ID(s) not found");
        return services;
    }
}
