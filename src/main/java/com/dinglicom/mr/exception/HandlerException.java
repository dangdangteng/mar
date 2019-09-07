package com.dinglicom.mr.exception;

import lombok.extern.java.Log;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @author saber-opensource
 */
@Log
@RestControllerAdvice
public class HandlerException {

    @InitBinder
    public void init(WebDataBinder webDataBinder) {

    }

    @ModelAttribute
    public void addAttribute(Model model) {
        model.addAttribute("author", "ez");
    }
    @ExceptionHandler(value = Exception.class)
    public Map errMessage(Exception e){
        Map map = new HashMap(16);
        map.put("code", 500);
        map.put("message",e.getMessage());
        return map;
    }
}
