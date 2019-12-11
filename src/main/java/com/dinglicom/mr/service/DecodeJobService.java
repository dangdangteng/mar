package com.dinglicom.mr.service;

import com.dinglicom.mr.entity.DecodeFileEntity;
import com.dinglicom.mr.producer.RabbitProducer;
import com.dinglicom.mr.repository.DecodeFileRepository;
import com.dinglicom.mr.response.MessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DecodeJobService {
    @Autowired
    private DecodeFileRepository decodeFileRepository;
    @Autowired
    private RabbitProducer rabbitProducer;

    public MessageCode saveDecodeFile(DecodeFileEntity decodeFileEntity) throws Exception {
        DecodeFileEntity save = decodeFileRepository.save(decodeFileEntity);
        if (save.getGroupId() == null ||  save.getFileName() == null){
            return new MessageCode(0,"decode: 入库失败!");
        }
        return new MessageCode<DecodeFileEntity>(1,"decode: 入库成功!",save);
    }
}
