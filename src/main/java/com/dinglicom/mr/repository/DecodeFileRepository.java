package com.dinglicom.mr.repository;

import com.dinglicom.mr.entity.DecodeFileEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DecodeFileRepository extends Repository<DecodeFileEntity, Long>, PagingAndSortingRepository<DecodeFileEntity, Long>, CrudRepository<DecodeFileEntity, Long> {
    Optional<DecodeFileEntity> findAllById(int id);

    @Override
    DecodeFileEntity save(DecodeFileEntity entity);

    @Override
    <S extends DecodeFileEntity> Iterable<S> saveAll(Iterable<S> entities);

    DecodeFileEntity findById(int id);

    @Override
    List<DecodeFileEntity> findAll();

    @Override
    Page<DecodeFileEntity> findAll(Pageable pageable);

    @Modifying
    @Transactional(rollbackFor = Exception.class)
    @Query(value = "update DecodeFileEntity df set " +
            "df.endDt = case when :#{#endData} is null then df.endDt else :#{#endData} end ," +
            "df.startDt = case when :#{#startData} is null then df.endDt else :#{#startData} end ," +
            "df.importStatus = case when :#{#importStatus} is null then df.importStatus else :#{#importStatus} end " +
            "where df.id = :#{#id}" ,nativeQuery = false)
    int updateEndTimeById( @Param("id") int id, @Param("importStatus") Integer importStatus, @Param("endData") LocalDateTime endData,@Param("startData") LocalDateTime startData);

}
