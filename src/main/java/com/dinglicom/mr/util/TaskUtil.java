package com.dinglicom.mr.util;

import com.dingli.cloudunify.pojo.dto.ReportDto;
import com.dingli.damain.DataSourceItem;
import com.dingli.damain.RequestHeader;
import com.dingli.damain.SiteInfo;
import com.dingli.damain.TaskRequest;
import com.dingli.domain.CommonAttributeVal;
import com.dingli.domain.ItemAttributeVal;
import com.dingli.domain.StatisticalReportRequest;
import com.dinglicom.mr.constant.Constants;
import com.dinglicom.mr.entity.DecodeFileEntity;
import com.dinglicom.mr.entity.SourceFileEntity;
import com.dinglicom.mr.entity.TaskConfigEntity;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


@Log
public class TaskUtil {
    /**
     * RCU
     * @param sourceFileEntity
     * @param taskConfigEntity
     * @return
     * @throws Exception
     */
    public static TaskRequest sourceFileToTaskRequest(SourceFileEntity sourceFileEntity, TaskConfigEntity taskConfigEntity) throws Exception {
        TaskConfigEntity taskConfigEntity1 = taskConfigTm(taskConfigEntity);
        if(taskConfigEntity1 == null){
            taskConfigEntity1.setResultPath(Constants.rucResultPath);
            taskConfigEntity1.setTemplatePath(Constants.rucTempLatePath);
            taskConfigEntity1.setTempPath(Constants.rucTempPath);
            taskConfigEntity1.setConfigPath(Constants.rucConfigPath);
        }
        String tempPath = taskConfigEntity1.getTempPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + sourceFileEntity.getId() + File.separator;
        MakeDir.makeDir(tempPath);
        String resultPath = taskConfigEntity1.getResultPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + sourceFileEntity.getId() + File.separator;
        RequestHeader requestHeader = new RequestHeader(tempPath, resultPath, sourceFileEntity.getId() + "");
        DataSourceItem dataSourceItem = new DataSourceItem(sourceFileEntity.getFilePathName(), String.valueOf(sourceFileEntity.getPort()), "", "", "", "", "");
        SiteInfo siteInfo = new SiteInfo("");
        TaskRequest taskRequest = new TaskRequest(tempPath , requestHeader, dataSourceItem, siteInfo);
        return taskRequest;
    }

    /**
     * DDIB
     * @param decodeFileEntity
     * @param taskConfigEntity
     * @return
     * @throws Exception
     */
    public static TaskRequest decodeFileToTaskRequest(DecodeFileEntity decodeFileEntity, TaskConfigEntity taskConfigEntity) throws Exception {
        TaskConfigEntity taskConfigEntity1 = taskConfigTm(taskConfigEntity);
        if(taskConfigEntity1 == null){
            taskConfigEntity1.setResultPath(Constants.ddibResultPath);
            taskConfigEntity1.setTemplatePath(Constants.ddibTempLatePath);
            taskConfigEntity1.setTempPath(Constants.ddibTempPath);
            taskConfigEntity1.setConfigPath(Constants.ddibConfigPath);
        }
        log.info("id ------------------:" + decodeFileEntity.getId());
        String resultFile = taskConfigEntity1.getResultPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + decodeFileEntity.getId() + File.separator;
        MakeDir.makeDir(resultFile);
        String decodeId = decodeFileEntity.getId() + "";
        String temppath = taskConfigEntity1.getTempPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + decodeId + File.separator;
        MakeDir.makeDir(temppath);
        RequestHeader requestHeader = new RequestHeader(resultFile, decodeId, temppath, resultFile, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        DataSourceItem dataSourceItem = new DataSourceItem(decodeFileEntity.getFileName(), String.valueOf(decodeFileEntity.getPort()), "", "", "", "", "");
        SiteInfo siteInfo = new SiteInfo("");
        TaskRequest request = new TaskRequest(temppath, requestHeader, dataSourceItem, siteInfo);
        return request;
    }

    private static TaskConfigEntity taskConfigTm(TaskConfigEntity taskConfigEntity) {
        if (StringUtils.isEmpty(taskConfigEntity.getConfigPath())
                || StringUtils.isEmpty(taskConfigEntity.getTemplatePath())
                || StringUtils.isEmpty(taskConfigEntity.getTempPath())
        || StringUtils.isEmpty(taskConfigEntity.getResultPath())) {
            return null;
        }
        return taskConfigEntity;
    }

    /**
     * report
     * @param taskConfigEntity
     * @param reportDto
     * @return
     * @throws Exception
     */
    public static List<StatisticalReportRequest> getStatisticalReportRequest(TaskConfigEntity taskConfigEntity, ReportDto reportDto, int id) throws Exception {
        TaskConfigEntity taskConfigEntity1 = taskConfigTm(taskConfigEntity);
        if (reportDto.getData().size() < 0) {
            return null;
        }
        String TempFilePath = taskConfigEntity1.getTempPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + id + File.separator;
        MakeDir.makeDir(TempFilePath);
        String templateFileName = reportDto.getReportItem().getXmlTemplateFile();
        log.info(templateFileName + "------------I级别任务");
        String configFileName = taskConfigEntity1.getConfigPath();
        String filePath = TempFilePath;
        List<StatisticalReportRequest> listStatisticalReportRequest = new ArrayList<>(reportDto.getData().size());
        /**
         *  端口号暂时没有判断异常情况
         */
        String exin = taskConfigEntity1.getTempPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + id + File.separator;
        boolean b = MakeDir.makeDir(exin);
        log.info("生成文件路径!");
        reportDto.getData().stream().forEach(reportFileDto -> {
            //uk 存放路径
            String ResultFile = exin + "report_1_" + UUIDUtils.getUUIDStr() + ".uk";
            log.info("1级别任务,文件存放路径: " + ResultFile);
            CommonAttributeVal commonAttributeVal = new CommonAttributeVal(ResultFile, TempFilePath);
            ItemAttributeVal itemAttributeVal = new ItemAttributeVal(reportFileDto.getFileName(), String.valueOf(reportFileDto.getPort()), null);
            StatisticalReportRequest elementAttribute = new StatisticalReportRequest(commonAttributeVal, itemAttributeVal, templateFileName, configFileName, filePath, id + "");
            listStatisticalReportRequest.add(elementAttribute);
        });
        return listStatisticalReportRequest;
    }

    public static List<List<StatisticalReportRequest>> getStatisticalReportRequest(TaskConfigEntity taskConfigEntity, ReportDto reportDto, int id, List<String> sceneName) throws Exception {
        TaskConfigEntity taskConfigEntity1 = taskConfigTm(taskConfigEntity);
        if (reportDto.getData().size() < 0) {
            return null;
        }
        String TempFilePath = taskConfigEntity1.getTempPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + id + File.separator;
        MakeDir.makeDir(TempFilePath);
        String templateFileName = reportDto.getReportItem().getXmlTemplateFile();
        log.info(templateFileName + "------------I级别任务");
        String configFileName = taskConfigEntity1.getConfigPath();
        String filePath = TempFilePath;
        /**
         *  端口号暂时没有判断异常情况
         */
        String exin = taskConfigEntity1.getTempPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + id + File.separator;
        boolean b = MakeDir.makeDir(exin);
        log.info("生成文件路径!");
        List<List<StatisticalReportRequest>> collect = sceneName.parallelStream().map(s -> {
            List<StatisticalReportRequest> listStatisticalReportRequest = new CopyOnWriteArrayList<>();
            reportDto.getData().parallelStream().forEach(reportFileDto -> {
                //uk 存放路径
                String ResultFile = exin + s + UUIDUtils.getUUIDStr() + ".uk";
                log.info("1级别任务,文件存放路径: " + ResultFile);
                CommonAttributeVal commonAttributeVal = new CommonAttributeVal(ResultFile, TempFilePath);
                ItemAttributeVal itemAttributeVal = new ItemAttributeVal(reportFileDto.getFileName(), String.valueOf(reportFileDto.getPort()), s);
                StatisticalReportRequest elementAttribute = new StatisticalReportRequest(commonAttributeVal, itemAttributeVal, templateFileName, configFileName, filePath, id + "");
                listStatisticalReportRequest.add(elementAttribute);
            });
            return listStatisticalReportRequest;
        }).collect(Collectors.toList());
        return collect;
    }

    /**
     * II 级别任务
     *
     * @param taskConfigEntity
     * @param relist
     * @param reportDto
     * @return
     * @throws Exception
     */
    public static StatisticalReportRequest getStatisticalReportRequestForII(TaskConfigEntity taskConfigEntity, List<String> relist, String reportDto, int id) throws Exception {
        TaskConfigEntity taskConfigEntity1 = taskConfigTm(taskConfigEntity);
        String exin = taskConfigEntity1.getTempPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + id + File.separator;
        String ResultFile = exin + "report_2_" + UUIDUtils.getUUIDStr() + ".uk";
        log.info("2级别任务,文件存放路径: " + ResultFile);
        boolean resultfilepathname = MakeDir.makeDir(exin);
        log.info("结果文件生成路径,生成成功!");
        String TempFilePath = exin;

        String templateFileName = reportDto;
        log.info(templateFileName + "-------------------II级别任务");
        String configFileName = taskConfigEntity1.getConfigPath();
        String filePath = exin;
        CommonAttributeVal commonAttributeVal = new CommonAttributeVal(ResultFile, TempFilePath);
        ArrayList alist = new ArrayList();
        alist.addAll(relist);
        ItemAttributeVal itemAttributeVal = new ItemAttributeVal(alist);
        StatisticalReportRequest elementAttribute = new StatisticalReportRequest(commonAttributeVal, itemAttributeVal, templateFileName, configFileName, filePath, id + "");
        return elementAttribute;
    }

    /**
     * report 简易报表
     *
     * @param taskConfigEntity
     * @param resulteFile
     * @param xmlTmp
     * @param id
     * @return
     * @throws Exception
     */
    public static StatisticalReportRequest getStatisticalReportRequestForIII(TaskConfigEntity taskConfigEntity, String resulteFile, String xmlTmp, int id) throws Exception {
        TaskConfigEntity taskConfigEntity1 = taskConfigTm(taskConfigEntity);
        String exin = taskConfigEntity1.getResultPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + id + File.separator;
        String ResultFile = exin + "sjdl_report.xlsx";
        MakeDir.makeDir(exin);
        String cexin = taskConfigEntity1.getTempPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + id + File.separator;
        String TempFilePath = cexin;
        MakeDir.makeDir(cexin);
        String templateFileName = xmlTmp;
        log.info(templateFileName + "--------------------III级别任务");
        String configFileName = taskConfigEntity1.getConfigPath();
        String filePath = taskConfigEntity1.getTempPath() + DateUtils.longTimeToDateTimeByJodaString() + File.separator + id + File.separator;
        MakeDir.makeDir(filePath);
        CommonAttributeVal commonAttributeVal = new CommonAttributeVal(ResultFile, TempFilePath);
        ArrayList list = new ArrayList();
        list.add(resulteFile);
        ItemAttributeVal itemAttributeVal = new ItemAttributeVal(list);
        StatisticalReportRequest elementAttribute = new StatisticalReportRequest(commonAttributeVal, itemAttributeVal, templateFileName, configFileName, filePath, id + "");
        return elementAttribute;
    }

    public static void main(String[] args) {
        List<String> a = new ArrayList();
        a.add("1");
        a.add("2");
        List<String> b = new ArrayList();
        b.add("a");
        b.add("b");
        b.add("c");
        b.add("d");
        b.add("e");
        List<List<String>> t = new CopyOnWriteArrayList();
        List<List<String>> collect = a.parallelStream().map(s -> {
            List<String> test = new CopyOnWriteArrayList();
            b.parallelStream().forEach(ss -> {
                test.add(ss);
            });
            return test;
        }).collect(Collectors.toList());
        System.out.println(collect.toString());
    }
}
