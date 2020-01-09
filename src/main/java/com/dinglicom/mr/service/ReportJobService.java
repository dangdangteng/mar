package com.dinglicom.mr.service;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.pojo.dto.ReportDto;
import com.dingli.cloudunify.pojo.entity.report.ReportTask;
import com.dingli.domain.StatisticalReportRequest;
import com.dingli.simplifyStatistics.SimplifyStatistics;
import com.dinglicom.mr.concurrent.RedisAddStringAndAddNum;
import com.dinglicom.mr.concurrent.RedisIncr;
import com.dinglicom.mr.entity.ReportKidJobEntity;
import com.dinglicom.mr.entity.TaskConfigEntity;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.feign.CloudunifyAdminFeign;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import com.dinglicom.mr.repository.TaskConfigRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

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

    private MessageCode noScene(Optional<TaskConfigEntity> byId, ReportDto reportDto, int id, Integer priority) throws Exception {
        List<StatisticalReportRequest> elementAttribute = TaskUtil.getStatisticalReportRequest(byId.get(), reportDto, id);
        CopyOnWriteArrayList<String> reportIList = new CopyOnWriteArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        if (elementAttribute == null) {
            return new MessageCode(0, "数据有误！");
        }

        ListenableFuture<Response<String>> addNum = ListeningExecutorServiceUtil.ListenPool().submit(new RedisAddStringAndAddNum(elementAttribute.size() + "", id + "ALL", "addNum", cloudUnifyRedisFeign));
        ListenableFuture<Response<String>> xmladd = ListeningExecutorServiceUtil.ListenPool().submit(new RedisAddStringAndAddNum(reportDto.getReportItem().getXmlTemplateFile(), id + "xml", "addNum", cloudUnifyRedisFeign));
        ListenableFuture<Response> f = ListeningExecutorServiceUtil.ListenPool().submit(new RedisIncr(id + "F", cloudUnifyRedisFeign));
        Futures.addCallback(addNum, new CallBackListenPool<>(), ListeningExecutorServiceUtil.threadPoolExecutor);
        Futures.addCallback(xmladd, new CallBackListenPool<>(), ListeningExecutorServiceUtil.threadPoolExecutor);
        Futures.addCallback(f, new CallBackListenPool<>(), ListeningExecutorServiceUtil.threadPoolExecutor);

        ForkJoinPool forkJoinPool1 = new ForkJoinPool(16);
        forkJoinPool1.submit(() -> {
            elementAttribute.parallelStream().forEach(elementAttribute2 -> {
                if (StringUtils.isEmpty(elementAttribute2.getItemAttributeVal().getDdibFileName())) {
                    log.info("数据源ddib文件为空");
                    Response<Long> longResponse = cloudUnifyRedisFeign.incrNum(id + "F");
                    log.info("失败的任务数量:" + longResponse.getData().toString());
                    return;
                }
                SimplifyStatistics simplifyStatistics = new SimplifyStatistics();
                log.info(elementAttribute2.toString() + "----" + elementAttribute2.getCommonAttributeVal().toString() + "--------" + elementAttribute2.getItemAttributeVal().toString());
                ArrayList<String> simplifyList = new ArrayList();
                Boolean simplifyStatisticRequestFile = null;
                try {
                    simplifyStatisticRequestFile = simplifyStatistics.getSimplifyStatisticRequestFile(elementAttribute2, simplifyList);
                } catch (Exception e) {
                    log.info("调超强任务接口失败!" + e.toString());
                    Response<Long> longResponse = cloudUnifyRedisFeign.incrNum(id + "F");
                    log.info("失败的任务数量:" + longResponse.getData().toString());
                    return;
                }
                String yi = simplifyList.get(0);
                if (simplifyStatisticRequestFile) {
                    try {
                        ReportKidJobEntity reportKidJobEntity = new ReportKidJobEntity();
                        reportKidJobEntity.setLevel(1);
                        reportKidJobEntity.setState(1);
                        reportKidJobEntity.setException(reportDto.getReportItem().getXmlTemplateFile());
                        reportKidJobEntity.setRetryCount(0);
                        reportKidJobEntity.setReturnValue(yi);
                        reportKidJobEntity.setStartTime(System.currentTimeMillis());
                        reportKidJobEntity.setEndTime(null);
                        reportKidJobEntity.setTaskId((long) id);
                        reportKidJobEntity.setResponse(elementAttribute2.getCommonAttributeVal().getResultFile());
                        String s = mapper.writeValueAsString(reportKidJobEntity);
                        Response response = cloudUnifyRedisFeign.addString(s, id + "|report1");
                        Response<Long> longResponse = cloudUnifyRedisFeign.incrNum(id + "IA");
                        AllObject allObject = new AllObject();
                        allObject.setId("REPORT-IA|" + response.getData());
                        allObject.setPort(reportDto.getData().get(0).getPort());
                        allObject.setFilePathName(reportDto.getData().get(0).getFileName());
                        allObject.setObj(yi + ":" + "REPORT-IA|" + response.getData());
                        allObject.setNum(0);
                        log.info("uk file is..." + elementAttribute2.getCommonAttributeVal().getResultFile());
                        reportIList.add(elementAttribute2.getCommonAttributeVal().getResultFile());
                        rabbitProducer.send(allObject, allObject.getObj(), priority == null ? 50 : priority, true);
                    } catch (Exception e) {
                        log.info("I 级别任务出错 ：" + e.getMessage() + e.toString());
                    }
                }
            });
        }).get();
        Thread.sleep(reportIList.size());
        log.info("my name is ....." + reportIList.toString());
        Response<Long> longNum = cloudUnifyRedisFeign.getLongNum(id + "F");
        if (NumCompare.doubleCompare(longNum.getData() - 1, elementAttribute.size(), 0.2)) {
            //发布订阅
            log.info("任务失败过多");
            ReportKidJobEntity reportKidJobEntity = new ReportKidJobEntity();
            reportKidJobEntity.setException("数据源文件存在问题");
            reportKidJobEntity.setStartTime(System.currentTimeMillis());
            reportKidJobRepository.save(reportKidJobEntity);
            log.info("数据源文件存在问题...");
            return new MessageCode(0, "数据源文件存在问题...");
        }
        log.info(reportIList.toString() + "wocao  -----------");
        List<List<String>> lists = ListUtil.splitList(reportIList, 40);
        log.info("一级任务产生的uk：" + lists.get(0).toString());
        lists.parallelStream().forEach(l -> {
            Response response2 = cloudUnifyRedisFeign.addString(String.join(",", l), id + "Lover");
        });
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
        List<String> sceneName = SAXxml.findSceneName(reportDto.getReportItem().getXmlTemplateFile());
        if (sceneName == null) {
            MessageCode messageCode = noScene(byId, reportDto, id, priority);
            return messageCode;
        }
        MessageCode sence = Sence(byId, reportDto, id, priority, sceneName);
        return sence;
    }

    private MessageCode Sence(Optional<TaskConfigEntity> byId, ReportDto reportDto, int id, Integer priority, List<String> sceneName) throws Exception {
        List<List<StatisticalReportRequest>> statisticalReportRequest = TaskUtil.getStatisticalReportRequest(byId.get(), reportDto, id, sceneName);
        CopyOnWriteArrayList<String> reportIList = new CopyOnWriteArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        if (statisticalReportRequest == null) {
            return new MessageCode(0, "数据有误！");
        }
        ListenableFuture<Response<String>> addNum = ListeningExecutorServiceUtil.ListenPool().submit(new RedisAddStringAndAddNum(NumCompare.multiplicationNum(statisticalReportRequest.size(), statisticalReportRequest.get(0).size()) + "", id + "ALL", "addNum", cloudUnifyRedisFeign));
        ListenableFuture<Response<String>> xmladd = ListeningExecutorServiceUtil.ListenPool().submit(new RedisAddStringAndAddNum(reportDto.getReportItem().getXmlTemplateFile(), id + "xml", "addNum", cloudUnifyRedisFeign));
        ListenableFuture<Response> f = ListeningExecutorServiceUtil.ListenPool().submit(new RedisIncr(id + "F", cloudUnifyRedisFeign));
        Futures.addCallback(addNum, new CallBackListenPool<>(), ListeningExecutorServiceUtil.threadPoolExecutor);
        Futures.addCallback(xmladd, new CallBackListenPool<>(), ListeningExecutorServiceUtil.threadPoolExecutor);
        Futures.addCallback(f, new CallBackListenPool<>(), ListeningExecutorServiceUtil.threadPoolExecutor);

        ForkJoinPool forkJoinPool1 = new ForkJoinPool(16);
        forkJoinPool1.submit(() -> {
            statisticalReportRequest.parallelStream().forEach(list -> {
                list.parallelStream().forEach(statisticalReportRequest1 -> {
                    if (StringUtils.isEmpty(statisticalReportRequest1.getItemAttributeVal().getDdibFileName())) {
                        log.info("数据源ddib文件为空");
                        Response<Long> longResponse = cloudUnifyRedisFeign.incrNum(id + "F");
                        log.info("失败的任务数量:" + longResponse.getData().toString());
                        return;
                    }
                    SimplifyStatistics simplifyStatistics = new SimplifyStatistics();
                    log.info(statisticalReportRequest1.toString() + "----" + statisticalReportRequest1.getCommonAttributeVal().toString() + "--------" + statisticalReportRequest1.getItemAttributeVal().toString());
                    ArrayList<String> simplifyList = new ArrayList();
                    Boolean simplifyStatisticRequestFile = null;
                    try {
                        simplifyStatisticRequestFile = simplifyStatistics.getSimplifyStatisticRequestFile(statisticalReportRequest1, simplifyList);
                    } catch (Exception e) {
                        log.info("调超强任务接口失败!" + e.toString());
                        Response<Long> longResponse = cloudUnifyRedisFeign.incrNum(id + "F");
                        log.info("失败的任务数量:" + longResponse.getData().toString());
                        return;
                    }
                    String yi = simplifyList.get(0);
                    if (simplifyStatisticRequestFile) {
                        try {
                            ReportKidJobEntity reportKidJobEntity = new ReportKidJobEntity();
                            reportKidJobEntity.setLevel(1);
                            reportKidJobEntity.setState(1);
                            reportKidJobEntity.setException(reportDto.getReportItem().getXmlTemplateFile());
                            reportKidJobEntity.setRetryCount(0);
                            reportKidJobEntity.setReturnValue(yi);
                            reportKidJobEntity.setStartTime(System.currentTimeMillis());
                            reportKidJobEntity.setEndTime(null);
                            reportKidJobEntity.setTaskId((long) id);
                            reportKidJobEntity.setResponse(statisticalReportRequest1.getCommonAttributeVal().getResultFile());
                            String s = mapper.writeValueAsString(reportKidJobEntity);
                            Response response = cloudUnifyRedisFeign.addString(s, id + "|report1");
                            Response<Long> longResponse = cloudUnifyRedisFeign.incrNum(id + "IA");
                            AllObject allObject = new AllObject();
                            allObject.setId("REPORT-IA|" + response.getData());
                            allObject.setPort(reportDto.getData().get(0).getPort());
                            allObject.setFilePathName(reportDto.getData().get(0).getFileName());
                            allObject.setObj(yi + ":" + "REPORT-IA|" + response.getData());
                            allObject.setNum(0);
                            log.info("uk file is..." + statisticalReportRequest1.getCommonAttributeVal().getResultFile());
                            reportIList.add(statisticalReportRequest1.getCommonAttributeVal().getResultFile());
                            rabbitProducer.send(allObject, allObject.getObj(), priority == null ? 50 : priority, true);
                        } catch (Exception e) {
                            log.info("I 级别任务出错 ：" + e.getMessage() + e.toString());
                        }
                    }
                });
            });
        }).get();
        Thread.sleep(reportIList.size());
        log.info("my name is ....." + reportIList.toString());
        Response<Long> longNum = cloudUnifyRedisFeign.getLongNum(id + "F");
        if (NumCompare.doubleCompare(longNum.getData() - 1, Long.valueOf(addNum.get().getData()), 0.2)) {
            //发布订阅
            log.info("任务失败过多");
            ReportKidJobEntity reportKidJobEntity = new ReportKidJobEntity();
            reportKidJobEntity.setException("数据源文件存在问题");
            reportKidJobEntity.setStartTime(System.currentTimeMillis());
            reportKidJobRepository.save(reportKidJobEntity);
            log.info("数据源文件存在问题...");
            return new MessageCode(0, "数据源文件存在问题...");
        }
        log.info(reportIList.toString() + "wocao  -----------");
        List<List<String>> lists = ListUtil.splitList(reportIList, 40);
        log.info("一级任务产生的uk：" + lists.get(0).toString());
        lists.parallelStream().forEach(l -> {
            Response response2 = cloudUnifyRedisFeign.addString(String.join(",", l), id + "Lover");
        });
        ReportTask reportTask = new ReportTask();
        reportTask.setId(id);
        reportTask.setStatus(2);
        reportTask.setStateInfo("1级任务已经压入队列");
        Response response = cloudunifyAdminFeign.updateReportTaskById(reportTask);
        return new MessageCode(1, reportTask.toString());
    }
}
