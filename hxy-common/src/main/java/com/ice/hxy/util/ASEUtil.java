package com.ice.hxy.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * @Author ice
 * @Date 2023/5/19 16:47
 * @Description: ase 加密
 */
public class ASEUtil {

    // 加密算法
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    // 字符编码
    private static final String CHARSET = "UTF-8";

    /**
     * 加密
     *
     * @param content  需要加密的原始字符串
     * @param key      加密使用的密钥，长度必须为16位
     * @param ivString 加密使用的向量，长度必须为16位
     * @return 加密后的字符串
     */
    public static String encrypt(String content, String key, String ivString) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(CHARSET), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(ivString.getBytes(CHARSET));
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedByte = cipher.doFinal(content.getBytes(CHARSET));
        return Base64.getEncoder().encodeToString(encryptedByte);
    }



    /**
     * 解密
     *
     * @param content  需要解密的字符串
     * @param key      解密时使用的密钥，长度必须为16位
     * @param ivString 解密时使用的向量，长度必须为16位
     * @return 解密后的原始字符串
     */
    public static String decrypt(String content, String key, String ivString) throws Exception {
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(CHARSET), "AES");
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        IvParameterSpec ivSpec = new IvParameterSpec(ivString.getBytes(CHARSET));
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
        byte[] encryptedByte = Base64.getDecoder().decode(content);
        byte[] originalByte = cipher.doFinal(encryptedByte);
        return new String(originalByte, CHARSET);
    }


}
