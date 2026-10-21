package com.academiaapp.backend.config;

import com.academiaapp.backend.admin.Admin;
import com.academiaapp.backend.user.Role;
import com.academiaapp.backend.user.User;
import com.academiaapp.backend.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, EntityManager entityManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
    }

    @Override
    @Transactional
    public void run(String... args) {
        String testEmail = "admin@academiaapp.com";

        if (userRepository.findByEmail(testEmail).isPresent()) {
            return;
        }

        User user = new User();
        user.setEmail(testEmail);
        user.setPasswordHash(passwordEncoder.encode("admin123"));
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        Admin admin = new Admin();
        admin.setUser(user);
        admin.setName("Administrador de Teste");
        entityManager.persist(admin);
    }
}