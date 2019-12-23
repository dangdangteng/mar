package com.dinglicom.mr.concurrent;

import com.dinglicom.mr.entity.TaskConfigEntity;
import com.dinglicom.mr.repository.TaskConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.Callable;

public class taskFindById implements Callable<Optional<TaskConfigEntity>> {

    private Logger logger = LoggerFactory.getLogger(taskFindById.class);

    long id;
    TaskConfigRepository taskConfigRepository;

    public taskFindById(long id, TaskConfigRepository taskConfigRepository) {
        this.id = id;
        this.taskConfigRepository = taskConfigRepository;
    }

    @Override
    public Optional<TaskConfigEntity> call() throws Exception {
        Optional<TaskConfigEntity> byId = taskConfigRepository.findById(id);
        return byId;
    }
}
