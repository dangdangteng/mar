package com.dinglicom.mr.service;

import com.dinglicom.mr.Enum.CErrorCodeEnum;
import com.dinglicom.mr.entity.SourceFile;
import com.dinglicom.mr.repository.SourceFileRepository;
import com.dinglicom.mr.response.MessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SourceJobService {
    @Autowired
    private SourceFileRepository sourceFileRepository;

    public MessageCode UpdateStatusAndEndTime(int id,int statusCode) throws Exception {
        LocalDateTime time = LocalDateTime.now();
        if (statusCode == 2){
            int i = sourceFileRepository.updateStatusAndEndDtById(4, id, time);
            return new MessageCode(1,CErrorCodeEnum.getMessage(2));
        }
        int i = sourceFileRepository.updateStatusAndEndDtById(0, id, time);
        return new MessageCode(0,CErrorCodeEnum.getMessage(statusCode));
    }
    public SourceFile findById(int id) throws Exception{
        Optional<SourceFile> byId = sourceFileRepository.findById(id);
        return byId.get();
    }
}
