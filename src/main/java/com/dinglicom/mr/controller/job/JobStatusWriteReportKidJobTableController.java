package com.dinglicom.mr.controller.job;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "")
public class JobStatusWriteReportKidJobTableController {
    @RequestMapping(value = "",method = RequestMethod.POST)
    public Boolean retryJob() throws Exception{
        return null;
    }
}
