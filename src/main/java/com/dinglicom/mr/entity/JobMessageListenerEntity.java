package com.dinglicom.mr.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@ToString
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "job_message_listener")
public class JobMessageListenerEntity implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String jobMessage;
    private String iworkIp;
    private Integer iworkPort;
    private Integer isDel;
    private String exception;
}
