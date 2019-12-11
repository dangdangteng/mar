package com.dinglicom.mr.service;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.pojo.dto.ReportDto;
import com.dingli.cloudunify.pojo.entity.report.ReportTask;
import com.dingli.domain.StatisticalReportRequest;
import com.dingli.fillReport.FillReport;
import com.dingli.merger.Merger;
import com.dingli.simplifyStatistics.SimplifyStatistics;
import com.dinglicom.mr.Enum.StatusEnum;
import com.dinglicom.mr.entity.ReportKidJobEntity;
import com.dinglicom.mr.entity.TaskConfigEntity;
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
        Optional<TaskConfigEntity> byId = taskConfigRepository.findById(300);
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
        List<AllObject> successList = new ArrayList();
        Response response1 = cloudUnifyRedisFeign.addNum(elementAttribute.size() + "", id + "ALL");
        elementAttribute.stream().forEach(elementAttribute2 -> {
            if (StringUtils.isEmpty(elementAttribute2.getItemAttributeVal().getFileName())) {
                log.info("数据源ddib文件为空");
                errorList.add(elementAttribute2.toString());
                return;
            }
            reportI.add(elementAttribute2.getCommonAttributeVal().getResultFile());
            SimplifyStatistics simplifyStatistics = new SimplifyStatistics();
            log.info(elementAttribute2.toString() + "----" + elementAttribute2.getCommonAttributeVal().toString() + "--------" + elementAttribute2.getItemAttributeVal().toString());
            ArrayList<String> simplifyList = new ArrayList();
            Boolean simplifyStatisticRequestFile = null;
            try {
                simplifyStatisticRequestFile = simplifyStatistics.getSimplifyStatisticRequestFile(elementAttribute2, simplifyList);
            } catch (Exception e) {
                errorList.add(elementAttribute2.toString());
                log.info("调超强任务接口失败!" + e.toString());
            }
            String yi = simplifyList.get(0);
            if (simplifyStatisticRequestFile) {
                try {
                    ReportKidJobEntity reportKidJobEntity = new ReportKidJobEntity();
                    reportKidJobEntity.setLevel(1);
                    reportKidJobEntity.setState(1);
                    reportKidJobEntity.setException(StatusEnum.getMessage(1));
                    reportKidJobEntity.setRetryCount(0);
                    reportKidJobEntity.setReturnValue(yi);
                    reportKidJobEntity.setStartTime(System.currentTimeMillis());
                    reportKidJobEntity.setEndTime(null);
                    reportKidJobEntity.setTaskId((long) id);
                    reportKidJobEntity.setResponse(elementAttribute2.getCommonAttributeVal().getResultFile());
                    String s = mapper.writeValueAsString(reportKidJobEntity);
                    Response response = cloudUnifyRedisFeign.addString(s, id + "|report1");
                    log.info("SimplifyStatistics 入库成功 ：...." + response.toString());
                    AllObject allObject = new AllObject();
                    allObject.setId("REPORT-IA|" + response.getData().toString());
                    allObject.setPort(reportDto.getData().get(0).getPort());
                    allObject.setFilePathName(reportDto.getData().get(0).getFileName());
                    allObject.setObj(yi + ":" + "REPORT-IA|" + response.getData().toString());
                    allObject.setNum(0);
                    successList.add(allObject);
                } catch (Exception e) {
                    log.info("I 级别任务出错 ：" + e.getMessage() + e.toString());
                }
            }
        });
        log.info("下发任务实际数量::" + elementAttribute.size());
        log.info("失败的任务数量::" + errorList.size());
        log.info("真实任务成功数量::" + successList.size());
        /**
         * 校验1级任务的合法性
         */
        if ((errorList.size() / elementAttribute.size()) > 0.2) {
            ReportKidJobEntity reportKidJobEntity = new ReportKidJobEntity();
            reportKidJobEntity.setException("数据源文件存在问题");
            reportKidJobEntity.setStartTime(System.currentTimeMillis());
            reportKidJobRepository.save(reportKidJobEntity);
            log.info("数据源文件存在问题...");
            return new MessageCode(0, "数据源文件存在问题...");
        }
        Response response2 = cloudUnifyRedisFeign.addNum(successList.size() + "", id + "IA");
        Response response3 = cloudUnifyRedisFeign.addNum(errorList.size() + "", id + "F");
        successList.parallelStream().forEach(allObject -> {
            try {
                rabbitProducer.send(allObject, allObject.getObj(), priority == null ? 50 : priority, true);
            } catch (Exception e) {
                log.info("1级任务入队列失败!" + e.toString() + ";任务内容:" + allObject.toString());
            }
        });

        /**
         * 二级任务
         */
        StatisticalReportRequest elementAttributeForII = TaskUtil.getStatisticalReportRequestForII(byId.get(), reportI, reportDto, id);
        if (elementAttributeForII == null) {
            return new MessageCode(0, "数据有误！");
        }
        log.info("**elementAttributeForII :" + elementAttributeForII.toString());
        Merger merger = new Merger();
        ArrayList<String> mergerList = new ArrayList();
        boolean mergerRequestFile = merger.getMergerRequestFile(elementAttributeForII, mergerList);
        ReportKidJobEntity reportKidJobEntity = new ReportKidJobEntity();
        List jobList = new ArrayList();
        if (!mergerRequestFile) {
            return new MessageCode(0, elementAttributeForII.toString());
        }

        String er = mergerList.get(0);
        reportKidJobEntity.setLevel(2);
        reportKidJobEntity.setState(1);
        reportKidJobEntity.setException(StatusEnum.getMessage(1));
        reportKidJobEntity.setRetryCount(0);
        reportKidJobEntity.setReturnValue(er);
        reportKidJobEntity.setStartTime(System.currentTimeMillis());
        reportKidJobEntity.setEndTime(null);
        reportKidJobEntity.setResponse(elementAttributeForII.getCommonAttributeVal().getResultFile());
        reportKidJobEntity.setTaskId((long) id);
        try {
            String s = mapper.writeValueAsString(reportKidJobEntity);
            Response<String> response = cloudUnifyRedisFeign.addString(s, id + "|report2");
            log.info("merger 入库成功 ：...." + response.toString());
        } catch (Exception e) {
            log.info("redis : " + e.getMessage());
        }
        /*
            三级任务
         */
        StatisticalReportRequest elementAttributeForIII = TaskUtil.getStatisticalReportRequestForIII(byId.get(), reportKidJobEntity, reportDto, id);
        FillReport report = new FillReport();
        ArrayList<String> reportList = new ArrayList();
        boolean fillRequestFile = report.getFillRequestFile(elementAttributeForIII, reportList);
        if (!fillRequestFile) {
            return new MessageCode(0, "三级任务拆分失败fillrequestfile" + fillRequestFile);
        }

        String san = reportList.get(0);
        ReportKidJobEntity reportKidJobEntity1 = new ReportKidJobEntity();
        reportKidJobEntity1.setLevel(3);
        reportKidJobEntity1.setState(1);
        reportKidJobEntity1.setException(StatusEnum.getMessage(1));
        reportKidJobEntity1.setRetryCount(0);
        reportKidJobEntity1.setReturnValue(san);
        reportKidJobEntity1.setStartTime(System.currentTimeMillis());
        reportKidJobEntity1.setEndTime(null);
        reportKidJobEntity1.setResponse(elementAttributeForIII.getCommonAttributeVal().getResultFile());
        reportKidJobEntity1.setTaskId((long) id);
        try {
            Response<String> response = cloudUnifyRedisFeign.addString(mapper.writeValueAsString(reportKidJobEntity1), id + "|report3");
            log.info("FillReport 入库成功 ：...." + response.toString());
        } catch (Exception e) {
            log.info("redis 3 :" + e.getMessage());
        }
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
