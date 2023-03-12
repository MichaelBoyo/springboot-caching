package com.tericcabrel.springbootcaching.repositories;

import com.tericcabrel.springbootcaching.models.CacheData;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CacheDataRepository extends CrudRepository<CacheData, String> {
}
