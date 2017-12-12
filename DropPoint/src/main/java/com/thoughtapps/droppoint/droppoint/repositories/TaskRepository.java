package com.thoughtapps.droppoint.droppoint.repositories;

import com.thoughtapps.droppoint.core.dto.InstructionType;
import com.thoughtapps.droppoint.droppoint.model.Task;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by zaskanov on 04.04.2017.
 */
@Repository
public interface TaskRepository extends CrudRepository<Task, Long> {

    List<Task> findByType(InstructionType type);

    List<Task> findByNodeId(String nodeId);

}
