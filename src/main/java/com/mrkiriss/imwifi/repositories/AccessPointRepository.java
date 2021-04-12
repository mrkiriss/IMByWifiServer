package com.mrkiriss.imwifi.repositories;

import com.mrkiriss.imwifi.entity.AccessPoint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@RepositoryRestResource(collectionResourceRel = "accessPoints", path = "accessPoints")
public interface AccessPointRepository  extends CrudRepository<AccessPoint, Long> {
    Optional<AccessPoint> findById(Long id);
}
