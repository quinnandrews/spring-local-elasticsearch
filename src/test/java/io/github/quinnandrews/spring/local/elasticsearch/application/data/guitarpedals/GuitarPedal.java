package io.github.quinnandrews.spring.local.elasticsearch.application.data.guitarpedals;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class GuitarPedal {

    @Id
    @Column(name = "id",
            columnDefinition = "BIGINT",
            nullable = false,
            updatable = false)
    private Long id;

    @Column(name = "name",
            columnDefinition = "VARCHAR(63)",
            nullable = false)
    private String name;

    public GuitarPedal() {
        // no-op
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
