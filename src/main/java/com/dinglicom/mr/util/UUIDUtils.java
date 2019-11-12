package com.dinglicom.mr.util;

import java.util.Random;
import java.util.UUID;

public class UUIDUtils {
    public static String getUUIDStr(){
        Random random = new Random();
        int i = random.nextInt(10);
        String replace = UUID.randomUUID().toString().replace("-", "");
        String sub1 = replace.substring(10,20);
        String sub3 = (System.nanoTime()+""+7758521).substring(i, i+5);
        String replace1 = replace.replace(sub1,sub3);
        return replace1;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(System.nanoTime());
        String uuidStr = getUUIDStr();
        System.out.println(uuidStr);
    }
}
