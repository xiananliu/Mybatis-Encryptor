package com.github.lxn.mybatisEncryptor.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

/**
 * 对象访问方法
 * Created by lxn on 2019/6/3.
 */
@AllArgsConstructor
@Data
class MethodBox{


    private Method readMethod;

    private Method writeMethod;

}
