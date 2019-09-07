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
import java.util.Optional;

public interface ReportKidJobRepository extends Repository<ReportKidJob, Long>, PagingAndSortingRepository<ReportKidJob, Long> {
    Optional<ReportKidJob> findById(long id);

    List<ReportKidJob> findByStateAndTaskId(int state, int taskId);
    /**
     * 单查state 为state 轮循,没有进行过retrycount 的
     */
    List<ReportKidJob> findByStateAndRetryCount(int state, int retryCount);

    @Override
    ReportKidJob save(ReportKidJob reportKidJob);

    @Modifying
    @Transactional
    @Query(value = "update ReportKidJob sfjk set " +
            "sfjk.state = case when :#{#state} is null then sfjk.state else :#{#state} end , " +
            "sfjk.startTime = case when :#{#startTime} is null then sfjk.startTime else :#{#startTime} end " +
            "where sfjk.id = :#{#id}" ,nativeQuery = false)
    int updateSourceFileKidJobStateAndStartTimeById(@Param("state")int state, @Param("id") int id, @Param("startTime") LocalDateTime startTime);

    @Modifying
    @Transactional
    @Query(value = "update ReportKidJob sfjk set " +
            "sfjk.state = case when :#{#state} is null then sfjk.state else :#{#state} end , " +
            "sfjk.endTime = case when :#{#endTime} is null then sfjk.endTime else :#{#endTime} end " +
            "where sfjk.id = :#{#id}" ,nativeQuery = false)
    int updateSourceFileKidJobStateAndEndTimeById(@Param("state")int state, @Param("id") int id, @Param("endTime") LocalDateTime endTime);

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
}
