package com.dinglicom.mr.service;

import com.dinglicom.mr.entity.DecodeFile;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.DecodeFileRepository;
import com.dinglicom.mr.response.MessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DecodeJobService {
    @Autowired
    private DecodeFileRepository decodeFileRepository;
    @Autowired
    private RabbitProducer rabbitProducer;

    public MessageCode saveDecodeFile(DecodeFile decodeFile) throws Exception {
        DecodeFile save = decodeFileRepository.save(decodeFile);
        if (save.getGroupId() == null ||  save.getFileName() == null){
            return new MessageCode(0,"decode: 入库失败!");
        }
        return new MessageCode<DecodeFile>(1,"decode: 入库成功!",save);
    }
}
