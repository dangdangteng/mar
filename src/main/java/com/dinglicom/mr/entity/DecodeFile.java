package com.dinglicom.mr.entity;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
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
public class DecodeFile implements Serializable {


    private static final long serialVersionUID = 4511671010896401436L;

    /**
     * DecodeFileID,主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    /**
     * 分组ID
     */
    private Integer groupId;

    /**
     * 源文件ID
     */
    private Integer sourceFileId;

    /**
     * 设备模型
     */
    private String deviceModel;

    /**
     * 设备端口号
     */
    private Integer port;

    /**
     * 端口模块类型
     */
    private String portDeviceType;

    /**
     * 数据开始时间第一个采样点的有效时间
     */
    private LocalDateTime startDt;

    /**
     * 数据结束时间
     */
    private LocalDateTime endDt;

    /**
     * 经纬度左坐标
     */
    private Double coordLeft;

    /**
     * 经纬度上坐标
     */
    private Double coordTop;

    /**
     * 经纬度右坐标
     */
    private Double coordRight;

    /**
     * 经纬度下坐标
     */
    private Double coordBottom;

    /**
     * 文件路径名称全路径
     */
    private String filePathName;

    /**
     * 系统服务ID
     */
    private Integer systemServiceId;

    /**
     * 设备网络类型
     */
    private String deviceNetType;

    /**
     * 数据网络类型
     */
    private String dataNetTypes;

    /**
     * 国家移动号信息
     */
    private String mncMccInfo;

    /**
     * 测试总时长
     */
    private Integer totalTime;

    /**
     * 测试总距离
     */
    private Double totalDistance;

    /**
     * 测试业务描述
     */
    private String testSummary;

    /**
     * 数据类型（室内室外室外定点）
     */
    private String dataType;

    /**
     * 写入记录创建时间
     */
    private LocalDateTime createDt;

    /**
     * 采样点总量
     */
    private Integer totalPointCount;

    /**
     * 采样点gps总量
     */
    private Integer totalGpsPointCount;

    private Boolean rcuPacketLenZero;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 设备端测试业务_csfb主叫
     */
    private Boolean csfbMoc;

    /**
     * 设备端测试业务csfb被叫
     */
    private Boolean csfbMot;

    /**
     * 设备端测试业务_volte主叫
     */
    private Boolean volteMoc;

    /**
     * 设备端测试业务_volte被叫
     */
    private Boolean volteMtc;

    /**
     * 是否已经删除
     */
    private Integer isDelete;

    /**
     * idm状态智能分析状态
     */
    private Integer idmStatus;

    /**
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 设备ID
     */
    private Integer deviceId;

    /**
     * 测试类型
     */
    private Integer testType;

    /**
     * 测试级别
     */
    private Integer testLevel;

    /**
     * 测试场景
     */
    private Integer testScenario;

    /**
     * 测试计划名称
     */
    private String testplanName;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 是否异常文件
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
     * 删除
     */
    private Integer msgLostCount;

    /**
     * 数据网络类型最高网络制式
     */
    private Integer dataNetType;

    /**
     * 区分nbiot或者cdma
     */
    private String technologys;

    /**
     * 区域id合集
     */
    private String areaIds;

    /**
     * 道路ID合集
     */
    private String roadIds;

    /**
     * 入库状态
     */
    private Integer importStatus;

    public DecodeFile(Integer id, Integer port, String filename) {
        this.id = id;
        this.port = port;
        this.fileName = filename;
    }

}
