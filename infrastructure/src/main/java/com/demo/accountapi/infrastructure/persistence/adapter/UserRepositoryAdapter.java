package com.demo.accountapi.infrastructure.persistence.adapter;

import com.demo.accountapi.application.port.out.UserRepositoryPort;
import com.demo.accountapi.domain.model.Role;
import com.demo.accountapi.domain.model.User;
import com.demo.accountapi.infrastructure.persistence.entity.UserEntity;
import com.demo.accountapi.infrastructure.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepositoryPort {

    private final UserJpaRepository jpa;

    public UserRepositoryAdapter(UserJpaRepository jpa) { this.jpa = jpa; }

    @Override
    public User save(User user) {
        return toDomain(jpa.save(toEntity(user)));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpa.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }

    private UserEntity toEntity(User u) {
        UserEntity e = new UserEntity();
        e.setId(u.getId());
        e.setEmail(u.getEmail());
        e.setPasswordHash(u.getPasswordHash());
        e.setRole(UserEntity.RoleEntity.valueOf(u.getRole().name()));
        e.setCreatedAt(u.getCreatedAt());
        e.setEnabled(u.isEnabled());
        return e;
    }

    private User toDomain(UserEntity e) {
        return new User.Builder()
                .id(e.getId()).email(e.getEmail()).passwordHash(e.getPasswordHash())
                .role(Role.valueOf(e.getRole().name()))
                .createdAt(e.getCreatedAt()).enabled(e.isEnabled())
                .build();
    }
}
