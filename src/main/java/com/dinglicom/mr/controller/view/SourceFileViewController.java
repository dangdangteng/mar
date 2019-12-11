package com.dinglicom.mr.controller.view;

import com.dinglicom.mr.entity.SourceFileEntity;
import com.dinglicom.mr.entity.page.PageRequestEntity;
import com.dinglicom.mr.repository.SourceFileRepository;
import com.dinglicom.mr.response.MessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sourcefile")
public class SourceFileViewController {
    @Autowired
    private SourceFileRepository sourceFileRepository;

    @RequestMapping(value = "/findAllSourceFile", method = RequestMethod.POST)
    public MessageCode findAllSourceFile(@RequestParam Integer page, @RequestParam Integer size) throws Exception {
        if (page == null || page < 0) {
            return new MessageCode(0, "分页信息不合法: page");
        }
        PageRequestEntity pageRequestEntity = new PageRequestEntity(page, size == null ? 100 : size, new Sort(Sort.Direction.ASC, "id"));
        Page<SourceFileEntity> all = sourceFileRepository.findAll(pageRequestEntity);
        MessageCode<Page<SourceFileEntity>> messageCode = new MessageCode<>();
        messageCode.setCode(1);
        messageCode.setMessage("获取数据成功!");
        messageCode.setData(all);
        return messageCode;
    }
    @RequestMapping(value = "/deleteSourceFileByID" ,method = RequestMethod.DELETE)
    public MessageCode deleteSourceFileByID(@PathVariable Long id) throws Exception{
        sourceFileRepository.deleteById(id);
        MessageCode messageCode = new MessageCode();
        messageCode.setCode(1);
        messageCode.setMessage("删除成功!");
        return messageCode;
    }
}
