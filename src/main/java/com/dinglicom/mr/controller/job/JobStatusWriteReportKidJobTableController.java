package com.dinglicom.mr.controller.job;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dingli.cloudunify.pojo.dto.ReportDto;
import com.dingli.cloudunify.pojo.entity.report.ReportTask;
import com.dinglicom.mr.entity.ReportKidJobEntity;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.feign.CloudunifyAdminFeign;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.service.ReportJobService;
import com.dinglicom.mr.util.CallBackListenPool;
import com.dinglicom.mr.util.NumCompare;
import com.dinglicom.mr.util.SAXxml;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Log
@RestController
@RequestMapping(value = "/job/report")
public class JobStatusWriteReportKidJobTableController {
    private Logger logger = LoggerFactory.getLogger(JobStatusWriteReportKidJobTableController.class);
    @Autowired
    private ReportJobService reportJobService;
    @Autowired
    private ReportKidJobRepository reportKidJobRepository;
    @Autowired
    private CloudunifyAdminFeign cloudunifyAdminFeign;
    @Autowired
    private RabbitProducer rabbitProducer;

    @Autowired
    private CloudUnifyRedisFeign cloudUnifyRedisFeign;

    private Lock lock = new ReentrantLock();

    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(10,20,30L, TimeUnit.SECONDS,new LinkedBlockingDeque<>(10));

    @RequestMapping(value = "/web", method = RequestMethod.POST)
    public Response reportJob(@RequestBody ReportDto reportDto, Integer id, Integer prority) throws Exception {
        MessageCode messageCode = reportJobService.reportJobI(reportDto, id, prority == null ? 50 : prority);
        return ResponseGenerator.genSuccessResult(messageCode.getData());
    }

    @RequestMapping(value = "/updateStateAndStartTimeByIdToReportKidJob", method = RequestMethod.POST)
    public MessageCode updateStateAndStartTimeByIdToReportKidJob(@RequestParam String id) throws Exception {
        Response stringByKey = cloudUnifyRedisFeign.getStringByKey(id);
        ObjectMapper objectMapper = new ObjectMapper();
        ReportKidJobEntity reportKidJobEntity = objectMapper.readValue(stringByKey.getData().toString(), ReportKidJobEntity.class);
        reportKidJobEntity.setState(3);
        reportKidJobEntity.setStartTime(System.currentTimeMillis());
        Response response = cloudUnifyRedisFeign.updateDate(id, stringByKey.getData().toString());
        if (response.getData() != "0"){
            return new MessageCode(1, "SUCCESS!");
        }
        return new MessageCode(0, "fail!");
    }

    private static ReportKidJobEntity stringToRKJE(String a) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ReportKidJobEntity reportKidJobEntity = objectMapper.readValue(a, ReportKidJobEntity.class);
            return reportKidJobEntity;
        } catch (Exception e) {
            log.info("序列化失败:" + e.getMessage());
            return null;
        }

    }

    @RequestMapping(value = "/updateReportII")
    public MessageCode updateReportII(@RequestParam String key, @RequestParam String response, @RequestParam int stateCode) throws Exception {
        ObjectMapper o = new ObjectMapper();
        if (stateCode != 2) {
            Response stringByKey = cloudUnifyRedisFeign.getStringByKey(key);
            try {
                ReportKidJobEntity reportKidJobEntity = o.readValue(stringByKey.getData().toString(), ReportKidJobEntity.class);
                reportKidJobEntity.setState(stateCode);
                ReportKidJobEntity save = reportKidJobRepository.save(reportKidJobEntity);
                log.info("保存移除到mysql:" + save.toString());
                Response remove = cloudUnifyRedisFeign.remove(key);
                return new MessageCode(0, "stateCode : " + stateCode + ": reportkidJob " + save);
            } catch (IOException e) {
                log.info(e.toString());
            }
        }
        //考虑未来2级别多任务
        Response remove = cloudUnifyRedisFeign.remove(key);

//        Response<Set<String>> setResponse = cloudUnifyRedisFeign.searchKey(key.substring(0, key.indexOf(":")));
//        if (setResponse.getData().size() != 0) {
//            return new MessageCode(0, "2级别任务未结束...");
//        }
        String index = key.substring(0, key.indexOf(":") - 1);
        log.info("index: " + index);
        Response<Set<String>> setResponse1 = cloudUnifyRedisFeign.searchKey(index + "3");
        Set<String> data1 = setResponse1.getData();
        data1.parallelStream().forEach(s -> {
            try {
                Response stringByKey = cloudUnifyRedisFeign.getStringByKey(s);
                ReportKidJobEntity reportKidJobEntity = o.readValue(stringByKey.getData().toString(), ReportKidJobEntity.class);
                log.info("2级任务完成，3级任务开始..." + reportKidJobEntity.toString());
                if (reportKidJobEntity.getTaskId() != null) {
                    String returnValue = reportKidJobEntity.getReturnValue();
                    AllObject allObject = new AllObject();
                    allObject.setNum(0);
                    allObject.setObj(returnValue);
                    allObject.setId("REPORT-IC|" + s);
                    try {
                        rabbitProducer.send(allObject, returnValue + ":REPORT-IC|" + s, 70, true);
                        log.info("3级任务已经进入队列,待执行*****************");
                    } catch (Exception e) {
                        log.info(e.getMessage());
                    }
                }
            } catch (IOException e) {
                log.info(e.toString());
            }
        });
        ReportTask reportTask = new ReportTask();
        log.info("父id:" + Integer.parseInt(key.substring(0, key.indexOf("|"))));
        reportTask.setId(Integer.parseInt(key.substring(0, key.indexOf("|"))));
        reportTask.setStateInfo("1级任务完成，2级任务完成，3级任务开始...");
        Response response1 = cloudunifyAdminFeign.updateReportTaskById(reportTask);
        return new MessageCode(0, logger.getName());
    }

    @RequestMapping(value = "/updateReportIII")
    public MessageCode updateReportIII(@RequestParam String key, @RequestParam String response, @RequestParam int stateCode) throws Exception {
        Response stringByKey = cloudUnifyRedisFeign.getStringByKey(key);
        ObjectMapper objectMapper = new ObjectMapper();
        ReportKidJobEntity reportKidJobEntity1 = objectMapper.readValue(stringByKey.getData().toString(), ReportKidJobEntity.class);
        Response remove = cloudUnifyRedisFeign.remove(key);
        if (stateCode != 2) {
            reportKidJobEntity1.setState(stateCode);
            ReportKidJobEntity save = reportKidJobRepository.save(reportKidJobEntity1);
            log.info("report 3 :" + save.toString());
            return new MessageCode(0, "statecode:" + stateCode);
        }
        log.info("2级任务完成，3级任务完成");
        ReportTask reportTask = new ReportTask();
        reportTask.setId(Integer.parseInt(key.substring(0, key.indexOf("|"))));
        reportTask.setStatus(4);
        reportTask.setReportFile(reportKidJobEntity1.getResponse());
        reportTask.setStateInfo("1级任务完成，2级任务完成，3级任务完成！");
        log.info("更新task: " + reportTask.toString());
        Response response2 = cloudunifyAdminFeign.updateReportTaskById(reportTask);
        log.info("admin 响应:" + response2.toString());
        return new MessageCode(1, reportKidJobEntity1.toString());
    }
    private void localFindList(ReportKidJobEntity reportKidJobEntity) throws Exception {
        lock.lock();
        List<ReportKidJobEntity> byTaskIdAndLevelAndStateNot = reportKidJobRepository.findByTaskIdAndLevelAndStateNot(1, reportKidJobEntity.getLevel(), reportKidJobEntity.getState());
        if (byTaskIdAndLevelAndStateNot != null && byTaskIdAndLevelAndStateNot.size() > 0) {
            lock.unlock();
            return;
        }
        lock.unlock();
    }

  public static void main(String[] args) throws IOException {
      AtomicInteger a = new AtomicInteger(1);
      a.addAndGet(-1);
    System.out.println(a);
  }

    private static ListeningExecutorService ListenPool() {
        ListeningExecutorService listeningExecutorService = MoreExecutors.listeningDecorator(threadPoolExecutor);
        return listeningExecutorService;
    }

    @RequestMapping(value = "/updateReportI")
    public MessageCode updateReportI(@RequestParam String key, @RequestParam String response, @RequestParam int stateCode) throws Exception {
        String id = key.substring(0, key.indexOf("|"));
        if (stateCode != 2) {
            Response stringByKey = cloudUnifyRedisFeign.getStringByKey(key);
            ReportKidJobEntity reportKidJobEntity = stringToRKJE(stringByKey.getData().toString());
            log.info("rkje:" + reportKidJobEntity.toString());
            ReportKidJobEntity save = reportKidJobRepository.save(reportKidJobEntity);
            log.info(" " + save.toString());
        }
        Response num = cloudUnifyRedisFeign.getNum(id + "IA");
        log.info("new num:" + num.getData().toString());
        log.info("key:" + key + "--------------------/response:" + response + "---------------------/stateCode:" + stateCode);
        Response remove = cloudUnifyRedisFeign.remove(key);
        StringBuffer stringBuffer = new StringBuffer();
        if ((Integer) num.getData() == 0) {
            log.info("并发访问开始...");
            ListenableFuture<Response> IaRemove = ListenPool().submit(new RedisRemove(id + "IA", cloudUnifyRedisFeign));
            ListenableFuture<Response> failNum = ListenPool().submit(new RedisGet(id + "F", cloudUnifyRedisFeign));
            ListenableFuture<Response> allNum = ListenPool().submit(new RedisGet(id + "ALL", cloudUnifyRedisFeign));
            Futures.addCallback(IaRemove, new CallBackListenPool(), threadPoolExecutor);
            Futures.addCallback(failNum, new CallBackListenPool(), threadPoolExecutor);
            Futures.addCallback(allNum, new CallBackListenPool(), threadPoolExecutor);
            log.info("并发访问结束...");

//            Response IaRemove = cloudUnifyRedisFeign.remove(id + "IA");
//            Response failNum = cloudUnifyRedisFeign.getStringByKey(id + "F");
//            Response allNum = cloudUnifyRedisFeign.getStringByKey(id+"ALL");

            log.info("1级任务完成" + "2级任务开始...");
            Response<Set<String>> response1 = cloudUnifyRedisFeign.searchKey(id + "|report2");
            log.info("夜空中最闪亮的星..." + response1.getData().toString());
            response1.getData().stream().forEach(ostr -> {
                log.info(ostr + "----------------------------- 这个为空就是错");
                Response stringByKey1 = cloudUnifyRedisFeign.getStringByKey(ostr);
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    ReportKidJobEntity reportKidJobEntity1 = objectMapper.readValue((String) stringByKey1.getData(), ReportKidJobEntity.class);
                    AllObject allObject = new AllObject();
                    allObject.setNum(0);
                    allObject.setObj(reportKidJobEntity1.getReturnValue());
                    allObject.setId("REPORT-IB|" + reportKidJobEntity1.getId());
                    MessageCode<CopyOnWriteArrayList> copyOnWriteArrayListMessageCode = SAXxml.snoreOppositeMe(reportKidJobEntity1.getReturnValue(), 0.2);
                    log.info("不存在的文件..." + copyOnWriteArrayListMessageCode.getData().toString());
                    Integer failInt = Integer.valueOf(failNum.get().getData().toString())+copyOnWriteArrayListMessageCode.getData().size();
                    if (NumCompare.doubleCompare(failInt,Integer.valueOf(allNum.get().getData().toString()),0.2)){
                        log.info("失败任务过多");
                        return;
                    }
                    stringBuffer.append(copyOnWriteArrayListMessageCode.getData());
                    if (copyOnWriteArrayListMessageCode.getCode() == 1) {
                        //带key 下发队列,便于返回查找value
                        rabbitProducer.send(allObject, reportKidJobEntity1.getReturnValue() + ":REPORT-IB|" + ostr, 60, true);
                        log.info("2级任务*****************");
                    }
                } catch (Exception e) {
                    log.info(e.toString());
                }
            });
            ListenableFuture<Response> submit = ListenPool().submit(new RedisRemove(id + "ALL", cloudUnifyRedisFeign));
            ListenableFuture<Response> submit1 = ListenPool().submit(new RedisRemove(id + "F", cloudUnifyRedisFeign));

            Futures.addCallback(submit, new CallBackListenPool(), threadPoolExecutor);
            Futures.addCallback(submit1, new CallBackListenPool(), threadPoolExecutor);
//            cloudUnifyRedisFeign.remove(id+"ALL");
//            cloudUnifyRedisFeign.remove(id+"F");
        }




        ReportTask reportTask = new ReportTask();
        reportTask.setId(Integer.valueOf(id));
        reportTask.setStateInfo("1级任务完成，2级任务" + stringBuffer.toString());
        Response response1 = cloudunifyAdminFeign.updateReportTaskById(reportTask);
        return new MessageCode(1, "success!");
    }

    class RedisGet implements Callable<Response> {

        private Logger logger = LoggerFactory.getLogger(RedisGet.class);

        String key;
        CloudUnifyRedisFeign cloudUnifyRedisFeign;

        public RedisGet(String key, CloudUnifyRedisFeign cloudUnifyRedisFeign) {
            this.key = key;
            this.cloudUnifyRedisFeign = cloudUnifyRedisFeign;
        }


        @Override
        public Response call() throws Exception {
            logger.info("并发执行redis方法调用");
            Response stringByKey = cloudUnifyRedisFeign.getStringByKey(key);
            log.info("这个不是空" + stringByKey.toString());
            return stringByKey;
        }
    }

    class RedisRemove implements Callable<Response> {

        private Logger logger = LoggerFactory.getLogger(RedisRemove.class);

        String key;
        CloudUnifyRedisFeign cloudUnifyRedisFeign;

        public RedisRemove(String key, CloudUnifyRedisFeign cloudUnifyRedisFeign) {
            this.key = key;
            this.cloudUnifyRedisFeign = cloudUnifyRedisFeign;
        }

        @Override
        public Response call() throws Exception {
            logger.info("并发执行redis方法调用");
            Response stringByKey = cloudUnifyRedisFeign.remove(key);
            return stringByKey;
        }
    }

}
