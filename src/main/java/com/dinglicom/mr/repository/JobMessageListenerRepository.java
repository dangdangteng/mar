package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.JobMessageListenerEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface JobMessageListenerRepository extends CrudRepository<JobMessageListenerEntity, Long>, Repository<JobMessageListenerEntity, Long> {

    @Override
    JobMessageListenerEntity save(JobMessageListenerEntity jobMessageListenerEntity);

    @Override
    Optional<JobMessageListenerEntity> findById(Long along);

    Optional<JobMessageListenerEntity> findByIworkIpAndIworkPort(String iWorkIp, Integer iWorkPort);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "update JobMessageListenerEntity jmle set " +
            "jmle.jobMessage = case when :#{#jobMessage} is null then jmle.jobMessage else :#{#jobMessage} end " +
            "where jmle.iworkIp = :#{#iWorkIp} AND jmle.iworkPort = :#{#iWorkPort}", nativeQuery = false)
    int updateJobMessageListenerEntity(@Param("iWorkIp") String iWorkIp, @Param("iWorkPort") Integer iWorkPort, @Param("jobMessage") String jobMessage);

    void deleteByIworkIpAndIworkPort(String iWorkIp, Integer iWorkPort);
}
