package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.droppoint.model.Configuration;
import com.thoughtapps.droppoint.droppoint.repositories.ConfigurationRepository;
import com.thoughtapps.droppoint.droppoint.service.ConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by zaskanov on 01.04.2017.
 */
@Test
public class ConfigurationServiceTest extends AbstractTransactionalTest {

    @Autowired
    ConfigurationService confService;

    @Autowired
    ConfigurationRepository confRepository;

    @Test(expectedExceptions = RuntimeException.class)
    public void testNotAllowedValueSave() {
        confService.setPropertyValue("testK", 100);
    }

    @Test
    public void testValueSaveAndRead() {
        Configuration configuration = Configuration.builder().key("testK").value("100").build();
        configuration = confRepository.save(configuration);

        String sValue = confService.getPropertyValue(configuration.getKey());
        assertEquals(configuration.getValue(), sValue);

        Integer iValue = confService.getIntPropertyValue(configuration.getKey());
        assertEquals(Integer.valueOf(configuration.getValue()), iValue);
    }
}
