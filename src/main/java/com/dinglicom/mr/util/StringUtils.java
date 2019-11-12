package com.dinglicom.mr.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class StringUtils {
    public static Integer subToInt(String message){
        String substring = message.substring(message.indexOf(":")+1);
        Integer integer = Integer.valueOf(substring);
        return integer;
    }
    public static Long subToIntTwo(String message){
        String substring = message.substring(message.indexOf("|")+1);
        Long integer = Long.valueOf(substring);
        return integer;
    }
    public static <T> String objectToStr(T t){
        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.convertValue(t, String.class);
        return s;
    }

    public static void main(String[] args) {
        System.out.println(subToIntTwo("REPORT-IA|88"));
    }
}
