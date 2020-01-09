package com.dinglicom.mr.service;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.pojo.entity.report.ReportTask;
import com.dingli.domain.StatisticalReportRequest;
import com.dingli.fillReport.FillReport;
import com.dinglicom.mr.concurrent.RedisRemove;
import com.dinglicom.mr.concurrent.taskFindById;
import com.dinglicom.mr.entity.ReportKidJobEntity;
import com.dinglicom.mr.entity.TaskConfigEntity;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.feign.CloudUnifyRedisFeign;
import com.dinglicom.mr.feign.CloudunifyAdminFeign;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.TaskConfigRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.util.CallBackListenPool;
import com.dinglicom.mr.util.ListeningExecutorServiceUtil;
import com.dinglicom.mr.util.TaskUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Log
@Service
public class ReportIIIService {
    @Autowired
    private TaskConfigRepository taskConfigRepository;
    @Autowired
    private CloudUnifyRedisFeign cloudUnifyRedisFeign;
    @Autowired
    private RabbitProducer rabbitProducer;
    @Autowired
    private CloudunifyAdminFeign cloudunifyAdminFeign;

    public MessageCode IIIJobDoing(String resulteFile, String xmlTmp, int id,String key) throws Exception {
        ListenableFuture<Response> saberRemove = ListeningExecutorServiceUtil.ListenPool().submit(new RedisRemove(id + "Saber", cloudUnifyRedisFeign));
        ListenableFuture<Response> ibRemove = ListeningExecutorServiceUtil.ListenPool().submit(new RedisRemove(id + "IB", cloudUnifyRedisFeign));
        ListenableFuture<Response> allRemove = ListeningExecutorServiceUtil.ListenPool().submit(new RedisRemove(id + "ALL", cloudUnifyRedisFeign));
        ListenableFuture<Response> fRemove = ListeningExecutorServiceUtil.ListenPool().submit(new RedisRemove(id + "F", cloudUnifyRedisFeign));
        ListenableFuture<Optional<TaskConfigEntity>> task = ListeningExecutorServiceUtil.ListenPool().submit(new taskFindById(300L, taskConfigRepository));


        Futures.addCallback(saberRemove, new CallBackListenPool(),ListeningExecutorServiceUtil.threadPoolExecutor);
        Futures.addCallback(ibRemove, new CallBackListenPool(),ListeningExecutorServiceUtil.threadPoolExecutor);
        Futures.addCallback(allRemove, new CallBackListenPool(),ListeningExecutorServiceUtil.threadPoolExecutor);
        Futures.addCallback(fRemove, new CallBackListenPool(),ListeningExecutorServiceUtil.threadPoolExecutor);
        Futures.addCallback(task, new CallBackListenPool(),ListeningExecutorServiceUtil.threadPoolExecutor);
        ObjectMapper objectMapper = new ObjectMapper();
        log.info(resulteFile+","+xmlTmp+id+"____key:"+key);
        StatisticalReportRequest statisticalReportRequestForIII = TaskUtil.getStatisticalReportRequestForIII(task.get().get(), resulteFile, xmlTmp, id);
        FillReport report = new FillReport();
        ArrayList reportList = new ArrayList();
        boolean fillRequestFile = report.getFillRequestFile(statisticalReportRequestForIII, reportList);
        if (fillRequestFile) {
            reportList.stream().forEach(o -> {
                ReportKidJobEntity reportKidJob1 = new ReportKidJobEntity();
                reportKidJob1.setLevel(3);
                reportKidJob1.setState(1);
                reportKidJob1.setException(xmlTmp);
                reportKidJob1.setRetryCount(0);
                reportKidJob1.setReturnValue(o + "");
                reportKidJob1.setStartTime(System.currentTimeMillis());
                reportKidJob1.setEndTime(null);
                reportKidJob1.setResponse(statisticalReportRequestForIII.getCommonAttributeVal().getResultFile());
                reportKidJob1.setTaskId((long) id);
                try {
                    Response<String> response = cloudUnifyRedisFeign.addString(objectMapper.writeValueAsString(reportKidJob1), id + "|report3");
                    log.info("FillReport 入库成功 ：...." + response.toString());
                    AllObject allObject = new AllObject();
                    allObject.setNum(0);
                    allObject.setObj(o + "");
                    allObject.setId("REPORT-IC|" + response.getData());
                    rabbitProducer.send(allObject, o + ":REPORT-IC|" + response.getData(), 90, true);
                    log.info("3级任务压入队列*****************");
                } catch (Exception e) {
                    log.info("redis 3 :" + e.getMessage());
                }
            });
        }
        ReportTask reportTask = new ReportTask();
        log.info("父id:" + Integer.parseInt(key.substring(0, key.indexOf("|"))));
        reportTask.setId(Integer.parseInt(key.substring(0, key.indexOf("|"))));
        reportTask.setStateInfo("1级任务完成，2级任务完成，3级任务开始...");
        Response response1 = cloudunifyAdminFeign.updateReportTaskById(reportTask);
        return new MessageCode(1, "1111");
    }
}
