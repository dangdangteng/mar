package com.dinglicom.mr.entity;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "decode_file_kid_job")
public class DecodeFileKidJob implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer taskId;

    private Integer level;

    private Integer state;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer retryCount;

    private String exception;
}
