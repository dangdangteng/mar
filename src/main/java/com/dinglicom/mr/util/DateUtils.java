package com.dinglicom.mr.util;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtils {
    //并发不可靠
    @Deprecated
    public static String longTimeToDateByJDKString(long longTime) {
        Date date = new Date(longTime);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = simpleDateFormat.format(date);
        return format;
    }
    //线程安全并发下不会出问题,无阻塞,响应快速
    public static String longTimeToDateTimeByJodaString(long longTime) {
        DateTime dateTime = new DateTime(longTime);
        return dateTime.toString("yyyy-MM-dd HH:mm:ss");
    }
    //long 转 localdatatime 安全并发下无阻塞
    public static LocalDateTime longTimeToLocalDateTime(long longTime) {
        DateTime dateTime = new DateTime(longTime);
        LocalDateTime localDateTime = dateTime.toLocalDateTime();
        return localDateTime;
    }

    public static void main(String[] args) {
        long l = System.currentTimeMillis();
        LocalDateTime localDateTime = longTimeToLocalDateTime(l);
        System.out.println(localDateTime);
    }
}
