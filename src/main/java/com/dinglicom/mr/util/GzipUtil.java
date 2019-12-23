package com.dinglicom.mr.util;

import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GzipUtil {

    public static String gzip(String str) throws IOException {
        if (str == null || str.isEmpty()) {
            return str;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(out);) {
            gzip.write(str.getBytes());
        }
        return Base64.encodeBase64String(out.toByteArray());
    }

    public static String gunzip(String compressedStr) throws IOException {
        if (compressedStr == null || compressedStr.isEmpty()) {
            return compressedStr;
        }
        byte[] compressed = Base64.decodeBase64(compressedStr);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayInputStream in = new ByteArrayInputStream(compressed);
        try (GZIPInputStream ginzip = new GZIPInputStream(in);) {
            byte[] buffer = new byte[4096];
            int len = -1;
            while ((len = ginzip.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
        }
        return out.toString();
    }
  public static void main(String[] args) throws Exception{
    //
      String ni = "nifdfafasfdsafaffddddddddddddfffffffsafsdfafdfasdffafdsafsdfsafasdfasdfasdfasfasfsfsfsdfasfasfaffsfffsdffsdfsfasfasfdfjjkjkjlgnixnfiaffafdsfafadfafdsfafdsfasfdasdfasdfafafkgjkjiwnfisjajdfkajdflafkalfdalfjaslfdjalkfdjhao";
      String gzip = gzip(ni);
      String gunzip = gunzip(gzip);
    System.out.println(gzip);
    System.out.println(gunzip);
  }
}
