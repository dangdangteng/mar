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
import com.dinglicom.mr.entity.DecodeFile;
import com.dinglicom.mr.entity.ReportKidJob;
import com.dinglicom.mr.entity.SourceFile;
import com.dinglicom.mr.entity.TaskConfig;
import lombok.extern.java.Log;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;


@Log
public class TaskUtil {
    /**
     * RCU
     * @param sourceFile
     * @param taskConfig
     * @return
     * @throws Exception
     */
    public static TaskRequest sourceFileToTaskRequest(SourceFile sourceFile, TaskConfig taskConfig) throws Exception {
        TaskConfig taskConfig1 = taskConfigTm(taskConfig);
        if(taskConfig1 == null){
            taskConfig1.setResultPath(Constants.rucResultPath);
            taskConfig1.setTemplatePath(Constants.rucTempLatePath);
            taskConfig1.setTempPath(Constants.rucTempPath);
            taskConfig1.setConfigPath(Constants.rucConfigPath);
        }
        RequestHeader requestHeader = new RequestHeader(taskConfig1.getTempPath(), taskConfig1.getResultPath());
        DataSourceItem dataSourceItem = new DataSourceItem(sourceFile.getFilePathName(), String.valueOf(sourceFile.getPort()), "", "", "", "", "");
        SiteInfo siteInfo = new SiteInfo("");
        TaskRequest taskRequest = new TaskRequest(taskConfig1.getTempPath(), requestHeader, dataSourceItem, siteInfo);
        return taskRequest;
    }

    /**
     * DDIB
     * @param decodeFile
     * @param taskConfig
     * @return
     * @throws Exception
     */
    public static TaskRequest decodeFileToTaskRequest(DecodeFile decodeFile, TaskConfig taskConfig) throws Exception {
        TaskConfig taskConfig1 = taskConfigTm(taskConfig);
        if(taskConfig1 == null){
            taskConfig1.setResultPath(Constants.ddibResultPath);
            taskConfig1.setTemplatePath(Constants.ddibTempLatePath);
            taskConfig1.setTempPath(Constants.ddibTempPath);
            taskConfig1.setConfigPath(Constants.ddibConfigPath);
        }
        log.info("id------------------:" + decodeFile.getId());
        RequestHeader requestHeader = new RequestHeader(taskConfig1.getTempPath(), decodeFile.getId() + "", taskConfig1.getTempPath(), taskConfig1.getResultPath(), System.currentTimeMillis());
        DataSourceItem dataSourceItem = new DataSourceItem(decodeFile.getFileName(), String.valueOf(decodeFile.getPort()), "", "", "", "", "");
        SiteInfo siteInfo = new SiteInfo("");
        TaskRequest request = new TaskRequest(taskConfig1.getTempPath(), requestHeader, dataSourceItem, siteInfo);
        return request;
    }

    private static TaskConfig taskConfigTm(TaskConfig taskConfig) {
        if (StringUtils.isEmpty(taskConfig.getConfigPath())
                || StringUtils.isEmpty(taskConfig.getTemplatePath())
                || StringUtils.isEmpty(taskConfig.getTempPath())
        || StringUtils.isEmpty(taskConfig.getResultPath())) {
            return null;
        }
        return taskConfig;
    }

    /**
     * report
     * @param taskConfig
     * @param reportDto
     * @return
     * @throws Exception
     */
    public static List<StatisticalReportRequest> getStatisticalReportRequest(TaskConfig taskConfig, ReportDto reportDto, int id) throws Exception {
        TaskConfig taskConfig1 = taskConfigTm(taskConfig);
        if (reportDto.getData().size() < 0) {
            return null;
        }
        String TempFilePath = taskConfig1.getTempPath();
        String templateFileName = reportDto.getReportItem().getXmlTemplateFile();
        log.info(templateFileName + "------------I级别任务");
        String configFileName = taskConfig1.getConfigPath();
        String filePath = taskConfig1.getResultPath();
        List<StatisticalReportRequest> listStatisticalReportRequest = new ArrayList<>(reportDto.getData().size());
        /**
         *  端口号暂时没有判断异常情况
         */
        reportDto.getData().stream().forEach(reportFileDto -> {
            String ResultFile = taskConfig1.getResultPath() + UUIDUtils.getUUIDStr() + ".uk";
            CommonAttributeVal commonAttributeVal = new CommonAttributeVal(ResultFile, TempFilePath);
            ItemAttributeVal itemAttributeVal = new ItemAttributeVal(reportFileDto.getFileName(), String.valueOf(reportFileDto.getPort()));
            StatisticalReportRequest elementAttribute = new StatisticalReportRequest(commonAttributeVal, itemAttributeVal, templateFileName, configFileName, filePath, id + "");
            listStatisticalReportRequest.add(elementAttribute);
        });
        return listStatisticalReportRequest;
    }

    /**
     * II 级别任务
     *
     * @param taskConfig
     * @param relist
     * @param reportDto
     * @return
     * @throws Exception
     */
    public static StatisticalReportRequest getStatisticalReportRequestForII(TaskConfig taskConfig, List<String> relist, ReportDto reportDto, int id) throws Exception {
        TaskConfig taskConfig1 = taskConfigTm(taskConfig);
        String ResultFile = taskConfig1.getResultPath() + UUIDUtils.getUUIDStr() + ".uk";
        String TempFilePath = taskConfig1.getTempPath();

        String templateFileName = reportDto.getReportItem().getXmlTemplateFile();
        log.info(templateFileName + "-------------------II级别任务");
        String configFileName = taskConfig1.getConfigPath();
        String filePath = taskConfig1.getResultPath();
        CommonAttributeVal commonAttributeVal = new CommonAttributeVal(ResultFile, TempFilePath);
        ItemAttributeVal itemAttributeVal = new ItemAttributeVal((ArrayList) relist);
        StatisticalReportRequest elementAttribute = new StatisticalReportRequest(commonAttributeVal, itemAttributeVal, templateFileName, configFileName, filePath, id + "");
        return elementAttribute;
    }

    public static StatisticalReportRequest getStatisticalReportRequestForIII(TaskConfig taskConfig, ReportKidJob reportKidJob, ReportDto reportDto, int id) throws Exception {
        TaskConfig taskConfig1 = taskConfigTm(taskConfig);
        if (reportKidJob.getResponse() == null) {
            return null;
        }
        String ResultFile = taskConfig1.getResultPath() + UUIDUtils.getUUIDStr() + ".xlsx";
        String TempFilePath = taskConfig1.getTempPath();

        String templateFileName = reportDto.getReportItem().getXmlTemplateFile();
        log.info(templateFileName + "--------------------III级别任务");
        String configFileName = taskConfig1.getConfigPath();
        String filePath = taskConfig1.getResultPath();
        CommonAttributeVal commonAttributeVal = new CommonAttributeVal(ResultFile, TempFilePath);
        ArrayList list = new ArrayList();
        list.add(reportKidJob.getResponse());
        ItemAttributeVal itemAttributeVal = new ItemAttributeVal(list);
        StatisticalReportRequest elementAttribute = new StatisticalReportRequest(commonAttributeVal, itemAttributeVal, templateFileName, configFileName, filePath, id + "");
        return elementAttribute;
    }

    public static void main(String[] args) {
//        String s = " /home/fleet/fleetSwapDatas/swapTemps/decode/";
//        //           12345678901234567890123456789012345678901234
//        String a = "/";
//        s.getBytes();
//        if (s.length() > 0 && !"/".equals(s.charAt(s.length() - 1))) {
//            System.out.println(File.separator.equals(s.charAt(s.length() - 1)));
//            System.out.println(s.charAt(s.length() - 1));
//            System.out.println(s.length());
//            s += "/";
//            System.out.println(s);
//            System.out.println(s.charAt(s.length() - 1));
////            byte[] byteData=Encoding.Default.GetBytes(cChar);
//        }
    }
}
