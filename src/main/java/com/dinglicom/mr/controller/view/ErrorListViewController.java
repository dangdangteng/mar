package com.dinglicom.mr.controller.view;

import com.dinglicom.mr.entity.ErrorListEntity;
import com.dinglicom.mr.repository.ErrorListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/errorList")
public class ErrorListViewController {
    @Autowired
    private ErrorListRepository errorListRepository;

    @RequestMapping("/findAll")
    public List<ErrorListEntity> findAll(){
        List<ErrorListEntity> all = errorListRepository.findAll();
        return all;
    }
}
