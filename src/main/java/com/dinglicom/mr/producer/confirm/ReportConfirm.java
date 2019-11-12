package com.dinglicom.mr.producer.confirm;

import com.dinglicom.mr.Enum.StatusEnum;
import com.dinglicom.mr.entity.correlationdata.AllObject;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import com.dinglicom.mr.service.ReportJobService;
import com.dinglicom.mr.util.StringUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Log
@Transactional
@Service
public class ReportConfirm {
    @Autowired
    private ReportJobService reportJobService;
    @Autowired
    private ReportKidJobRepository reportKidJobRepository;

    public void ReportRetryAndUpdate(String correlationData, boolean ack) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AllObject allObject = mapper.readValue(correlationData, AllObject.class);
        Long id = StringUtils.subToIntTwo(allObject.getId());
        log.info(id + "=------------=" + allObject.toString());
        if (ack) {
            //如果confirm返回成功 则进行更新
            try {
                int i = reportKidJobRepository.updateReportKidJobStateAndStartTimeById(2, id, LocalDateTime.now(), StatusEnum.getMessage(2));
                log.info("report kid job 更新状态: " + i);
            } catch (Exception e) {
                log.info(e.getMessage());
            }
        } else {
            log.info("异常补救...");
            if (allObject.getNum() < 3) {
                try {
                    reportJobService.retryReportI(allObject);
                } catch (Exception e) {
                    log.info(e.getMessage());
                }
            } else {
                log.info("无法压入队列,请排查队列状态!");
            }

        }
    }
}
