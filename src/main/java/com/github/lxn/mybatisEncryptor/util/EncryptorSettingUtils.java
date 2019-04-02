package com.github.lxn.mybatisEncryptor.util;

import com.github.lxn.mybatisEncryptor.core.Settings;

import java.io.FileNotFoundException;

/**
 * Created by lxn on 2019/4/2.
 */
public class EncryptorSettingUtils {

    private static Settings settings;


    public static Settings getSettings() {
        return settings;
    }

    public static Settings getSettings(String configLocation) {
        if (settings==null){
            try {
                settings=XmlUtil.readXml(Settings.class,configLocation);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("db加解密拦截器配置文件加载异常",e.fillInStackTrace());
            }
        }
        return settings;
    }


}
