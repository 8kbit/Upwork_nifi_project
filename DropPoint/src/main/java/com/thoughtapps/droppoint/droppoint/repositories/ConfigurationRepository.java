package com.thoughtapps.droppoint.droppoint.repositories;

import com.thoughtapps.droppoint.droppoint.model.Configuration;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by zaskanov on 04.04.2017.
 */
@Repository
public interface ConfigurationRepository extends CrudRepository<Configuration, Long> {
    Configuration findOneByKey(String key);
}
