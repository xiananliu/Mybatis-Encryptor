package com.github.lxn.mybatisEncryptor.extend;

/**
 * 自定义加解密方法
 * Created by lxn on 2019/10/28.
 */
public interface CustomEncryptor {

    /**
     * 加密
     * @param data
     * @return
     */
    String encrpt(String data);


    /**
     * 解密
     * @param data
     * @return
     */
    String decrypt(String data);
}
