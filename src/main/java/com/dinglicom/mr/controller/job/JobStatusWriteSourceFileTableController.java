package com.dinglicom.mr.controller.job;

import com.dinglicom.mr.entity.DecodeFile;
import com.dinglicom.mr.entity.SourceFile;
import com.dinglicom.mr.response.MessageCode;
import com.dinglicom.mr.service.DDiBJobService;
import com.dinglicom.mr.service.DecodeJobService;
import com.dinglicom.mr.service.SourceJobService;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;


@Log
@RestController
@RequestMapping(value = "/job/write/sourceFile")
public class JobStatusWriteSourceFileTableController {
   @Autowired
   private SourceJobService sourceJobService;
    @Autowired
    private DecodeJobService decodeJobService;
    @Autowired
    private DDiBJobService dDiBJobService;

    @RequestMapping(value = "/callBack",method = RequestMethod.POST)
    public MessageCode workCallBackMessage(@RequestParam int id, @RequestParam int statusCode, @RequestParam String ddibFileName) throws Exception {
        MessageCode messageCode = sourceJobService.UpdateStatusAndEndTime(id, statusCode);
        if (messageCode.getCode() == 0) {
            return messageCode;
        }
        log.info(id+"----"+statusCode+"------"+ddibFileName);
        SourceFile byId = sourceJobService.findById(id);
        DecodeFile decodeFile = new DecodeFile();
        decodeFile.setSourceFileId(id);
        decodeFile.setGroupId(byId.getGroupId());
        decodeFile.setFileName(ddibFileName);
        decodeFile.setPort(byId.getPort());
        decodeFile.setDataType(byId.getDataType());
        decodeFile.setDeviceModel("");
        decodeFile.setFilePathName(byId.getFilePathName());
        decodeFile.setSystemServiceId(1);
        decodeFile.setTotalPointCount(0);
        decodeFile.setTotalGpsPointCount(0);
        decodeFile.setIsDelete(0);
        decodeFile.setCreateDt(LocalDateTime.now());
        MessageCode messageCode1 = decodeJobService.saveDecodeFile(decodeFile);
        if (messageCode1.getCode() != 1 || messageCode1.getData() == null) {
            return messageCode1;
        }
        MessageCode ddib = dDiBJobService.ddib(decodeFile.getId(), decodeFile.getFileName(), decodeFile.getPort(), 20);
        return ddib;
    }
}
