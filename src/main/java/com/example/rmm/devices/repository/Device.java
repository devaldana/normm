package com.example.rmm.devices.repository;

import com.example.rmm.common.persistence.BaseEntity;
import com.example.rmm.services.repository.Service;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.CascadeType.MERGE;
import static jakarta.persistence.CascadeType.PERSIST;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Device extends BaseEntity {

    private String systemName;
    private String type;
    private Long customerId;

    @ManyToMany(cascade = {PERSIST, MERGE})
    @JoinTable(
            name = "device_service",
            joinColumns = @JoinColumn(name = "device_id"),
            inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<Service> services = new HashSet<>();

    public Device(final String systemName, final String type, final Long customerId) {
        this.systemName = systemName;
        this.type = type;
        this.customerId = customerId;
    }

    public void addServices(final List<Service> newServices) {
        this.services.addAll(newServices);
    }

    public void removeServices(final List<Service> currentServices) {
        currentServices.forEach(this.services::remove);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o instanceof Device that) return this.id != null && this.id.equals(that.id);
        return false;
    }
}
