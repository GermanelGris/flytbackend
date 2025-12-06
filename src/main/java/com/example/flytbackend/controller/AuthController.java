package com.example.flytbackend.controller;

import com.example.flytbackend.controller.dto.LoginRequest;
import com.example.flytbackend.controller.dto.RegisterRequest;
import com.example.flytbackend.entity.Cliente;
import com.example.flytbackend.repository.ClienteRepository;
import com.example.flytbackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Datos incompletos"));
        }

        if (clienteRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "El correo ya está registrado"));
        }

        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getFono());
        if (request.getFechaNacimiento() != null) {
            cliente.setFechaNacimiento(request.getFechaNacimiento());
        }
        cliente.setContrasena(passwordEncoder.encode(request.getPassword()));

        // Asignar roles según lo que recibas en el request
        Set<String> rolesRequest = request.getRoles();
        if (rolesRequest == null || rolesRequest.isEmpty()) {
            cliente.setRoles("CLIENTE");
        } else {
            // Guardar sin el prefijo ROLE_ en la BD, se añade en CustomUserDetailsService
            cliente.setRoles(String.join(",", rolesRequest).toUpperCase());
        }

        clienteRepository.save(cliente);

        String token = jwtUtil.generateToken(cliente.getEmail(), cliente.getId());

        List<String> rolesList = Arrays.stream(cliente.getRoles().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Usuario registrado correctamente");
        body.put("token", token);
        body.put("id", cliente.getId());
        body.put("email", cliente.getEmail());
        body.put("roles", rolesList);

        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Cliente> clienteOpt = clienteRepository.findByEmail(request.getEmail());
        if (clienteOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciales inválidas"));
        }

        Cliente cliente = clienteOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), cliente.getContrasena())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciales inválidas"));
        }

        String token = jwtUtil.generateToken(cliente.getEmail(), cliente.getId());

        List<String> rolesList = Arrays.stream(
                        Optional.ofNullable(cliente.getRoles()).orElse("")
                                .split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Login exitoso");
        body.put("token", token);
        body.put("id", cliente.getId());
        body.put("nombre", cliente.getNombre());
        body.put("apellido", cliente.getApellido());
        body.put("email", cliente.getEmail());
        body.put("roles", rolesList);

        return ResponseEntity.ok(body);
    }
}
