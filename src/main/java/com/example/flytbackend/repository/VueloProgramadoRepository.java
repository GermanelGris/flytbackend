package com.example.flytbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.flytbackend.entity.VueloProgramado;

public interface VueloProgramadoRepository extends JpaRepository<VueloProgramado, Integer> {
}