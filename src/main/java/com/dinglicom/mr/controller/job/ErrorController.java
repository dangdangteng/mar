package com.dinglicom.mr.controller.job;

import com.dinglicom.mr.entity.ErrorEntity;
import com.dinglicom.mr.repository.ErrorRepository;
import com.dinglicom.mr.response.MessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/error")
public class ErrorController {
    @Autowired
    private ErrorRepository errorRepository;

    @RequestMapping(value = "/saveErrorMessage",method = RequestMethod.POST)
    public MessageCode saveErrorMessage(@RequestParam String errorMessage) throws Exception{
        ErrorEntity e = new ErrorEntity();
        e.setError(errorMessage);
        ErrorEntity save = errorRepository.save(e);
        return new MessageCode(1,e.getError());
    }
}
