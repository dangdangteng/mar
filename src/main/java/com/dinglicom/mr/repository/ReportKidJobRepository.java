package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.ReportKidJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface ReportKidJobRepository extends Repository<ReportKidJob, Long>, PagingAndSortingRepository<ReportKidJob, Long> {
    List<ReportKidJob> findAllById(long id);

    List<ReportKidJob> findByStateAndTaskId(int state, int taskId);
    /**
     * 单查state 为state 轮循,没有进行过retrycount 的
     */
    List<ReportKidJob> findByStateAndRetryCount(int state, int retryCount);

    @Override
    @Modifying
    @Transactional
    ReportKidJob save(ReportKidJob reportKidJob);

    @Modifying
    @Transactional
    @Query(value = "update ReportKidJob rkj set " +
            "rkj.state = case when :#{#state} is null then rkj.state else :#{#state} end , " +
            "rkj.startTime = case when :#{#startTime} is null then rkj.startTime else :#{#startTime} end ," +
            "rkj.exception = case when :#{#exception} is null then rkj.exception else :#{#exception} end " +
            "where rkj.id = :#{#id}", nativeQuery = false)
    int updateReportKidJobStateAndStartTimeById(@Param("state") int state,
                                                @Param("id") Long id,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("exception") String exception);

    @Modifying
    @Transactional
    @Query(value = "update ReportKidJob rkj set " +
            "rkj.state = case when :#{#state} is null then rkj.state else :#{#state} end , " +
            "rkj.endTime = case when :#{#endTime} is null then rkj.endTime else :#{#endTime} end ," +
            "rkj.response = case when :#{#response} is null then rkj.response else :#{#response} end ," +
            "rkj.exception = case when :#{#exception} is null then rkj.exception else :#{#exception} end " +
            "where rkj.id = :#{#id}", nativeQuery = false)
    int updateReportKidJobStateAndEndTimeById(@Param("state") int state,
                                              @Param("id") Long id,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("exception") String exception,
                                              @Param("response") String response);

    @Modifying
    @Transactional
    @Query(value = "update ReportKidJob rkj set " +
            "rkj.state = case when :#{#state} is null then rkj.state else :#{#state} end , " +
            "rkj.endTime = case when :#{#endTime} is null then rkj.endTime else :#{#endTime} end ," +
            "rkj.exception = case when :#{#exception} is null then rkj.exception else :#{#exception} end " +
            "where rkj.id = :#{#id}", nativeQuery = false)
    int updateReportKidJobStateAndEndTimeByIdA(@Param("state") int state,
                                              @Param("id") Long id,
                                              @Param("endTime") LocalDateTime endTime,
                                              @Param("exception") String exception);
    /**
     * 这个是根据taskid 父任务id 和 level级别 查任务入队列
     * @param taskId
     * @param level
     * @return
     */
    List<ReportKidJob> findByTaskIdAndLevel(int taskId , int level);

    void deleteById(long id);

    Page<ReportKidJob> findByState(int state, Pageable pageable);

    @Override
    Page<ReportKidJob> findAll(Pageable pageable);

    Page<ReportKidJob> findAllByStateNot(Integer state, Pageable pageable);

    List<ReportKidJob> findByTaskIdAndLevelAndStateNot(int taskId, int level, int state);
}
