package com.github.lxn.mybatisEncryptor.core;

import lombok.Data;

/**
 * Created by lxn on 2018/10/20.
 */
@Data
public class Setting {

    private String aeskey;

    private String customEncryptorClass;
}
