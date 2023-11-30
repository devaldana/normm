package com.example.rmm.services.repository;

import com.example.rmm.common.persistence.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
public class Service extends BaseEntity {

    private String name;
    private Float price;

    public Service(final String name, final Float price) {
        this.name = name;
        this.price = price;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o instanceof Service that) return this.id != null && this.id.equals(that.id);
        return false;
    }
}
