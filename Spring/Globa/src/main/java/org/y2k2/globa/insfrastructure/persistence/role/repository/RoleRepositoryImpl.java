package org.y2k2.globa.insfrastructure.persistence.role.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.insfrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.domain.role.RoleRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class RoleRepositoryImpl implements RoleRepository {
    private final RoleJpaRepository roleJpaRepository;

    @Override
    public Optional<RoleEntity> findByName(String name) {
        return roleJpaRepository.findByName(name);
    }
}
