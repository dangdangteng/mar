package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.Error;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;

public interface ErrorRepository extends Repository<Error,Long>, PagingAndSortingRepository<Error,Long> {
    Error findById(long id);

    @Override
    Error save(Error error);

}
