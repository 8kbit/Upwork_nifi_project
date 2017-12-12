package com.thoughtapps.droppoint.droppoint.schedule;

import org.apache.commons.lang.time.DateUtils;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.Date;

/**
 * Created by zaskanov on 07.04.2017.
 */
public abstract class AbstractConfigScheduledJob implements SchedulingConfigurer, Runnable {

    protected abstract int getIntervalSec();

    protected boolean startImmediately = true;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.addTriggerTask(this, new Trigger() {
            @Override
            public Date nextExecutionTime(TriggerContext triggerContext) {
                Date lastTime = triggerContext.lastActualExecutionTime();
                if (lastTime == null) {
                    if (startImmediately) return new Date();
                    else lastTime = new Date();
                }

                return DateUtils.addSeconds(lastTime, getIntervalSec());
            }
        });
    }
}
