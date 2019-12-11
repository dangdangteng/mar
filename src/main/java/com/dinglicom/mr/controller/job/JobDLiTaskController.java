package com.dinglicom.mr.controller.job;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dingli.cloudunify.pojo.dto.ReportDto;
import com.dinglicom.mr.cc.SiteManagerIFace;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.service.ReportJobService;
import com.dinglicom.mr.util.SAXxml;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;

/**
 * @author saber-opensource
 * 顶级父接口
 */
@RestController
@RequestMapping("/dingli/report")
public class JobDLiTaskController {
    @Autowired
    private ReportJobService reportJobService;
    /**
     * 顶级接口
     * @return
     * @throws Exception
     */
    @RequestMapping("/task")
    public Response dingLiApi(@RequestBody ReportDto reportDto) throws Exception {
        int p = (reportDto.getPriority() == null ? 50 : reportDto.getPriority());
        if (reportDto.getReportItem().getXmlTemplateFile() == null || reportDto.getReportItem().getXmlTemplateFile() == ""){
            return null;
        }
        String xmlElementSet = SAXxml.getXmlElementSet(reportDto.getReportItem().getXmlTemplateFile()   );
        if ("1".equals(xmlElementSet)) {
            MessageCode messageCode = reportJobService.reportJobI(reportDto, reportDto.getId(), p);
            return ResponseGenerator.genSuccessResult(messageCode);
        }
        //针对每个任务请求一次c 合理吗
        String message = "信息以及更新";
        File file = new File(xmlElementSet);
        if (!file.exists() || message.equals("")) {
            String ukPath = SiteManagerIFace.SITE_MANAGER_I_FACE.CreateSiteManagerHandle(xmlElementSet);
            SAXxml.addUkElement(reportDto.getReportItem().getXmlTemplateFile(),ukPath);
        }
        //这里一定要等 uk 产生在做report 任务,否则会出错,不能并发执行
        MessageCode messageCode = reportJobService.reportJobI(reportDto, reportDto.getId(), p);
        return ResponseGenerator.genSuccessResult(messageCode);
    }
}
