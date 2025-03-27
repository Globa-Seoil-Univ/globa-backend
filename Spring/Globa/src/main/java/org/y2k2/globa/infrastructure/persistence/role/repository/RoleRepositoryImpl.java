package org.y2k2.globa.infrastructure.persistence.role.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.domain.role.repository.RoleRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class RoleRepositoryImpl implements RoleRepository {
    private final RoleJpaRepository roleJpaRepository;

    @Override
    public Optional<RoleEntity> getRole(String name) {
        return roleJpaRepository.findByName(name);
    }
}
