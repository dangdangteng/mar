package com.dinglicom.mr.controller.job;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dingli.cloudunify.pojo.dto.ReportDto;
import com.dingli.cloudunify.pojo.entity.report.ReportTask;
import com.dinglicom.mr.concurrent.RedisGet;
import com.dinglicom.mr.concurrent.RedisGetLong;
import com.dinglicom.mr.entity.ErrorListEntity;
import com.dinglicom.mr.entity.ReportKidJobEntity;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.feign.CloudunifyAdminFeign;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.ErrorListRepository;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.service.ReportIIIService;
import com.dinglicom.mr.service.ReportIIService;
import com.dinglicom.mr.service.ReportJobService;
import com.dinglicom.mr.util.CallBackListenPool;
import com.dinglicom.mr.util.ListeningExecutorServiceUtil;
import com.dinglicom.mr.util.NumCompare;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.dinglicom.mr.util.ListeningExecutorServiceUtil.ListenPool;

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

    @Autowired
    private ReportIIService reportIIService;
    @Autowired
    private ReportIIIService reportIIIService;

    private Lock lock = new ReentrantLock();
    @Autowired
    private ErrorListRepository errorListRepository;

    @RequestMapping(value = "/web", method = RequestMethod.POST)
    public Response reportJob(@RequestBody ReportDto reportDto, Integer id, Integer prority) throws Exception {
        Map map = new HashMap(16);
        MessageCode messageCode = reportJobService.reportJobI(reportDto, id, prority == null ? 50 : prority);
        return ResponseGenerator.genSuccessResult(messageCode.getData());
    }
    @RequestMapping(value = "/webmap", method = RequestMethod.POST)
    public Response reportJob(@RequestBody ReportDto reportDto, Integer id, Integer prority, Map<String,String> map) throws Exception {
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



    @RequestMapping(value = "/updateReportII")
    public MessageCode updateReportII(@RequestParam String key, @RequestParam String response, @RequestParam int stateCode) throws Exception {
        ObjectMapper o = new ObjectMapper();
        String id = key.substring(0, key.indexOf("|"));
        Response<Long> longResponse1 = cloudUnifyRedisFeign.decrNum(id + "IB");
        Response stringByKey = cloudUnifyRedisFeign.getStringByKey(key);
        Response remove = cloudUnifyRedisFeign.remove(key);
        if (stateCode != 2) {
            Response<Long> longResponse = cloudUnifyRedisFeign.incrRanNum(id + "F", 200);
            try{
                ReportKidJobEntity reportKidJobEntity = o.readValue(stringByKey.getData().toString(), ReportKidJobEntity.class);
                reportKidJobEntity.setState(stateCode);
                ReportKidJobEntity save = reportKidJobRepository.save(reportKidJobEntity);
                log.info("保存移除到mysql:" + save.toString());
            }catch (Exception e){
                log.info(e.toString());
            }
        }
        if (longResponse1.getData() > 0) {
            log.info("还有合并任务");
            return new MessageCode(1, "还有合并任务");
        }
        Response<Boolean> remove2 = cloudUnifyRedisFeign.remove(id + "IB");
        Response<Set<String>> setResponse = cloudUnifyRedisFeign.searchKey(id + "Saber");
        CopyOnWriteArrayList cowal = new CopyOnWriteArrayList();
        ForkJoinPool forkJoinPool1 = new ForkJoinPool(16);
        forkJoinPool1.submit(() -> {
            setResponse.getData().parallelStream().forEach(s -> {
                Response<String> stringByKey1 = cloudUnifyRedisFeign.getStringByKey(s);
                List<String> strings = Arrays.asList(stringByKey1.getData().split(","));
                strings.parallelStream().forEach(s1 -> {
                    cowal.add(s1);
                });
                Response<Boolean> remove1 = cloudUnifyRedisFeign.remove(s);
            });
        }).get();
//        Response<Boolean> delete = cloudUnifyRedisFeign.remove(id + "Saber");
        log.info("夜空中最闪亮的星..." + cowal.toString());
        if (cowal.size() == 1) {
            MessageCode test = reportIIIService.IIIJobDoing(cowal.get(0).toString(), cloudUnifyRedisFeign.getStringByKey(id+"xml").getData(), Integer.valueOf(id), key);
            return test;
        } else {
            MessageCode messageCode = reportIIService.iIReportDo(id, cowal, cloudUnifyRedisFeign.getStringByKey(id+"xml").getData());
            return messageCode;
        }
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
        log.info("2 complete，3 complete");
        ReportTask reportTask = new ReportTask();
        reportTask.setId(Integer.parseInt(key.substring(0, key.indexOf("|"))));
        reportTask.setStatus(4);
        reportTask.setReportFile(reportKidJobEntity1.getResponse());
        reportTask.setStateInfo("1 complete, 2 complete,3 complete!");
        log.info("更新task: " + reportTask.toString());
        Response response2 = cloudunifyAdminFeign.updateReportTaskById(reportTask);
        log.info("admin 响应:" + response2.toString());
        return new MessageCode(1, reportKidJobEntity1.toString());
    }

  public static void main(String[] args) throws Exception {
      AtomicInteger a = new AtomicInteger(1);
      a.addAndGet(-1);
    System.out.println(a);
  }
    @RequestMapping(value = "/updateReportI")
    public MessageCode updateReportI(@RequestParam String key, @RequestParam String response, @RequestParam int stateCode) throws Exception {
        String id = key.substring(0, key.indexOf("|"));
        Response<Long> num = cloudUnifyRedisFeign.decrNum(id + "IA");
        Response<String> stringByKey = cloudUnifyRedisFeign.getStringByKey(key);
        log.info("_____________________kanzheli_______________________"+stringByKey.getData());
        log.info("new num:" + num.getData().toString());
        if (stateCode != 2) {
            Response<Long> longResponse = cloudUnifyRedisFeign.incrNum(id + "F");
            ObjectMapper mapper = new ObjectMapper();
            ReportKidJobEntity reportKidJobEntity = mapper.readValue(stringByKey.getData(), ReportKidJobEntity.class);
            log.info("rkje:" + reportKidJobEntity.toString());
            ReportKidJobEntity save = reportKidJobRepository.save(reportKidJobEntity);
            log.info(" " + save.toString());
        }
        ListenableFuture<Response<Long>> failNum = ListenPool().submit(new RedisGetLong(id + "F", cloudUnifyRedisFeign));
        ListenableFuture<Response<String>> allNum = ListenPool().submit(new RedisGet(id + "ALL", cloudUnifyRedisFeign));
        Futures.addCallback(failNum, new CallBackListenPool(), ListeningExecutorServiceUtil.threadPoolExecutor);
        Futures.addCallback(allNum, new CallBackListenPool(), ListeningExecutorServiceUtil.threadPoolExecutor);
        if (NumCompare.doubleCompare(failNum.get().getData() - 1, Long.valueOf(allNum.get().getData()), 0.2)) {
            ErrorListEntity save = errorListRepository.save(ErrorListEntity.builder().build().setTaskId(id).setTime(LocalDateTime.now()));
            return new MessageCode(0, "fail!", save);
        }
        Response remove = cloudUnifyRedisFeign.remove(key);
        StringBuffer stringBuffer = new StringBuffer();
        if (num.getData() == 0) {
            MessageCode messageCode = reportIIService.IIReport(id, cloudUnifyRedisFeign.getStringByKey(id+"xml").getData());
            return messageCode;
        }
        ReportTask reportTask = new ReportTask();
        reportTask.setId(Integer.valueOf(id));
        reportTask.setStateInfo("1级任务完成");
        Response response1 = cloudunifyAdminFeign.updateReportTaskById(reportTask);
        return new MessageCode(1, "success!");
    }
}
