package com.dinglicom.mr.controller.view;

import com.dinglicom.mr.entity.DecodeFileKidJobEntity;
import com.dinglicom.mr.entity.page.PageRequestEntity;
import com.dinglicom.mr.repository.DecodeFileKidJobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/decodeFKJ")
public class DecodeFileKidJobViewController {
    @Autowired
    private DecodeFileKidJobRepository decodeFileKidJobRepository;

    @RequestMapping(value = "/findAll",method = RequestMethod.POST)
    public Page<DecodeFileKidJobEntity> findAll(@RequestParam int page, @RequestParam int size) throws Exception{
        PageRequestEntity pageRequestEntity = new PageRequestEntity(page,size, new Sort(Sort.Direction.ASC,"id"));
        Page<DecodeFileKidJobEntity> all = decodeFileKidJobRepository.findAll(pageRequestEntity);
        return all;
    }

    @RequestMapping(value = "/findByStateNot",method = RequestMethod.POST)
    public Page<DecodeFileKidJobEntity> findByStateNot(@RequestParam int state, @RequestParam int page, @RequestParam int size) throws Exception{
        PageRequestEntity pageRequestEntity = new PageRequestEntity(page,size, new Sort(Sort.Direction.ASC,"id"));
        Page<DecodeFileKidJobEntity> byStateNot = decodeFileKidJobRepository.findByStateNot(state, pageRequestEntity);
        return byStateNot;
    }

}
