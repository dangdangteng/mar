package com.dinglicom.mr.entity.correlationdata;

import lombok.*;

import java.io.Serializable;

/**
 * 常住内存,非我本意,设计者要求
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class AllObject implements Serializable {
    private String id;
    private String filePathName;
    private Integer port;
    private String obj;
    private int num;
}
