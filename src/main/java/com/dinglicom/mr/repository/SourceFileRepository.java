package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.SourceFileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


public interface SourceFileRepository extends Repository<SourceFileEntity, Long>, PagingAndSortingRepository<SourceFileEntity, Long> {
    Optional<SourceFileEntity> findSourceFileById(Long id);

    Optional<SourceFileEntity> findSourceFileByFilename(String filename);

    Optional<SourceFileEntity> findSourceFileByIndexId(int indexid);

    @Override
    List<SourceFileEntity> findAll();

    @Override
    Page<SourceFileEntity> findAll(Pageable pageable);

    Page<SourceFileEntity> findAllByStatus(int status, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update SourceFileEntity sf set sf.status = 2, sf.startDt = :#{#data} where sf.id = :#{#id}")
    int updateStartTimeById(@Param("id") Long id, @Param("data") LocalDateTime data);

    @Modifying
    @Transactional
    @Query("update SourceFileEntity sf set sf.status = 4, sf.endDt = :#{#data} where sf.id = :#{#id}")
    int updateStatusAndEndDtByID(@Param("id") Long id, @Param("data") LocalDate data);

    @Modifying
    @Transactional
    @Query("update SourceFileEntity sf set " +
            "sf.groupId = CASE WHEN :#{#sourceFileEntity.groupId} IS NULL THEN sf.groupId ELSE :#{#sourceFileEntity.groupId} END ," +
            "sf.deviceId = case when :#{#sourceFileEntity.groupId} is null then sf.deviceId else :#{#sourceFileEntity.deviceId} end ," +
            "sf.testPointId = case when :#{#sourceFileEntity.testPointId} is null then sf.testPointId else :#{#sourceFileEntity.testPointId} end ," +
            "sf.indexId = case when :#{#sourceFileEntity.indexId} is null then sf.indexId else :#{#sourceFileEntity.indexId} end ," +
            "sf.fileSize = case when :#{#sourceFileEntity.fileSize} is null then sf.fileSize else :#{#sourceFileEntity.fileSize} end ," +
            "sf.filePathName = case when :#{#sourceFileEntity.filePathName} is null then sf.filePathName else :#{#sourceFileEntity.filePathName} end ," +
            "sf.systemServiceId = case when :#{#sourceFileEntity.systemServiceId} is null then sf.systemServiceId else :#{#sourceFileEntity.systemServiceId} end ," +
            "sf.creator = case when :#{#sourceFileEntity.creator} is null then sf.creator else :#{#sourceFileEntity.creator} end ," +
            "sf.createDt = case when :#{#sourceFileEntity.createDt} is null then sf.createDt else :#{#sourceFileEntity.createDt} end ," +
            "sf.status = case when :#{#sourceFileEntity.status} is null then sf.status else :#{#sourceFileEntity.status} end ," +
            "sf.tag = case when :#{#sourceFileEntity.tag} is null then sf.tag else :#{#sourceFileEntity.tag} end ," +
            "sf.dataType = case when :#{#sourceFileEntity.dataType} is null then sf.dataType else :#{#sourceFileEntity.dataType} end ," +
            "sf.deviceType = case when :#{#sourceFileEntity.deviceType} is null then sf.deviceType else :#{#sourceFileEntity.deviceType} end ," +
            "sf.validateResult = case when :#{#sourceFileEntity.validateResult} is null then sf.validateResult else :#{#sourceFileEntity.validateResult} end ," +
            "sf.isDel = case when :#{#sourceFileEntity.isDel} is null then sf.isDel else :#{#sourceFileEntity.isDel} end ," +
            "sf.testInformation = case when :#{#sourceFileEntity.testInformation} is null then sf.testInformation else :#{#sourceFileEntity.testInformation} end ," +
            "sf.workItemId = case when :#{#sourceFileEntity.workItemId} is null then sf.workItemId else :#{#sourceFileEntity.workItemId} end ," +
            "sf.startDt = case when :#{#sourceFileEntity.startDt} is null then sf.startDt else :#{#sourceFileEntity.startDt} end ," +
            "sf.endDt = case when :#{#sourceFileEntity.endDt} is null then sf.endDt else :#{#sourceFileEntity.endDt} end ," +
            "sf.filenameAlias = case when :#{#sourceFileEntity.filenameAlias} is null then sf.filenameAlias else :#{#sourceFileEntity.filenameAlias} end ," +
            "sf.port = case when :#{#sourceFileEntity.port} is null then sf.port else :#{#sourceFileEntity.port} end ," +
            "sf.filename = case when :#{#sourceFileEntity.filename} is null then sf.filename else :#{#sourceFileEntity.filename} end ," +
            "sf.isSkipped = case when :#{#sourceFileEntity.isSkipped} is null then sf.isSkipped else :#{#sourceFileEntity.isSkipped} end ," +
            "sf.tagProtocol = case when :#{#sourceFileEntity.tagProtocol} is null then sf.tagProtocol else :#{#sourceFileEntity.tagProtocol} end , " +
            "sf.decodeFileCount = case when :#{#sourceFileEntity.decodeFileCount} is null then sf.decodeFileCount else :#{#sourceFileEntity.decodeFileCount} end , " +
            "sf.decodeServiceId = case when :#{#sourceFileEntity.decodeServiceId} is null then sf.decodeServiceId else :#{#sourceFileEntity.decodeServiceId} end , " +
            "sf.testplanVersion = case when :#{#sourceFileEntity.testplanVersion} is null then sf.testplanVersion else :#{#sourceFileEntity.testplanVersion} end , " +
            "sf.isAnalyzed = case when :#{#sourceFileEntity.isAnalyzed} is null then sf.isAnalyzed else :#{#sourceFileEntity.isAnalyzed} end , " +
            "sf.isAbnormal = case when :#{#sourceFileEntity.isAbnormal} is null then sf.isAbnormal else :#{#sourceFileEntity.isAbnormal} end , " +
            "sf.abnormalCh = case when :#{#sourceFileEntity.abnormalCh} is null then sf.abnormalCh else :#{#sourceFileEntity.abnormalCh} end , " +
            "sf.abnormalEn = case when :#{#sourceFileEntity.abnormalEn} is null then sf.abnormalEn else :#{#sourceFileEntity.abnormalEn} end , " +
            "sf.testplanName = case when :#{#sourceFileEntity.testplanName} is null then sf.testplanName else :#{#sourceFileEntity.testplanName} end , " +
            "sf.originalFileHash = case when :#{#sourceFileEntity.originalFileHash} is null then sf.originalFileHash else :#{#sourceFileEntity.originalFileHash} end , " +
            "sf.roadId = case when :#{#sourceFileEntity.roadId} is null then sf.roadId else :#{#sourceFileEntity.roadId} end , " +
            "sf.postCompensation = case when :#{#sourceFileEntity.postCompensation} is null then sf.postCompensation else :#{#sourceFileEntity.postCompensation} end , " +
            "sf.vendor = case when :#{#sourceFileEntity.vendor} is null then sf.vendor else :#{#sourceFileEntity.vendor} end , " +
            "sf.testScenario = case when :#{#sourceFileEntity.testScenario} is null then sf.testScenario else :#{#sourceFileEntity.testScenario} end , " +
            "sf.testType = case when :#{#sourceFileEntity.testType} is null then sf.testType else :#{#sourceFileEntity.testType} end , " +
            "sf.tester = case when :#{#sourceFileEntity.tester} is null then sf.tester else :#{#sourceFileEntity.tester} end , " +
            "sf.namingRuleId = case when :#{#sourceFileEntity.namingRuleId} is null then sf.namingRuleId else :#{#sourceFileEntity.namingRuleId} end , " +
            "sf.cellId = case when :#{#sourceFileEntity.cellId} is null then sf.cellId else :#{#sourceFileEntity.cellId} end , " +
            "sf.metroId = case when :#{#sourceFileEntity.metroId} is null then sf.metroId else :#{#sourceFileEntity.metroId} end , " +
            "sf.metroTimeTable = case when :#{#sourceFileEntity.metroTimeTable} is null then sf.metroTimeTable else :#{#sourceFileEntity.metroTimeTable} end  " +
            "where sf.id = :#{#sourceFileEntity.id}")
    int update(@Param("sourceFileEntity") SourceFileEntity sourceFileEntity);

    void deleteById(Long id);

    Optional<SourceFileEntity> findByFilePathNameAndPort(int port, String filePathName);

    @Modifying
    @Transactional
    @Query(value = "update SourceFileEntity sf set " +
            "sf.status = case when :#{#status} is null then sf.status else :#{#status} end , " +
            "sf.startDt = case when :#{#data} is null then sf.startDt else :#{#data} end " +
            "where sf.id = :#{#id}" ,nativeQuery = false)
    int updateStartTimeById(@Param("status")int status,@Param("id") Long id, @Param("data") LocalDateTime data);

    @Modifying
    @Transactional
    @Query(value = "update SourceFileEntity sf set " +
            "sf.status = case when :#{#status} is null then sf.status else :#{#status} end , " +
            "sf.endDt = case when :#{#data} is null then sf.endDt else :#{#data} end " +
            "where sf.id = :#{#id}", nativeQuery = false)
    int updateStatusAndEndDtById(@Param("status") int status, @Param("id") Long id, @Param("data") LocalDateTime data);

}
