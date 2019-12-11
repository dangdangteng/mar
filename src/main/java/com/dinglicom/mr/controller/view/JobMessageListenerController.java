package com.dinglicom.mr.controller.view;

import com.dinglicom.mr.entity.JobMessageListenerEntity;
import com.dinglicom.mr.repository.JobMessageListenerRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Log
@RestController
@RequestMapping("/jobmessage")
public class JobMessageListenerController {
    @Autowired
    private JobMessageListenerRepository jobMessageListenerRepository;
    @RequestMapping("/save")
    public void saveIPAndPort(@NotNull @RequestBody JobMessageListenerEntity jobMessageListenerEntity){
        log.info("开始上传work信息...");
        Optional<JobMessageListenerEntity> byIworkIpAndIworkPort = jobMessageListenerRepository.findByIworkIpAndIworkPort(jobMessageListenerEntity.getIworkIp(), jobMessageListenerEntity.getIworkPort());
        if (byIworkIpAndIworkPort.isPresent()) {
            int i = jobMessageListenerRepository.updateJobMessageListenerEntity(jobMessageListenerEntity.getIworkIp(), jobMessageListenerEntity.getIworkPort(), jobMessageListenerEntity.getJobMessage());
            return;
        }
        JobMessageListenerEntity save = jobMessageListenerRepository.save(jobMessageListenerEntity);
    }
}
