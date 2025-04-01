package com.lucas.pizzaria.repository;

import com.lucas.pizzaria.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByNumeroCelular(String numeroCelular);
}