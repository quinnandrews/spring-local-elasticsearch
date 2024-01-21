package io.github.quinnandrews.spring.local.elasticsearch.application.data.guitarpedals.repository;

import io.github.quinnandrews.spring.local.elasticsearch.application.data.guitarpedals.GuitarPedal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuitarPedalRepository extends JpaRepository<GuitarPedal, Long> {
}
