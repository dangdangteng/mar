package com.dinglicom.mr.service;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.domain.StatisticalReportRequest;
import com.dingli.merger.Merger;
import com.dinglicom.mr.concurrent.RedisRemove;
import com.dinglicom.mr.concurrent.RedisSearchKey;
import com.dinglicom.mr.concurrent.taskFindById;
import com.dinglicom.mr.entity.ReportKidJobEntity;
import com.dinglicom.mr.entity.TaskConfigEntity;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import com.dinglicom.mr.repository.TaskConfigRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.util.CallBackListenPool;
import com.dinglicom.mr.util.ListUtil;
import com.dinglicom.mr.util.ListeningExecutorServiceUtil;
import com.dinglicom.mr.util.TaskUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static com.dinglicom.mr.util.ListeningExecutorServiceUtil.ListenPool;

@Log
@Service
public class ReportIIService {
    @Autowired
    private CloudUnifyRedisFeign cloudUnifyRedisFeign;
    @Autowired
    private TaskConfigRepository taskConfigRepository;
    @Autowired
    private RabbitProducer rabbitProducer;
    @Autowired
    private ReportKidJobRepository reportKidJobRepository;


    public MessageCode IIReport(String id, String xmlTem) throws Exception {
        ListenableFuture<Optional<TaskConfigEntity>> submit = ListenPool().submit(new taskFindById(300L, taskConfigRepository));
        ListenableFuture<Response> IaRemove = ListenPool().submit(new RedisRemove(id + "IA", cloudUnifyRedisFeign));
        ListenableFuture<Response<Set<String>>> setSum = ListenPool().submit(new RedisSearchKey(id + "Lover", cloudUnifyRedisFeign));
        Futures.addCallback(IaRemove, new CallBackListenPool(), ListeningExecutorServiceUtil.threadPoolExecutor);
        Futures.addCallback(setSum, new CallBackListenPool(), ListeningExecutorServiceUtil.threadPoolExecutor);
        Futures.addCallback(submit, new CallBackListenPool(), ListeningExecutorServiceUtil.threadPoolExecutor);

        log.info("1级任务完成,2级任务开始..." + setSum.get().getData().toString());
        CopyOnWriteArrayList<String> cowal = new CopyOnWriteArrayList();
        ForkJoinPool forkJoinPool = new ForkJoinPool(16);
        forkJoinPool.submit(() -> {
            try {
                setSum.get().getData().parallelStream().forEach(
                        s -> {
                            Response<String> stringByKey = cloudUnifyRedisFeign.getStringByKey(s);
                            List<String> strings = Arrays.asList(stringByKey.getData().split(","));
                            strings.stream().forEach(s1 -> {
                                cowal.add(s1);
                            });
                            Response<Boolean> remove = cloudUnifyRedisFeign.remove(s);
                        }
                );
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }).get();

        List<String> collect = cowal.parallelStream().filter(s -> {
            File file = new File(s);
            if (file.exists()) {
                log.info("create file success!");
                return true;
            }
            return false;
        }).map(String::toString).collect(Collectors.toList());
        List<List<String>> lists = ListUtil.splitList(collect, 200);
        CopyOnWriteArrayList<String> copyOnWriteArrayList = new CopyOnWriteArrayList();
        ForkJoinPool forkJoinPool1 = new ForkJoinPool(16);
        forkJoinPool1.submit(() -> {
            lists.parallelStream().forEach(list -> {
                Response<Long> longResponse1 = cloudUnifyRedisFeign.incrNum(id + "IB");
                try {
                    StatisticalReportRequest statisticalReportRequestForII = TaskUtil.getStatisticalReportRequestForII(submit.get().get(), list, xmlTem, Integer.valueOf(id));
                    if (statisticalReportRequestForII == null) {
                        Response<Long> longResponse = cloudUnifyRedisFeign.incrRanNum(id + "F", 200L);
                        return;
                    }
                    log.info("**elementAttributeForII :" + statisticalReportRequestForII.toString());
                    Merger merger = new Merger();
                    ArrayList<String> mergerList = new ArrayList();
                    boolean mergerRequestFile = merger.getMergerRequestFile(statisticalReportRequestForII, mergerList);
                    ReportKidJobEntity reportKidJobEntity = new ReportKidJobEntity();
                    if (!mergerRequestFile) {
                        Response<Long> longResponse = cloudUnifyRedisFeign.incrRanNum(id + "F", 200L);
                        return;
                    }
                    String er = mergerList.get(0);
                    reportKidJobEntity.setLevel(2);
                    reportKidJobEntity.setState(1);
                    reportKidJobEntity.setException(xmlTem);
                    reportKidJobEntity.setRetryCount(0);
                    reportKidJobEntity.setReturnValue(er);
                    reportKidJobEntity.setStartTime(System.currentTimeMillis());
                    reportKidJobEntity.setEndTime(null);
                    reportKidJobEntity.setResponse(statisticalReportRequestForII.getCommonAttributeVal().getResultFile());
                    reportKidJobEntity.setTaskId(Long.valueOf(id));
                    ObjectMapper mapper = new ObjectMapper();
                    String s = mapper.writeValueAsString(reportKidJobEntity);
                    Response response = cloudUnifyRedisFeign.addString(s, id + "|report2");
                    AllObject allObject = new AllObject();
                    allObject.setNum(0);
                    allObject.setObj(er + ":" + "REPORT-IB|" + response.getData().toString());
                    allObject.setId("REPORT-IB|" + response.getData());
                    copyOnWriteArrayList.add(statisticalReportRequestForII.getCommonAttributeVal().getResultFile());
                    log.info("er" + er + "++++++++++++++" + statisticalReportRequestForII.getCommonAttributeVal().getResultFile());
                    rabbitProducer.send(allObject, allObject.getObj(), 70, true);
                    log.info("merger 入库成功 ：...." + response.toString());
                } catch (Exception e) {
                    Response<Long> longResponse = cloudUnifyRedisFeign.incrRanNum(id + "F", 200L);
                    log.info(e.toString());
                }
            });
        }).get();
        log.info(copyOnWriteArrayList.toString()+"看我 -------------------------------------------");

        List<List<String>> uklist = ListUtil.splitList(copyOnWriteArrayList, 40);
        forkJoinPool.submit(() -> {
            uklist.parallelStream().forEach(l -> {
                Response response2 = cloudUnifyRedisFeign.addString(String.join(",", l), id + "Saber");
            });
        }).get();
        return new MessageCode(1, "1级任务全部完成，2级任务已经下发。。。");
    }

    public void Error(String key, String id) throws Exception{
        Response stringByKey = cloudUnifyRedisFeign.getStringByKey(key);
        Response<Long> longResponse = cloudUnifyRedisFeign.incrNum(id + "F");
        ObjectMapper mapper = new ObjectMapper();
        ReportKidJobEntity reportKidJobEntity = mapper.readValue(stringByKey.getData().toString(), ReportKidJobEntity.class);
        log.info("rkje:" + reportKidJobEntity.toString());
        ReportKidJobEntity save = reportKidJobRepository.save(reportKidJobEntity);
        log.info(" " + save.toString());
    }

    public MessageCode iIReportDo(String id, List<String> strings, String xmlTem) throws Exception {
        Optional<TaskConfigEntity> byId = taskConfigRepository.findById(300);
        List<String> collect = strings.parallelStream().filter(s -> {
            File file = new File(s);
            if (file.exists()) {
                log.info("create file success!");
                return true;
            }
            return false;
        }).map(String::toString).collect(Collectors.toList());
        List<List<String>> lists = ListUtil.splitList(collect, 200);
        CopyOnWriteArrayList copyOnWriteArrayList = new CopyOnWriteArrayList();
        ForkJoinPool forkJoinPool1 = new ForkJoinPool(16);
        forkJoinPool1.submit(() -> {
            lists.parallelStream().forEach(list -> {
                Response<Long> longResponse1 = cloudUnifyRedisFeign.incrNum(id + "IB");
                try {
                    StatisticalReportRequest statisticalReportRequestForII = TaskUtil.getStatisticalReportRequestForII(byId.get(), list, xmlTem, Integer.valueOf(id));
                    if (statisticalReportRequestForII == null) {
                        Response<Long> longResponse = cloudUnifyRedisFeign.incrRanNum(id + "F", 200L);
                        return;
                    }
                    log.info("**elementAttributeForII :" + statisticalReportRequestForII.toString());
                    Merger merger = new Merger();
                    ArrayList<String> mergerList = new ArrayList();
                    boolean mergerRequestFile = merger.getMergerRequestFile(statisticalReportRequestForII, mergerList);
                    ReportKidJobEntity reportKidJobEntity = new ReportKidJobEntity();
                    if (!mergerRequestFile) {
                        Response<Long> longResponse = cloudUnifyRedisFeign.incrRanNum(id + "F", 200L);
                        return;
                    }
                    String er = mergerList.get(0);
                    reportKidJobEntity.setLevel(2);
                    reportKidJobEntity.setState(1);
                    reportKidJobEntity.setException(xmlTem);
                    reportKidJobEntity.setRetryCount(0);
                    reportKidJobEntity.setReturnValue(er);
                    reportKidJobEntity.setStartTime(System.currentTimeMillis());
                    reportKidJobEntity.setEndTime(null);
                    reportKidJobEntity.setResponse(statisticalReportRequestForII.getCommonAttributeVal().getResultFile());
                    reportKidJobEntity.setTaskId(Long.valueOf(id));
                    ObjectMapper mapper = new ObjectMapper();
                    String s = mapper.writeValueAsString(reportKidJobEntity);
                    Response response = cloudUnifyRedisFeign.addString(s, id + "|report2");
                    AllObject allObject = new AllObject();
                    allObject.setNum(0);
                    allObject.setObj(er + ":" + "REPORT-IB|" + response.getData().toString());
                    allObject.setId("REPORT-IB|" + response.getData());
                    copyOnWriteArrayList.add(statisticalReportRequestForII.getCommonAttributeVal().getResultFile());
                    rabbitProducer.send(allObject, allObject.getObj(), 70, true);
                    log.info("merger 入库成功 ：...." + response.toString());
                } catch (Exception e) {
                    Response<Long> longResponse = cloudUnifyRedisFeign.incrRanNum(id + "F", 200L);
                    log.info(e.toString());
                }
            });
        }).get();
        List<List<String>> uklist = ListUtil.splitList(copyOnWriteArrayList, 40);
        forkJoinPool1.submit(() -> {
            uklist.parallelStream().forEach(l -> {
                Response response2 = cloudUnifyRedisFeign.addString(String.join(",", l), id + "Saber");
            });
        }).get();
        return new MessageCode(1, "1级任务全部完成，2级任务已经下发。。。");
    }
}