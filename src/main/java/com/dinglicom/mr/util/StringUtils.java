package com.dinglicom.mr.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StringUtils {
    public static Integer subToInt(String message){
        String substring = message.substring(message.indexOf(":")+1);
        Integer integer = Integer.valueOf(substring);
        return integer;
    }
    public <T> String objectToStr(T t){
        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.convertValue(t, String.class);
        return s;
    }

    public static void main(String[] args) {
        System.out.println(subToInt("Rcu: 1"));
    }
}
