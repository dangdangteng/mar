package com.dinglicom.mr.entity;

import lombok.*;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Table(name = "source_file")
public class SourceFileEntity implements Serializable {

    private static final long serialVersionUID = -6421339807294459819L;
    /**
     * 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分组ID
     */
    private Integer groupId;

    /**
     * 设备ID
     */
    private Integer deviceId;

    /**
     * 测试点ID cqt用
     */
    private Integer testPointId;

    /**
     * 文件索引ID
     */
    private Integer indexId;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 文件路径名称 全路径
     */
    private String filePathName;

    /**
     * 系统服务ID
     */
    private Integer systemServiceId;

    /**
     * 创建者，设备名或者用户名
     */
    private String creator;

    /**
     * 创建日期
     */
    private LocalDateTime createDt;

    /**
     * 解码状态
     */
    private Integer status;

    /**
     * 后续处理的json串，控制流程用
     */
    private String tag;

    /**
     * 数据类型 dt或者indoor
     */
    private String dataType;

    /**
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 验证文件结果
     */
    private Integer validateResult;

    /**
     * 是否删除
     */
    private Integer isDel;

    /**
     * 测试信息，回填用
     */
    private String testInformation;

    /**
     * 工单ID，walktour传上来
     */
    private String workItemId;

    /**
     * 开始时间
     */
    private LocalDateTime startDt;

    /**
     * 结束时间
     */
    private LocalDateTime endDt;

    /**
     * 文件名别名，gmcc用
     */
    private String filenameAlias;

    /**
     * 文件端口号
     */
    private Integer port;

    /**
     * 文件名
     */
    private String filename;

    /**
     * 是否跳过文件
     */
    private Boolean isSkipped;

    /**
     * 传输协议
     */
    private Integer tagProtocol;

    /**
     * 解码文件数量
     */
    private Integer decodeFileCount;

    /**
     * 解码服务ID
     */
    private Integer decodeServiceId;

    /**
     * 测试计划 版本号，回写用
     */
    private Integer testplanVersion;

    /**
     * 已经分析过的文件
     */
    private Boolean isAnalyzed;

    /**
     * 是否为异常文件，结果显示在web端
     */
    private Boolean isAbnormal;

    /**
     * 中文异常
     */
    private String abnormalCh;

    /**
     * 英文异常
     */
    private String abnormalEn;

    /**
     * 测试计划名称
     */
    private String testplanName;

    /**
     * 原始文件hash
     */
    private Integer originalFileHash;

    /**
     * 道路ID
     */
    private Integer roadId;

    /**
     * 后补偿用，道路文件进行后补偿
     */
    private Integer postCompensation;

    /**
     * 厂家，是指测试厂家
     */
    private Integer vendor;

    /**
     * 测试场景
     */
    private Integer testScenario;

    /**
     * 测试类型
     */
    private Integer testType;

    /**
     * 测试人员
     */
    private String tester;

    /**
     * 命名规则ID
     */
    private Integer namingRuleId;

    /**
     * 小区ID
     */
    private Integer cellId;

    /**
     * 地铁ID
     */
    private Integer metroId;

    /**
     * 地铁时刻表
     */
    private String metroTimeTable;

    private String srcFilename;

    public SourceFileEntity(Long id, String filePathName, Integer port) {
        this.id = id;
        this.filePathName = filePathName;
        this.port = port;
    }
}
