package com.example.flytbackend.service;

import com.example.flytbackend.entity.Cliente;
import com.example.flytbackend.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Cliente cliente = clienteRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        // Convertir String "ADMIN" o "ADMIN,CLIENTE" de la BD a authorities con prefijo ROLE_
        List<GrantedAuthority> authorities = Arrays.stream(cliente.getRoles().split(","))
            .map(String::trim)
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
            .username(cliente.getEmail())
            .password(cliente.getContrasena())
            .authorities(authorities)
            .build();
    }
}