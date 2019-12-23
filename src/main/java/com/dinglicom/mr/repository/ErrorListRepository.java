package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.ErrorListEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ErrorListRepository extends Repository<ErrorListEntity,Integer>, CrudRepository<ErrorListEntity,Integer> {
    List<ErrorListEntity> findAll();
    ErrorListEntity save(ErrorListEntity errorListEntity);
}
