package com.dinglicom.mr.controller.job;

import com.dinglicom.mr.entity.DecodeFileEntity;
import com.dinglicom.mr.entity.SourceFileEntity;
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
    public MessageCode workCallBackMessage(@RequestParam Long id, @RequestParam int statusCode, @RequestParam String ddibFileName) throws Exception {
        sourceJobService.updateStatusAndEndTime(id, statusCode);
        log.info(id+"----"+statusCode+"------"+ddibFileName);
        SourceFileEntity byId = sourceJobService.findById(id);
        DecodeFileEntity decodeFileEntity = new DecodeFileEntity();
        decodeFileEntity.setSourceFileId(id);
        decodeFileEntity.setGroupId(byId.getGroupId());
        decodeFileEntity.setFileName(ddibFileName);
        decodeFileEntity.setPort(byId.getPort());
        decodeFileEntity.setDataType(byId.getDataType());
        decodeFileEntity.setDeviceModel("");
        decodeFileEntity.setFilePathName(byId.getFilePathName());
        decodeFileEntity.setSystemServiceId(1);
        decodeFileEntity.setTotalPointCount(0);
        decodeFileEntity.setTotalGpsPointCount(0);
        decodeFileEntity.setIsDelete(0);
        decodeFileEntity.setCreateDt(LocalDateTime.now());
        decodeFileEntity.setStartDt(LocalDateTime.now());
        MessageCode<DecodeFileEntity> messageCode1 = decodeJobService.saveDecodeFile(decodeFileEntity);
        if (messageCode1.getCode() != 1 || messageCode1.getData() == null) {
            return messageCode1;
        }
        MessageCode ddib = dDiBJobService.ddib(messageCode1.getData().getId(), decodeFileEntity.getFileName(), decodeFileEntity.getPort(), 10);
        return ddib;
    }
}
