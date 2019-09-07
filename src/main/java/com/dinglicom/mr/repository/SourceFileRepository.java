package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.SourceFile;
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


public interface SourceFileRepository extends Repository<SourceFile, Integer>, PagingAndSortingRepository<SourceFile, Integer> {
    Optional<SourceFile> findSourceFileById(int id);

    Optional<SourceFile> findSourceFileByFilename(String filename);

    Optional<SourceFile> findSourceFileByIndexId(int indexid);

    @Override
    List<SourceFile> findAll();

    @Override
    Page<SourceFile> findAll(Pageable pageable);

    Page<SourceFile> findAllByStatus(int status, Pageable pageable);

    @Modifying
    @Transactional
    @Query("update SourceFile sf set sf.status = 2, sf.startDt = :#{#data} where sf.id = :#{#id}")
    int updateStartTimeById(@Param("id") int id, @Param("data") LocalDateTime data);

    @Modifying
    @Transactional
    @Query("update SourceFile sf set sf.status = 4, sf.endDt = :#{#data} where sf.id = :#{#id}")
    int updateStatusAndEndDtByID(@Param("id") int id, @Param("data") LocalDate data);

    @Modifying
    @Transactional
    @Query("update SourceFile sf set " +
            "sf.groupId = CASE WHEN :#{#sourceFile.groupId} IS NULL THEN sf.groupId ELSE :#{#sourceFile.groupId} END ," +
            "sf.deviceId = case when :#{#sourceFile.groupId} is null then sf.deviceId else :#{#sourceFile.deviceId} end ," +
            "sf.testPointId = case when :#{#sourceFile.testPointId} is null then sf.testPointId else :#{#sourceFile.testPointId} end ," +
            "sf.indexId = case when :#{#sourceFile.indexId} is null then sf.indexId else :#{#sourceFile.indexId} end ," +
            "sf.fileSize = case when :#{#sourceFile.fileSize} is null then sf.fileSize else :#{#sourceFile.fileSize} end ," +
            "sf.filePathName = case when :#{#sourceFile.filePathName} is null then sf.filePathName else :#{#sourceFile.filePathName} end ," +
            "sf.systemServiceId = case when :#{#sourceFile.systemServiceId} is null then sf.systemServiceId else :#{#sourceFile.systemServiceId} end ," +
            "sf.creator = case when :#{#sourceFile.creator} is null then sf.creator else :#{#sourceFile.creator} end ," +
            "sf.createDt = case when :#{#sourceFile.createDt} is null then sf.createDt else :#{#sourceFile.createDt} end ," +
            "sf.status = case when :#{#sourceFile.status} is null then sf.status else :#{#sourceFile.status} end ," +
            "sf.tag = case when :#{#sourceFile.tag} is null then sf.tag else :#{#sourceFile.tag} end ," +
            "sf.dataType = case when :#{#sourceFile.dataType} is null then sf.dataType else :#{#sourceFile.dataType} end ," +
            "sf.deviceType = case when :#{#sourceFile.deviceType} is null then sf.deviceType else :#{#sourceFile.deviceType} end ," +
            "sf.validateResult = case when :#{#sourceFile.validateResult} is null then sf.validateResult else :#{#sourceFile.validateResult} end ," +
            "sf.isDel = case when :#{#sourceFile.isDel} is null then sf.isDel else :#{#sourceFile.isDel} end ," +
            "sf.testInformation = case when :#{#sourceFile.testInformation} is null then sf.testInformation else :#{#sourceFile.testInformation} end ," +
            "sf.workItemId = case when :#{#sourceFile.workItemId} is null then sf.workItemId else :#{#sourceFile.workItemId} end ," +
            "sf.startDt = case when :#{#sourceFile.startDt} is null then sf.startDt else :#{#sourceFile.startDt} end ," +
            "sf.endDt = case when :#{#sourceFile.endDt} is null then sf.endDt else :#{#sourceFile.endDt} end ," +
            "sf.filenameAlias = case when :#{#sourceFile.filenameAlias} is null then sf.filenameAlias else :#{#sourceFile.filenameAlias} end ," +
            "sf.port = case when :#{#sourceFile.port} is null then sf.port else :#{#sourceFile.port} end ," +
            "sf.filename = case when :#{#sourceFile.filename} is null then sf.filename else :#{#sourceFile.filename} end ," +
            "sf.isSkipped = case when :#{#sourceFile.isSkipped} is null then sf.isSkipped else :#{#sourceFile.isSkipped} end ," +
            "sf.tagProtocol = case when :#{#sourceFile.tagProtocol} is null then sf.tagProtocol else :#{#sourceFile.tagProtocol} end , " +
            "sf.decodeFileCount = case when :#{#sourceFile.decodeFileCount} is null then sf.decodeFileCount else :#{#sourceFile.decodeFileCount} end , " +
            "sf.decodeServiceId = case when :#{#sourceFile.decodeServiceId} is null then sf.decodeServiceId else :#{#sourceFile.decodeServiceId} end , " +
            "sf.testplanVersion = case when :#{#sourceFile.testplanVersion} is null then sf.testplanVersion else :#{#sourceFile.testplanVersion} end , " +
            "sf.isAnalyzed = case when :#{#sourceFile.isAnalyzed} is null then sf.isAnalyzed else :#{#sourceFile.isAnalyzed} end , " +
            "sf.isAbnormal = case when :#{#sourceFile.isAbnormal} is null then sf.isAbnormal else :#{#sourceFile.isAbnormal} end , " +
            "sf.abnormalCh = case when :#{#sourceFile.abnormalCh} is null then sf.abnormalCh else :#{#sourceFile.abnormalCh} end , " +
            "sf.abnormalEn = case when :#{#sourceFile.abnormalEn} is null then sf.abnormalEn else :#{#sourceFile.abnormalEn} end , " +
            "sf.testplanName = case when :#{#sourceFile.testplanName} is null then sf.testplanName else :#{#sourceFile.testplanName} end , " +
            "sf.originalFileHash = case when :#{#sourceFile.originalFileHash} is null then sf.originalFileHash else :#{#sourceFile.originalFileHash} end , " +
            "sf.roadId = case when :#{#sourceFile.roadId} is null then sf.roadId else :#{#sourceFile.roadId} end , " +
            "sf.postCompensation = case when :#{#sourceFile.postCompensation} is null then sf.postCompensation else :#{#sourceFile.postCompensation} end , " +
            "sf.vendor = case when :#{#sourceFile.vendor} is null then sf.vendor else :#{#sourceFile.vendor} end , " +
            "sf.testScenario = case when :#{#sourceFile.testScenario} is null then sf.testScenario else :#{#sourceFile.testScenario} end , " +
            "sf.testType = case when :#{#sourceFile.testType} is null then sf.testType else :#{#sourceFile.testType} end , " +
            "sf.tester = case when :#{#sourceFile.tester} is null then sf.tester else :#{#sourceFile.tester} end , " +
            "sf.namingRuleId = case when :#{#sourceFile.namingRuleId} is null then sf.namingRuleId else :#{#sourceFile.namingRuleId} end , " +
            "sf.cellId = case when :#{#sourceFile.cellId} is null then sf.cellId else :#{#sourceFile.cellId} end , " +
            "sf.metroId = case when :#{#sourceFile.metroId} is null then sf.metroId else :#{#sourceFile.metroId} end , " +
            "sf.metroTimeTable = case when :#{#sourceFile.metroTimeTable} is null then sf.metroTimeTable else :#{#sourceFile.metroTimeTable} end  " +
            "where sf.id = :#{#sourceFile.id}")
    int update(@Param("sourceFile") SourceFile sourceFile);

    int deleteById(int id);

    Optional<SourceFile> findByFilePathNameAndPort(int port, String filePathName);

    @Modifying
    @Transactional
    @Query(value = "update SourceFile sf set " +
            "sf.status = case when :#{#status} is null then sf.status else :#{#status} end , " +
            "sf.startDt = case when :#{#data} is null then sf.startDt else :#{#data} end " +
            "where sf.id = :#{#id}" ,nativeQuery = false)
    int updateStartTimeById(@Param("status")int status,@Param("id") int id, @Param("data") LocalDateTime data);

    @Modifying
    @Transactional
    @Query(value = "update SourceFile sf set " +
            "sf.status = case when :#{#status} is null then sf.status else :#{#status} end , " +
            "sf.endDt = case when :#{#data} is null then sf.endDt else :#{#data} end " +
            "where sf.id = :#{#id}", nativeQuery = false)
    int updateStatusAndEndDtById(@Param("status") int status, @Param("id") int id, @Param("data") LocalDateTime data);

}
