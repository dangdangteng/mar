package com.dinglicom.mr.controller.job;

import com.dingli.cloudunify.core.response.Response;
import com.dingli.cloudunify.core.response.ResponseGenerator;
import com.dingli.cloudunify.pojo.dto.ReportDto;
import com.dingli.cloudunify.pojo.dto.ReportFileDto;
import com.dingli.cloudunify.pojo.entity.report.ReportItem;
import com.dinglicom.mr.cc.SiteManagerIFace;
import com.dinglicom.mr.entity.DecodeFileEntity;
import com.dinglicom.mr.repository.DecodeFileRepository;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.service.ReportJobService;
import com.dinglicom.mr.util.FileUtil;
import com.dinglicom.mr.util.SAXxml;
import com.dinglicom.mr.util.UUIDUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author saber-opensource
 * 顶级父接口
 */
@RestController
@RequestMapping("/dingli/report")
public class JobDLiTaskController {
    @Autowired
    private ReportJobService reportJobService;
    @Autowired
    private DecodeFileRepository decodeFileRepository;

    /**
     * 顶级接口
     * 如果传入的参数不带port&filename
     * 就查数据库做写入
     *
     * @return
     * @throws Exception
     */
    @RequestMapping("/task")
    public Response dingLiApi(@RequestBody ReportDto reportDto) throws Exception {
        if (reportDto.getReportItem().getXmlTemplateFile() == null || reportDto.getReportItem().getXmlTemplateFile() == "") {
            return ResponseGenerator.genFailResult("null");
        }
        if (reportDto.getData().get(0).getPort() == null) {
            CopyOnWriteArrayList copyOnWriteArrayList = new CopyOnWriteArrayList();
            reportDto.getData().parallelStream().forEach(reportFileDto -> {
                Long sourceFileId = reportFileDto.getSourceFileId();
                Optional<DecodeFileEntity> byId = decodeFileRepository.findById(sourceFileId);
                ReportFileDto reportFileDto1 = new ReportFileDto();
                reportFileDto1.setPort(byId.get().getPort());
                reportFileDto1.setFileName(byId.get().getFileName());
                reportFileDto1.setSourceFileId(sourceFileId);
                copyOnWriteArrayList.add(reportFileDto1);
            });
            reportDto.setData(copyOnWriteArrayList);
        }
        String xmlTemplateFile = reportDto.getReportItem().getXmlTemplateFile();
        String excelTemplateFile = xmlTemplateFile.substring(0,xmlTemplateFile.lastIndexOf("."))+".xlsx";
        String copyXmlTLF = xmlTemplateFile.substring(0, xmlTemplateFile.lastIndexOf("/")) + File.separator + UUIDUtils.getUUIDStr() + reportDto.getId();
        String xmlNameC = copyXmlTLF + ".xml";
        String excelNameC = copyXmlTLF + ".xlsx";
        boolean b = FileUtil.copyFastbiubiu(xmlTemplateFile, xmlNameC);
        boolean b1 = FileUtil.copyFastbiubiu(excelTemplateFile, excelNameC);
        if (b && b1) {
            ReportItem reportItem = new ReportItem().setXmlTemplateFile(xmlNameC);
            reportDto.setReportItem(reportItem);
        }
        int p = (reportDto.getPriority() == null ? 50 : reportDto.getPriority());
        String xmlElementSet = SAXxml.getXmlElementSet(reportDto.getReportItem().getXmlTemplateFile());
        if ("1".equals(xmlElementSet)) {
            MessageCode messageCode = reportJobService.reportJobI(reportDto, reportDto.getId(), p);
            return ResponseGenerator.genSuccessResult(messageCode);
        }
        File file = new File(xmlElementSet);
        if (!file.exists()) {
            String ukPath = SiteManagerIFace.SITE_MANAGER_I_FACE.CreateSiteManagerHandle(xmlElementSet);
            SAXxml.addUkElement(reportDto.getReportItem().getXmlTemplateFile(), ukPath);
        }
        MessageCode messageCode = reportJobService.reportJobI(reportDto, reportDto.getId(), p);
        return ResponseGenerator.genSuccessResult(messageCode);
    }
}
