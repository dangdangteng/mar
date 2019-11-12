package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.DecodeFileKidJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DecodeFileKidJobRepository extends Repository<DecodeFileKidJob, Long>, PagingAndSortingRepository<DecodeFileKidJob, Long> {
    @Override
    @Modifying(flushAutomatically = true)
    @Transactional(rollbackFor = Exception.class)
    DecodeFileKidJob save(DecodeFileKidJob decodeFileKidJob);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "update DecodeFileKidJob dfkj set " +
            "dfkj.state = case when :#{#state} is null then dfkj.state else :#{#state} end , " +
            "dfkj.startTime = case when :#{#data} is null then dfkj.startTime else :#{#data} end ," +
            "dfkj.exception = case when :#{#exception} is null then dfkj.exception else :#{#exception} end " +
            "where dfkj.id = :#{#id}", nativeQuery = false)
    int updateStartTimeById(@Param("state") int state, @Param("id") Long id, @Param("data") LocalDateTime data, @Param("exception") String exception);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "update DecodeFileKidJob dfkj set " +
            "dfkj.state = case when :#{#state} is null then dfkj.state else :#{#state} end , " +
            "dfkj.exception = case when :#{#exception} is null then dfkj.exception else :#{#exception} end , " +
            "dfkj.endTime = case when :#{#data} is null then dfkj.endTime else :#{#data} end " +
            "where dfkj.id = :#{#id}", nativeQuery = false)
    int updateEndTimeById(@Param("state") int state, @Param("id") Long id, @Param("data") LocalDateTime data);

    Optional<DecodeFileKidJob> findByStateNotAndTaskId(Integer state, Integer taskId);

    Optional<DecodeFileKidJob> findById(int id);

    Optional<DecodeFileKidJob> findByLevelAndStateAndTaskId(Integer levle, Integer state, Integer taskId);

    Page<DecodeFileKidJob> findByStateNot(int state, Pageable pageable);

    @Override
    Page<DecodeFileKidJob> findAll(Pageable pageable);
}
