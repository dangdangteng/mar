package com.dinglicom.mr.util;

import com.dingli.damain.DataSourceItem;
import com.dingli.damain.RequestHeader;
import com.dingli.damain.SiteInfo;
import com.dingli.damain.TaskRequest;
import com.dinglicom.mr.entity.DecodeFile;
import com.dinglicom.mr.entity.SourceFile;
import com.dinglicom.mr.entity.TaskConfig;
import org.apache.commons.lang3.StringUtils;

import java.io.File;


public class ObjectToObjectUtils {

    public static TaskRequest sourceFileToTaskRequest(SourceFile sourceFile, TaskConfig taskConfig) throws Exception {
        TaskConfig taskConfig1 = taskConfigTm(taskConfig);
        RequestHeader requestHeader = new RequestHeader("",taskConfig1.getTempPath(), taskConfig1.getResultPath());
        DataSourceItem dataSourceItem = new DataSourceItem(sourceFile.getFilePathName(), String.valueOf(sourceFile.getPort()), "", "", "", "", "");
        SiteInfo siteInfo = new SiteInfo("");
        TaskRequest taskRequest = new TaskRequest(taskConfig1.getTempPath(), requestHeader, dataSourceItem, siteInfo);
        return taskRequest;
    }

    public static TaskRequest decodeFileToTaskRequest(DecodeFile decodeFile, TaskConfig taskConfig) throws Exception {
        TaskConfig taskConfig1 = taskConfigTm(taskConfig);
        RequestHeader requestHeader = new RequestHeader("", taskConfig1.getTempPath(), taskConfig1.getResultPath());
        DataSourceItem dataSourceItem = new DataSourceItem(decodeFile.getFileName(),String.valueOf(decodeFile.getPort()),"","","","","");
        SiteInfo siteInfo = new SiteInfo("");
        TaskRequest request = new TaskRequest(taskConfig1.getTempPath(),requestHeader,dataSourceItem,siteInfo);
        return request;
    }

    private static TaskConfig taskConfigTm(TaskConfig taskConfig) {
        if (StringUtils.isEmpty(taskConfig.getConfigPath())
                || StringUtils.isEmpty(taskConfig.getTemplatePath())
                || StringUtils.isEmpty(taskConfig.getTempPath())) {
            return null;
        }
        return taskConfig;
    }

    public static void main(String[] args) {
        String s = " /home/fleet/fleetSwapDatas/swapTemps/decode/";
        //           12345678901234567890123456789012345678901234
        String a = "/";
        s.getBytes();
        if (s.length() > 0 && !"/".equals(s.charAt(s.length() - 1))) {
            System.out.println(File.separator.equals(s.charAt(s.length() - 1)));
            System.out.println(s.charAt(s.length() - 1));
            System.out.println(s.length());
            s += "/";
            System.out.println(s);
            System.out.println(s.charAt(s.length() - 1));
//            byte[] byteData=Encoding.Default.GetBytes(cChar);
        }
    }
}
