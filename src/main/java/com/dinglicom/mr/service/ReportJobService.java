package com.dinglicom.mr.service;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.pojo.dto.ReportDto;
import com.dingli.cloudunify.pojo.entity.report.ReportTask;
import com.dingli.domain.StatisticalReportRequest;
import com.dingli.fillReport.FillReport;
import com.dingli.merger.Merger;
import com.dingli.simplifyStatistics.SimplifyStatistics;
import com.dinglicom.mr.Enum.StatusEnum;
import com.dinglicom.mr.entity.ReportKidJob;
import com.dinglicom.mr.entity.TaskConfig;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.feign.CloudunifyAdminFeign;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import com.dinglicom.mr.repository.TaskConfigRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.util.TaskUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Log
@Service
public class ReportJobService{

    private Logger logger = LoggerFactory.getLogger(ReportJobService.class);

    @Autowired
    private TaskConfigRepository taskConfigRepository;

    @Autowired
    private RabbitProducer rabbitProducer;

    @Autowired
    private ReportKidJobRepository reportKidJobRepository;

    @Autowired
    private CloudunifyAdminFeign cloudunifyAdminFeign;

    @Autowired
    private CloudUnifyRedisFeign cloudUnifyRedisFeign;
    /**
     * 1级任务
     *
     * @param reportDto
     * @param id
     * @param priority
     * @return
     * @throws Exception
     */
    @Async
    public MessageCode reportJobI(ReportDto reportDto, int id, Integer priority) throws Exception {
        Optional<TaskConfig> byId = taskConfigRepository.findById(300);
        log.info("=============" + id);
        /**
         * 一级任务
         */
        List<StatisticalReportRequest> elementAttribute = TaskUtil.getStatisticalReportRequest(byId.get(), reportDto, id);
        List<String> reportI = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        if (elementAttribute == null) {
            return new MessageCode(0, "数据有误！");
        }
        log.info("*elementAttribute : " + elementAttribute.toString() + "-----------");
        List errorList = new ArrayList();
        Response response1 = cloudUnifyRedisFeign.addNum(elementAttribute.size()+"", id + "IA");
        elementAttribute.stream().forEach(elementAttribute2 -> {
            if (StringUtils.isEmpty(elementAttribute2.getItemAttributeVal().getFileName())) {
                log.info("数据源ddib文件为空");
                errorList.add(elementAttribute2.toString());
                return;
            }
            reportI.add(elementAttribute2.getCommonAttributeVal().getResultFile());
            SimplifyStatistics simplifyStatistics = new SimplifyStatistics();
            log.info(elementAttribute2.toString() + "----" + elementAttribute2.getCommonAttributeVal().toString() + "--------" + elementAttribute2.getItemAttributeVal().toString());
            ArrayList simplifyList = new ArrayList();
            Boolean simplifyStatisticRequestFile = null;
            try {
                simplifyStatisticRequestFile = simplifyStatistics.getSimplifyStatisticRequestFile(elementAttribute2, simplifyList);
            } catch (Exception e) {
                log.info("调超强任务接口失败!"+e.toString());
            }
            if (simplifyStatisticRequestFile) {
                simplifyList.parallelStream().forEach(o -> {
                    try {
                        ReportKidJob reportKidJob = new ReportKidJob();
                        reportKidJob.setLevel(1);
                        reportKidJob.setState(1);
                        reportKidJob.setException(StatusEnum.getMessage(1));
                        reportKidJob.setRetryCount(0);
                        reportKidJob.setReturnValue(o + "");
                        reportKidJob.setStartTime(System.currentTimeMillis());
                        reportKidJob.setEndTime(null);
                        reportKidJob.setTaskId(id);
                        reportKidJob.setResponse(elementAttribute2.getCommonAttributeVal().getResultFile());
                        String s = mapper.writeValueAsString(reportKidJob);
                        Response response = cloudUnifyRedisFeign.addString(s, id + "|report1");
                        log.info("SimplifyStatistics 入库成功 ：...." + response.toString() + "gg");
                        AllObject allObject = new AllObject();
                        allObject.setId("REPORT-IA|" + response.getData().toString());
                        allObject.setPort(reportDto.getData().get(1).getPort());
                        allObject.setFilePathName(reportDto.getData().get(1).getFileName());
                        allObject.setObj(o + ":" + "REPORT-IA|" + response.getData().toString());
                        allObject.setNum(0);
                        log.info(allObject.toString() + "这个对象是空吗");
                        rabbitProducer.send(allObject, allObject.getObj(), priority == null ? 50 : priority, true);
                        log.info("消息压入队列: ...");
                    } catch (Exception e) {
                        log.info("I 级别任务出错 ：" + e.getMessage() + e.toString());
                    }
                });
            } else {
                return;
            }
        });
        /**
         * 校验1级任务的合法性
         */
        if (elementAttribute.size() == errorList.size()) {
            ReportKidJob reportKidJob = new ReportKidJob();
            reportKidJob.setException("没有读取到文件名称");
            reportKidJob.setStartTime(System.currentTimeMillis());
            reportKidJobRepository.save(reportKidJob);
            log.info("数据源文件存在问题...");
            return new MessageCode(0, "数据源文件存在问题...");
        }
        /**
         * 二级任务
         */
        StatisticalReportRequest elementAttributeForII = TaskUtil.getStatisticalReportRequestForII(byId.get(), reportI, reportDto, id);
        if (elementAttributeForII == null) {
            return new MessageCode(0, "数据有误！");
        }
        log.info("**elementAttributeForII :" + elementAttributeForII.toString());
        Merger merger = new Merger();
        ArrayList mergerList = new ArrayList();
        boolean mergerRequestFile = merger.getMergerRequestFile(elementAttributeForII, mergerList);
        ReportKidJob reportKidJob = new ReportKidJob();
        List jobList = new ArrayList();
        if (mergerRequestFile) {
            mergerList.stream().forEach(o -> {
                reportKidJob.setLevel(2);
                reportKidJob.setState(1);
                reportKidJob.setException(StatusEnum.getMessage(1));
                reportKidJob.setRetryCount(0);
                reportKidJob.setReturnValue(o + "");
                reportKidJob.setStartTime(System.currentTimeMillis());
                reportKidJob.setEndTime(null);
                reportKidJob.setResponse(elementAttributeForII.getCommonAttributeVal().getResultFile());
                reportKidJob.setTaskId(id);
//                ReportKidJob save = reportKidJobRepository.save(reportKidJob);
                try {
                    String s = mapper.writeValueAsString(reportKidJob);
                    Response<String> response = cloudUnifyRedisFeign.addString(s, id + "|report2");
                    log.info("merger 入库成功 ：...." + response.toString());
                } catch (Exception e) {
                    log.info("redis : " + e.getMessage());
                }
            });
        } else {
            return new MessageCode(0, elementAttributeForII.toString());
        }

        StatisticalReportRequest elementAttributeForIII = TaskUtil.getStatisticalReportRequestForIII(byId.get(), reportKidJob, reportDto, id);
        FillReport report = new FillReport();
        ArrayList reportList = new ArrayList();
        boolean fillRequestFile = report.getFillRequestFile(elementAttributeForIII, reportList);
        if (fillRequestFile) {
            reportList.parallelStream().forEach(o -> {
                ReportKidJob reportKidJob1 = new ReportKidJob();
                reportKidJob1.setLevel(3);
                reportKidJob1.setState(1);
                reportKidJob1.setException(StatusEnum.getMessage(1));
                reportKidJob1.setRetryCount(0);
                reportKidJob1.setReturnValue(o + "");
                reportKidJob1.setStartTime(System.currentTimeMillis());
                reportKidJob1.setEndTime(null);
                reportKidJob1.setResponse(elementAttributeForIII.getCommonAttributeVal().getResultFile());
                reportKidJob1.setTaskId(id);
                try {
                    Response<String> response = cloudUnifyRedisFeign.addString(mapper.writeValueAsString(reportKidJob1), id + "|report3");
                    log.info("FillReport 入库成功 ：...." + response.toString());
                } catch (Exception e) {
                    log.info("redis 3 :" + e.getMessage());
                }
            });
        }
//        }
        ReportTask reportTask = new ReportTask();
        reportTask.setId(id);
        reportTask.setStatus(2);
        reportTask.setStateInfo("1级任务已经压入队列");
        Response response = cloudunifyAdminFeign.updateReportTaskById(reportTask);
        return new MessageCode(1, reportTask.toString());
    }

    /**
     * 任务retry
     *
     * @param allObject
     * @throws Exception
     */
    public void retryReportI(AllObject allObject) throws Exception {
        int num = allObject.getNum();
        int i = num + 1;
        allObject.setNum(1);
        rabbitProducer.send(allObject, allObject.getObj(), 0);
    }

//    @Override
//    public Response jobDoing(String jsonObj) throws Exception {
//        return null;
//    }
}
