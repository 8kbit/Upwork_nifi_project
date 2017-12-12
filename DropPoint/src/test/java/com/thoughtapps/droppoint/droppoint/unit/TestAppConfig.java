package com.thoughtapps.droppoint.droppoint.unit;

import com.thoughtapps.droppoint.droppoint.schedule.PingerJob;
import com.thoughtapps.droppoint.droppoint.schedule.SshConnectionHolder;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Created by zaskanov on 04.04.2017.
 */
public class TestAppConfig {
    @Bean
    @Primary
    public PingerJob pinger() {
        return Mockito.spy(PingerJob.class);
    }

    @Bean
    @Primary
    public SshConnectionHolder sshConnectionHolder() {
        return Mockito.spy(SshConnectionHolder.class);
    }
}
