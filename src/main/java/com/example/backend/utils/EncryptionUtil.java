package com.example.backend.utils;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.AES;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;

public class EncryptionUtil {
    private static final String SALT = "UwFOvC7fTFgWYaTy";
    /**
     * 加密
     * @param password 明文密码
     * @return
     */
    public static String encrypt(String password) {
        // 生成密钥
        byte[] encoded = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue(), SALT.getBytes()).getEncoded();
        // 加密
        AES aes = SecureUtil.aes(encoded);
        return aes.encryptBase64(password);
    }
    /**
     * 解密
     * @param password 明文密码
     * @return
     */
    public static String decrypts(String password) {
        // 生成密钥
        byte[] encoded = SecureUtil.generateKey(SymmetricAlgorithm.AES.getValue(), SALT.getBytes()).getEncoded();
        // 解密
        AES aes = SecureUtil.aes(encoded);
        return aes.decryptStr(password);
    }

    /**
     * 判断密码是否正确
     * @param password 前端发送的密码
     * @param encryptedPassword 该用户加密后密码
     * @return
     */
    public static Boolean checkPassword(String password, String encryptedPassword) {
        return encryptedPassword.equals(encrypt(password));
    }
}
