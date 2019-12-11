package com.dinglicom.mr.controller.view;

import com.dinglicom.mr.entity.ReportKidJobEntity;
import com.dinglicom.mr.entity.page.PageRequestEntity;
import com.dinglicom.mr.repository.ReportKidJobRepository;
import com.dinglicom.mr.response.MessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/reportkidjob")
public class ReportKidJobViewController {
    @Autowired
    private ReportKidJobRepository reportKidJobRepository;

    @RequestMapping(value = "/findAll", method = RequestMethod.POST)
    public MessageCode findAll(@RequestParam Integer page, @RequestParam Integer size) throws Exception {
        if (page == null || page < 0) {
            return new MessageCode(0, "分页信息不合法: page");
        }
        PageRequestEntity pageRequestEntity = new PageRequestEntity(page, size == null ? 100 : size, new Sort(Sort.Direction.ASC, "id"));
        Page<ReportKidJobEntity> all = reportKidJobRepository.findAll(pageRequestEntity);
        MessageCode<Page<ReportKidJobEntity>> messageCode = new MessageCode<>();
        messageCode.setCode(1);
        messageCode.setMessage("获取数据成功!");
        messageCode.setData(all);
        return messageCode;
    }
    @RequestMapping(value = "/findAllByState", method = RequestMethod.POST)
    public MessageCode findAllByState(@RequestParam Integer page, @RequestParam Integer size) throws Exception {
        if (page == null || page < 0) {
            return new MessageCode(0, "分页信息不合法: page");
        }
        PageRequestEntity pageRequestEntity = new PageRequestEntity(page, size == null ? 100 : size, new Sort(Sort.Direction.ASC, "id"));
        Page<ReportKidJobEntity> all = reportKidJobRepository.findAllByStateNot(4, pageRequestEntity);
        MessageCode<Page<ReportKidJobEntity>> messageCode = new MessageCode<>();
        messageCode.setCode(1);
        messageCode.setMessage("获取数据成功!");
        messageCode.setData(all);
        return messageCode;
    }
}
