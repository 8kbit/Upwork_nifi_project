package com.thoughtapps.droppoint.droppoint.task;

import com.thoughtapps.droppoint.droppoint.model.Task;

import java.util.List;

/**
 * Created by zaskanov on 04.04.2017.
 */
public interface TaskExecutor {

    //Return collection of new batch id
    List<Long> execute(Task task);
}
