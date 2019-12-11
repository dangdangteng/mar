package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.ErrorEntity;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;

public interface ErrorRepository extends Repository<ErrorEntity,Long>, PagingAndSortingRepository<ErrorEntity,Long> {
    ErrorEntity findById(long id);

    @Override
    ErrorEntity save(ErrorEntity errorEntity);

}
