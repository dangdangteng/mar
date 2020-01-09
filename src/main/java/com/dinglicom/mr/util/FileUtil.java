package com.dinglicom.mr.util;

import lombok.extern.java.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

@Log
public class FileUtil {
    public static boolean copyFastbiubiu(String sourceFile, String copyFile) {
        File file = new File(sourceFile);
        if (!file.exists()){
            log.info("文件不存在！");
            return false;
        }
        try (
                FileInputStream fileInputStream = new FileInputStream(sourceFile);
                FileOutputStream fileOutputStream = new FileOutputStream(copyFile);
        ) {
            FileChannel infc = null;
            FileChannel onfc = null;
            infc = fileInputStream.getChannel();
            onfc = fileOutputStream.getChannel();
            onfc.transferFrom(infc, 0, infc.size());
            onfc.close();
            infc.close();
            return true;
        } catch (IOException e) {
            log.info(e.toString());
            return false;
        }
    }
    public static boolean deleteBiuBiu(String filePath){
        if (filePath == null){
            return false ;
        }
        File file = new File(filePath);
        if (file.exists()){
            file.delete();
            log.info("file delete ！");
            return true;
        }
        return false;
    }

  public static void main(String[] args) throws InterruptedException {
    //
      String a = "/Users/saber-opensource/dingli/mar/src/main/resources/"+UUIDUtils.getUUIDStr()+12312+".xml";
      int i = a.lastIndexOf("/");
      String substring = a.substring(i);
    System.out.println(substring);
      boolean b = copyFastbiubiu("/Users/saber-opensource/dingli/mar/src/main/resources/生成基站UK.xml",a );
    System.out.println(b);
    Thread.sleep(60000);
    deleteBiuBiu(a);
  }
}
