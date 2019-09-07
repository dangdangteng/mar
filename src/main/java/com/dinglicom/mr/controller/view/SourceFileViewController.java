package com.dinglicom.mr.controller.view;

import com.dinglicom.mr.entity.SourceFile;
import com.dinglicom.mr.entity.page.PageRequest;
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
        PageRequest pageRequest = new PageRequest(page, size == null ? 100 : size, new Sort(Sort.Direction.ASC, "id"));
        Page<SourceFile> all = sourceFileRepository.findAll(pageRequest);
        MessageCode<Page<SourceFile>> messageCode = new MessageCode<>();
        messageCode.setCode(1);
        messageCode.setMessage("获取数据成功!");
        messageCode.setData(all);
        return messageCode;
    }
    @RequestMapping(value = "/deleteSourceFileByID" ,method = RequestMethod.DELETE)
    public MessageCode deleteSourceFileByID(@PathVariable int id) throws Exception{
        int i = sourceFileRepository.deleteById(id);
        MessageCode messageCode = new MessageCode();
        messageCode.setCode(1);
        messageCode.setMessage("删除成功!");
        return messageCode;
    }
}
