package com.dinglicom.mr.producer.confirm;

import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.repository.SourceFileRepository;
import com.dinglicom.mr.service.RcuJobService;
import com.dinglicom.mr.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Log
@Component
public class RcuConfirm {
    @Autowired
    private RcuJobService rcuJobService;
    @Autowired
    private SourceFileRepository sourceFileRepository;

    public void RcuRetryAndUpdate(String correlationData, boolean ack) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AllObject allObject = mapper.readValue(correlationData, AllObject.class);
        Integer id = StringUtils.subToInt(allObject.getId());
        if (ack) {
            LocalDateTime localDateTime = LocalDateTime.now();
            //如果confirm返回成功 则进行更新
            int i = sourceFileRepository.updateStartTimeById(2, id, localDateTime);
            log.info("source_file 更新状态: " + i);
        } else {
            //todo: 消息压入队列 是否确保一定压入,如果无法压入这里会进入一个无限retry的状态
            log.info("异常补救...");
            if (allObject.getNum() < 3) {
                try {
                    rcuJobService.retryRcu(allObject);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            } else {
                log.info("无法压入队列,请排查队列状态!");

            }

        }
    }
}
