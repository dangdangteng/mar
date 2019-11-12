package com.dinglicom.mr.entity;

import lombok.*;

import java.util.List;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class JobType<T> {
    private Integer count;
    private List<T> job;
}

