package com.dinglicom.mr.controller.job;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dingli.cloudunify.pojo.dto.ReportDto;
import com.dingli.cloudunify.pojo.entity.report.ReportTask;
import com.dinglicom.mr.entity.ReportKidJob;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.feign.CloudunifyAdminFeign;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.service.ReportJobService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Set;
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

    @RequestMapping(value = "/web", method = RequestMethod.POST)
    public Response reportJob(@RequestBody ReportDto reportDto, Integer id, Integer prority) throws Exception {
        MessageCode messageCode = reportJobService.reportJobI(reportDto, id, prority == null ? 50 : prority);
        return ResponseGenerator.genSuccessResult(messageCode.getData());
    }

    @RequestMapping(value = "/updateStateAndStartTimeByIdToReportKidJob", method = RequestMethod.POST)
    public MessageCode updateStateAndStartTimeByIdToReportKidJob(@RequestParam String id) throws Exception {
        Response stringByKey = cloudUnifyRedisFeign.getStringByKey(id);
        ObjectMapper objectMapper = new ObjectMapper();
        ReportKidJob reportKidJob = objectMapper.readValue(stringByKey.getData().toString(), ReportKidJob.class);
        reportKidJob.setState(3);
        reportKidJob.setStartTime(System.currentTimeMillis());
        Response response = cloudUnifyRedisFeign.updateDate(id, stringByKey.getData().toString());
        if (response.getData() != "0"){
            return new MessageCode(1, "SUCCESS!");
        }
        return new MessageCode(0, "fail!");
    }

    @RequestMapping(value = "/updateReportI")
    public MessageCode updateReportI(@RequestParam String key, @RequestParam String response, @RequestParam int stateCode) throws Exception {
        Response stringByKey = cloudUnifyRedisFeign.getStringByKey(key);
        ObjectMapper objectMapper = new ObjectMapper();
        ReportKidJob reportKidJob = objectMapper.readValue(stringByKey.getData().toString(), ReportKidJob.class);
        Response remove = cloudUnifyRedisFeign.remove(key);
        if (stateCode != 2) {
            ReportKidJob save = reportKidJobRepository.save(reportKidJob);
            log.info(" " + save.toString());
        }
        String id = key.substring(0, key.indexOf("|"));
        Response stringByKey2 = cloudUnifyRedisFeign.getStringByKey(id + "IA");
        log.info("num :"+stringByKey2.getData().toString());
        //理论上返回应该为一个参数，迭代获取data，转换自减 1
        Response num = cloudUnifyRedisFeign.getNum(id + "IA");
        Integer integer = (Integer)num.getData();
        log.info("new num:"+integer);
        if (integer == 0) {
            Response remove1 = cloudUnifyRedisFeign.remove(id + "IA");
            log.info("1级任务完成" + "2级任务开始...");
            Response<Set<String>> response1 = cloudUnifyRedisFeign.searchKey(id + "|report2");
            log.info(response1.toString() + "set里面的破玩意!");
            response1.getData().stream().forEach(ostr -> {
                log.info(ostr + "----------------------------- 这里出错了吗?");
                Response stringByKey1 = cloudUnifyRedisFeign.getStringByKey(ostr);
                try {
                    ReportKidJob reportKidJob1 = objectMapper.readValue((String) stringByKey1.getData(), ReportKidJob.class);
                    AllObject allObject = new AllObject();
                    allObject.setNum(0);
                    allObject.setObj(reportKidJob1.getReturnValue());
                    allObject.setId("REPORT-IB|" + reportKidJob1.getId());
                    try {
                        //带key 下发队列,便于返回查找value
                        rabbitProducer.send(allObject, reportKidJob1.getReturnValue() + ":REPORT-IB|" + ostr, 50, true);
                        log.info("2级任务*****************");
                    } catch (Exception e) {
                        log.info(e.getMessage());
                    }
                } catch (IOException e) {
                    log.info(e.toString());
                }
            });
        }
        ReportTask reportTask = new ReportTask();
        reportTask.setId(Integer.valueOf(id));
        reportTask.setStateInfo("1级任务完成，2级任务开始...");
        Response response1 = cloudunifyAdminFeign.updateReportTaskById(reportTask);
        return new MessageCode(1, "success!");
    }

    @RequestMapping(value = "/updateReportII")
    public MessageCode updateReportII(@RequestParam String key, @RequestParam String response, @RequestParam int stateCode) throws Exception {
        ObjectMapper o = new ObjectMapper();
        if (stateCode != 2) {
            Response stringByKey = cloudUnifyRedisFeign.getStringByKey(key);
            try {
                ReportKidJob reportKidJob = o.readValue(stringByKey.getData().toString(), ReportKidJob.class);
                reportKidJob.setState(stateCode);
                ReportKidJob save = reportKidJobRepository.save(reportKidJob);
                log.info("保存移除到mysql:" + save.toString());
                Response remove = cloudUnifyRedisFeign.remove(key);
                return new MessageCode(0, "stateCode : " + stateCode + ": reportkidJob " + save);
            } catch (IOException e) {
                log.info(e.toString());
            }
        }
        //考虑未来2级别多任务
        Response remove = cloudUnifyRedisFeign.remove(key);
        Response<Set<String>> setResponse = cloudUnifyRedisFeign.searchKey(key.substring(0, key.indexOf(":")));
        if (setResponse.getData().size() != 0) {
            return new MessageCode(0, "2级别任务未结束...");
        }
        String index = key.substring(0, key.indexOf(":") - 1);
        log.info("index: " + index);
        Response<Set<String>> setResponse1 = cloudUnifyRedisFeign.searchKey(index + "3");
        Set<String> data1 = setResponse1.getData();
        data1.parallelStream().forEach(s -> {
            try {
                Response stringByKey = cloudUnifyRedisFeign.getStringByKey(s);
                ReportKidJob reportKidJob = o.readValue(stringByKey.getData().toString(), ReportKidJob.class);
                log.info("2级任务完成，3级任务开始..." + reportKidJob.toString());
                if (reportKidJob.getTaskId() != null) {
                    String returnValue = reportKidJob.getReturnValue();
                    AllObject allObject = new AllObject();
                    allObject.setNum(0);
                    allObject.setObj(returnValue);
                    allObject.setId("REPORT-IC|" + s);
                    try {
                        rabbitProducer.send(allObject, returnValue + ":REPORT-IC|" + s, 50, true);
                        log.info("3完成级任务*****************");
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
        ReportKidJob reportKidJob1 = objectMapper.readValue(stringByKey.getData().toString(), ReportKidJob.class);
        Response remove = cloudUnifyRedisFeign.remove(key);
        if (stateCode != 2) {
            reportKidJob1.setState(stateCode);
            ReportKidJob save = reportKidJobRepository.save(reportKidJob1);
            log.info("report 3 :" + save.toString());
            return new MessageCode(0, "statecode:" + stateCode);
        }
        log.info("2级任务完成，3级任务完成");
        ReportTask reportTask = new ReportTask();
        reportTask.setId(reportKidJob1.getTaskId());
        reportTask.setStatus(4);
        reportTask.setStateInfo("1级任务完成，2级任务完成，3级任务完成！");
        Response response2 = cloudunifyAdminFeign.updateReportTaskById(reportTask);
        return new MessageCode(1, reportKidJob1.toString());
    }
    private void localFindList(ReportKidJob reportKidJob) throws Exception {
        lock.lock();
        List<ReportKidJob> byTaskIdAndLevelAndStateNot = reportKidJobRepository.findByTaskIdAndLevelAndStateNot(reportKidJob.getTaskId(), reportKidJob.getLevel(), reportKidJob.getState());
        if (byTaskIdAndLevelAndStateNot != null && byTaskIdAndLevelAndStateNot.size() > 0) {
            lock.unlock();
            return;
        }
        lock.unlock();
    }

  public static void main(String[] args) throws IOException {
    // "{\"id\":null,\"taskId\":302,\"level\":1,\"state\":1,\"startTime\":1571207806239,\"endTime\":null,\"retryCount\":0,\"exception\":\"任务等待\",\"returnValue\":\"/home/fleet/fleetSwapDatas/report/04741ced-2f37-4b8c-9907-9818baebffe9.xml\",\"response\":\"/home/fleet/fleetSwapDatas/report/f95374434f73119b3c675e227cc.uk\",\"data\":null}"
      ObjectMapper objectMapper = new ObjectMapper();
      ReportKidJob reportKidJob = objectMapper.readValue("{\"id\":null,\"taskId\":302,\"level\":1,\"state\":1,\"startTime\":1571207806239,\"endTime\":null,\"retryCount\":0,\"exception\":\"任务等待\",\"returnValue\":\"/home/fleet/fleetSwapDatas/report/04741ced-2f37-4b8c-9907-9818baebffe9.xml\",\"response\":\"/home/fleet/fleetSwapDatas/report/f95374434f73119b3c675e227cc.uk\",\"data\":null}"
      ,ReportKidJob.class);
    System.out.println(reportKidJob.toString());
  }
}
