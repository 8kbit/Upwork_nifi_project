package com.thoughtapps.droppoint.droppoint.unit;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import com.thoughtapps.droppoint.droppoint.AppConfig;

/**
 * Created by zaskanov on 06.04.2017.
 */
@TestPropertySource({"/application.properties", "/test.properties"})
@ContextConfiguration(classes = {AppConfig.class, TestAppConfig.class})
public class AbstractTransactionalTest extends AbstractTransactionalTestNGSpringContextTests {
}
