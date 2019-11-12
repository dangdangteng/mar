package com.dinglicom.mr.constant;

import cn.hutool.core.net.NetUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import java.security.SecureRandom;
import java.util.Random;
import java.util.UUID;

/**
 * 存在逆向工程大神,这种加密分分钟破解
 */
public class DlingEncrypt {
    private static Logger logger = LoggerFactory.getLogger(DlingEncrypt.class);

    private static long id;

    public static void main(String[] args) throws Exception {
        // test
        String fileName = getFileName();
        System.out.println(getIndex(fileName).length());
        String dinglicom = encrypt(getIndex(fileName), "dinglicom");
        System.out.println(dinglicom);
        String dingliD = decrypt(getIndex(fileName), dinglicom);
        System.out.println(dingliD);
    }

    //产生分布式状态下唯一文件名称的方法,
    //方式获取手机的ipv4 或者host + uuid + 时间戳
    public synchronized static String getFileName() {
        try {
            //获取ipv4
            id = NetUtil.ipv4ToLong(NetUtil.getLocalhostStr());
        } catch (Exception e) {
            //获取host的hashcode
            id = NetUtil.getLocalhost().hashCode();
        }
        //uuid
        String uuidStr = UUID.randomUUID().toString().replace("-", "");
        Random random = new Random();
        //随机字符串标识头
        int i = random.nextInt(10);
        String fileName = i + uuidStr + id + System.currentTimeMillis();
        // 文件名称,在多台设备上也可确保唯一
        System.out.println(fileName);
        return fileName;
    }

    /**
     * 加密后的字段作为秘钥去加密最好
     *
     * @param password 加密密码
     * @param str      设备唯一识别码
     * @return
     */
    public static String encrypt(String password, String str) {
        try {
            byte[] datasource = str.getBytes("UTF-8");
            SecureRandom random = new SecureRandom();
            DESKeySpec desKey = new DESKeySpec(password.getBytes());
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, securekey, random);
            byte[] bytes = cipher.doFinal(datasource);
            String result = Base64.encodeBase64String(bytes);
            return result;
        } catch (Throwable e) {
            logger.error("DES 加密异常，详情：" + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密算法,不做解密以加密后的内容作为秘钥,进行加密
     *
     * @param password
     * @param encryptResult
     * @return
     * @throws Exception
     */
    @Deprecated
    public static String decrypt(String password, String encryptResult) throws Exception {
        byte[] bytes1 = Base64.decodeBase64(encryptResult);
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey = new DESKeySpec(password.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");//返回实现指定转换的 Cipher 对象
        SecretKey securekey = keyFactory.generateSecret(desKey);
        Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, securekey, random);
        byte[] bytes = cipher.doFinal(bytes1);
        String result = new String(bytes);
        return result;
    }

    /**
     * 这个位置也可作为秘钥,简单暴力
     */
    public static String getIndex(String fileName) {
        int i = Integer.parseInt(fileName.substring(0, 1));
        String substring = fileName.substring(i, i + 16);
        return substring;
    }
}
