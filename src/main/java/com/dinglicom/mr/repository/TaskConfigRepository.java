package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.TaskConfigEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface TaskConfigRepository extends Repository<TaskConfigEntity,Long> , PagingAndSortingRepository<TaskConfigEntity,Long> {
    Optional<TaskConfigEntity> findById(long id);
}
