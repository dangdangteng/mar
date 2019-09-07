package com.dinglicom.mr.controller.job;

import com.dinglicom.mr.Enum.StatusEnum;
import com.dinglicom.mr.entity.DecodeFileKidJob;
import com.dinglicom.mr.repository.DecodeFileKidJobRepository;
import com.dinglicom.mr.repository.DecodeFileRepository;
import com.dinglicom.mr.response.MessageCode;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Optional;

@Log
@RestController
@RequestMapping("/job/write/dfkj")
public class JobStatusWriteDecodeFileKidJobTableController {
    @Autowired
    private DecodeFileKidJobRepository decodeFileKidJobRepository;
    @Autowired
    private DecodeFileRepository decodeFileRepository;

    @RequestMapping(value = "/updateStateAndStartTimeById", method = RequestMethod.POST)
    public MessageCode updateStartTimeById(@RequestParam int id) throws Exception {
        int i = decodeFileKidJobRepository.updateStartTimeById(3, id, LocalDateTime.now());
        log.info(i + "================================decodefilekidjob starttime");
        if (i > 0) {
            return new MessageCode(1, StatusEnum.getMessage(3));
        }
        return new MessageCode(0, "更新失败!");
    }

    @RequestMapping(value = "/updateStateAndEndTimeById", method = RequestMethod.POST)
    public MessageCode updateEndTimeById(@RequestParam int id) throws Exception {
        int i = decodeFileKidJobRepository.updateEndTimeById(4, id, LocalDateTime.now());
        if (i > 0) {
            Optional<DecodeFileKidJob> byId = decodeFileKidJobRepository.findById(id);
            if (byId.get().getTaskId() != null || byId.get().getTaskId() != 0) {
                int i1 = decodeFileRepository.updateEndTimeById(byId.get().getTaskId(), 4, LocalDateTime.now());
                log.info(i1 + "-------------------------------------deocefileupdate endtiem");
            }
            log.info(i + "============================decodefilekidjob endtime");
            return new MessageCode(1, StatusEnum.getMessage(4));
        }
        return new MessageCode(0, "更新失败!");
    }
}
