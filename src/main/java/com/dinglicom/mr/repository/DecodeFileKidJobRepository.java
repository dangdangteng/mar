package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.DecodeFileKidJob;
import jdk.nashorn.internal.runtime.regexp.joni.encoding.ObjPtr;
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
    DecodeFileKidJob save(DecodeFileKidJob decodeFileKidJob);

    @Modifying
    @Transactional
    @Query(value = "update DecodeFileKidJob dfkj set " +
            "dfkj.state = case when :#{#state} is null then dfkj.state else :#{#state} end , " +
            "dfkj.startTime = case when :#{#data} is null then dfkj.startTime else :#{#data} end " +
            "where dfkj.id = :#{#id}", nativeQuery = false)
    int updateStartTimeById(@Param("state") int state, @Param("id") int id, @Param("data") LocalDateTime data);

    @Modifying
    @Transactional
    @Query(value = "update DecodeFileKidJob dfkj set " +
            "dfkj.state = case when :#{#state} is null then dfkj.state else :#{#state} end , " +
            "dfkj.endTime = case when :#{#data} is null then dfkj.endTime else :#{#data} end " +
            "where dfkj.id = :#{#id}", nativeQuery = false)
    int updateEndTimeById(@Param("state") int state, @Param("id") int id, @Param("data") LocalDateTime data);

    DecodeFileKidJob findByStateNotAndTaskId(Integer state,Integer taskId);

    Optional<DecodeFileKidJob> findById(int id);
}
