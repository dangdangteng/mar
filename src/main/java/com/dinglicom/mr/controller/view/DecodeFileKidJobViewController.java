package com.dinglicom.mr.controller.view;

import com.dinglicom.mr.entity.DecodeFileKidJob;
import com.dinglicom.mr.entity.page.PageRequest;
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
    public Page<DecodeFileKidJob> findAll(@RequestParam int page, @RequestParam int size) throws Exception{
        PageRequest pageRequest = new PageRequest(page,size, new Sort(Sort.Direction.ASC,"id"));
        Page<DecodeFileKidJob> all = decodeFileKidJobRepository.findAll(pageRequest);
        return all;
    }

    @RequestMapping(value = "/findByStateNot",method = RequestMethod.POST)
    public Page<DecodeFileKidJob> findByStateNot(@RequestParam int state, @RequestParam int page, @RequestParam int size) throws Exception{
        PageRequest pageRequest = new PageRequest(page,size, new Sort(Sort.Direction.ASC,"id"));
        Page<DecodeFileKidJob> byStateNot = decodeFileKidJobRepository.findByStateNot(state, pageRequest);
        return byStateNot;
    }

}
