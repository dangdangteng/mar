package com.dinglicom.mr.entity;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ToString
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "report_kid_job")
public class ReportKidJobEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long taskId;

    private Integer level;

    private Integer state;

    private Long startTime;

    private Long endTime;

    private Integer retryCount;

    private String exception;

    private String returnValue;

    private String response;

    private String data;
}
