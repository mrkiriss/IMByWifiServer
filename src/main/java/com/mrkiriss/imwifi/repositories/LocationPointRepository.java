package com.mrkiriss.imwifi.repositories;


import com.mrkiriss.imwifi.entity.LocationPoint;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Transactional
@RepositoryRestResource(collectionResourceRel = "locationPoints", path = "locationPoints")
public interface LocationPointRepository extends CrudRepository<LocationPoint, Long>{

    @Query(value = "SELECT lp FROM location_point lp WHERE (SELECT COUNT(*) FROM ((SELECT mac FROM lp.access_point) INTERSECT (:apNames)))>2", nativeQuery = true)
    List<LocationPoint> findAllSuitableByMacCount(@Param("apNames") List<String> apNames);
}