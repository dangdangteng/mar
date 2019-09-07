package com.dinglicom.mr.entity.correlationdata;

import lombok.*;

import java.io.Serializable;

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
