package com.dinglicom.mr.util;

import org.joda.time.DateTime;

import java.io.File;
import java.util.Date;

public class MakeDir {
    public static boolean makeDir(String filename) {
        File file = new File(filename + "1.txt");
        File file1 = file.getParentFile();
        if (!file1.exists()) {
            file1.mkdirs();
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        //为什么不用@Test  包都没引入
        boolean b = makeDir("/Users/saber-opensource/Documents/a");
        System.out.println(b);
        Date date = new Date();
        DateTime dateTime = new DateTime();
        System.out.println(dateTime);
    }
}
