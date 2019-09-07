package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.TaskConfig;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface TaskConfigRepository extends Repository<TaskConfig,Long> , PagingAndSortingRepository<TaskConfig,Long> {
    Optional<TaskConfig> findById(long id);
}
