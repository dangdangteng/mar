//package com.dinglicom.mr.service;
//
//import com.dingli.cloudunify.core.response.Response;
//import com.dingli.cloudunify.core.response.ResponseGenerator;
//import com.dingli.cloudunify.pojo.entity.report.ReportTask;
//import com.dingli.domain.StatisticalReportRequest;
//import com.dingli.fillReport.FillReport;
//import com.dingli.merger.Merger;
//import com.dingli.simplifyStatistics.SimplifyStatistics;
//import com.dinglicom.mr.Enum.StatusEnum;
//import com.dinglicom.mr.entity.ReportKidJob;
//import com.dinglicom.mr.entity.TaskConfig;
//import com.dinglicom.mr.entity.correlationdata.AllObject;
//import com.dinglicom.mr.handle.HandleIndexOf;
//import com.dinglicom.mr.repository.TaskConfigRepository;
//import com.dinglicom.mr.response.MessageCode;
//import com.dinglicom.mr.util.TaskUtil;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.java.Log;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Optional;
//
//@Log
//@Service("easy-report")
//public class EasyReportService implements HandleIndexOf {
//
//    @Autowired
//    private TaskConfigRepository taskConfigRepository;
//
//    @Override
//    @Async
//    public Response jobDoing(String jsonObj) throws Exception {
//        Optional<TaskConfig> byId = taskConfigRepository.findById(300);
//        log.info("=============" + id);
//        /**
//         * 一级任务
//         */
//        List<StatisticalReportRequest> elementAttribute = TaskUtil.getStatisticalReportRequest(byId.get(), reportDto);
//        List<String> reportI = new ArrayList<>();
//        ObjectMapper mapper = new ObjectMapper();
//        if (elementAttribute == null) {
//            return ResponseGenerator.genFailResult("失败!");
//        }
//        log.info("*elementAttribute : " + elementAttribute.toString() + "-----------");
//        List errorList = new ArrayList();
//        Response response1 = cloudUnifyRedisFeign.addNum(elementAttribute.size()+"", id + "IA");
//        elementAttribute.stream().forEach(elementAttribute2 -> {
//            if (StringUtils.isEmpty(elementAttribute2.getItemAttributeVal().getFileName())) {
//                log.info("数据源ddib文件为空");
//                errorList.add(elementAttribute2.toString());
//                return;
//            }
//            reportI.add(elementAttribute2.getCommonAttributeVal().getResultFile());
//            SimplifyStatistics simplifyStatistics = new SimplifyStatistics();
//            log.info(elementAttribute2.toString() + "----" + elementAttribute2.getCommonAttributeVal().toString() + "--------" + elementAttribute2.getItemAttributeVal().toString());
//            ArrayList simplifyList = new ArrayList();
//            Boolean simplifyStatisticRequestFile = null;
//            try {
//                simplifyStatisticRequestFile = simplifyStatistics.getSimplifyStatisticRequestFile(elementAttribute2, simplifyList);
//            } catch (Exception e) {
//                log.info("调超强任务接口失败!"+e.toString());
//            }
//            if (simplifyStatisticRequestFile) {
//                simplifyList.parallelStream().forEach(o -> {
//                    try {
//                        ReportKidJob reportKidJob = new ReportKidJob();
//                        reportKidJob.setLevel(1);
//                        reportKidJob.setState(1);
//                        reportKidJob.setException(StatusEnum.getMessage(1));
//                        reportKidJob.setRetryCount(0);
//                        reportKidJob.setReturnValue(o + "");
//                        reportKidJob.setStartTime(System.currentTimeMillis());
//                        reportKidJob.setEndTime(null);
//                        reportKidJob.setTaskId(id);
//                        reportKidJob.setResponse(elementAttribute2.getCommonAttributeVal().getResultFile());
//                        String s = mapper.writeValueAsString(reportKidJob);
//                        Response<String> response = cloudUnifyRedisFeign.addString(s, id + "|report1");
//                        log.info("SimplifyStatistics 入库成功 ：...." + response.toString());
//                        AllObject allObject = new AllObject();
//                        allObject.setId("REPORT-IA|" + response.getData());
//                        allObject.setObj(o + ":" + "REPORT-IA|" + response.getData());
//                        allObject.setNum(0);
//                        rabbitProducer.send(allObject, allObject.getObj(), priority == null ? 50 : priority, true);
//                        log.info("消息压入队列: ...");
//                    } catch (Exception e) {
//                        log.info("I 级别任务出错 ：" + e.getMessage());
//                    }
//                });
//            } else {
//                return;
//            }
//        });
//        /**
//         * 校验1级任务的合法性
//         */
//        if (elementAttribute.size() == errorList.size()) {
//            ReportKidJob reportKidJob = new ReportKidJob();
//            reportKidJob.setException("没有读取到文件名称");
//            reportKidJob.setStartTime(System.currentTimeMillis());
//            reportKidJobRepository.save(reportKidJob);
//            log.info("数据源文件存在问题...");
//            return new MessageCode(0, "数据源文件存在问题...");
//        }
//        /**
//         * 二级任务
//         */
//        StatisticalReportRequest elementAttributeForII = TaskUtil.getStatisticalReportRequestForII(byId.get(), reportI, reportDto);
//        if (elementAttributeForII == null) {
//            return new MessageCode(0, "数据有误！");
//        }
//        log.info("**elementAttributeForII :" + elementAttributeForII.toString());
//        Merger merger = new Merger();
//        ArrayList mergerList = new ArrayList();
//        boolean mergerRequestFile = merger.getMergerRequestFile(elementAttributeForII, mergerList);
//        ReportKidJob reportKidJob = new ReportKidJob();
//        List jobList = new ArrayList();
//        if (mergerRequestFile) {
//            mergerList.stream().forEach(o -> {
//                reportKidJob.setLevel(2);
//                reportKidJob.setState(1);
//                reportKidJob.setException(StatusEnum.getMessage(1));
//                reportKidJob.setRetryCount(0);
//                reportKidJob.setReturnValue(o + "");
//                reportKidJob.setStartTime(System.currentTimeMillis());
//                reportKidJob.setEndTime(null);
//                reportKidJob.setResponse(elementAttributeForII.getCommonAttributeVal().getResultFile());
//                reportKidJob.setTaskId(id);
////                ReportKidJob save = reportKidJobRepository.save(reportKidJob);
//                try {
//                    String s = mapper.writeValueAsString(reportKidJob);
//                    Response<String> response = cloudUnifyRedisFeign.addString(s, id + "|report2");
//                    log.info("merger 入库成功 ：...." + response.toString());
//                } catch (Exception e) {
//                    log.info("redis : " + e.getMessage());
//                }
//            });
//        } else {
//            return new MessageCode(0, elementAttributeForII.toString());
//        }
//
//        StatisticalReportRequest elementAttributeForIII = TaskUtil.getStatisticalReportRequestForIII(byId.get(), reportKidJob, reportDto);
//        FillReport report = new FillReport();
//        ArrayList reportList = new ArrayList();
//        boolean fillRequestFile = report.getFillRequestFile(elementAttributeForIII, reportList);
//        if (fillRequestFile) {
//            reportList.parallelStream().forEach(o -> {
//                ReportKidJob reportKidJob1 = new ReportKidJob();
//                reportKidJob1.setLevel(3);
//                reportKidJob1.setState(1);
//                reportKidJob1.setException(StatusEnum.getMessage(1));
//                reportKidJob1.setRetryCount(0);
//                reportKidJob1.setReturnValue(o + "");
//                reportKidJob1.setStartTime(System.currentTimeMillis());
//                reportKidJob1.setEndTime(null);
//                reportKidJob1.setResponse(elementAttributeForIII.getCommonAttributeVal().getResultFile());
//                reportKidJob1.setTaskId(id);
//                try {
//                    Response<String> response = cloudUnifyRedisFeign.addString(mapper.writeValueAsString(reportKidJob1), id + "|report3");
//                    log.info("FillReport 入库成功 ：...." + response.toString());
//                } catch (Exception e) {
//                    log.info("redis 3 :" + e.getMessage());
//                }
//            });
//        }
////        }
//        ReportTask reportTask = new ReportTask();
//        reportTask.setId(id);
//        reportTask.setStatus(2);
//        reportTask.setStateInfo("1级任务已经压入队列");
//        Response response = cloudunifyAdminFeign.updateReportTaskById(reportTask);
//        return new MessageCode(1, response.getMessage(), reportTask);
//    }
//}
