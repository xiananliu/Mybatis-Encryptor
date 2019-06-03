package com.github.lxn.mybatisEncryptor.core;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 原始值管理器
 * 记录对象原始值并一键恢复
 * Created by lxn on 2019/6/3.
 */
@Slf4j
public class OriginalValManager {


    private static ThreadLocal<Map<Object, Map<String,Object>>>  originalValue=new ThreadLocal<>();

    /**
     * 清理原始值
     */
    public static void clear(){
        originalValue.remove();
    }
    /**
     * 保存原始值
     * @param object
     * @param field
     */
    public static void needRecoveryAfterProcess(Object object, String field, Object value){
        Map<Object, Map<String,Object>> history= originalValue.get();
        if (history==null){
            history=new HashMap<>();
            originalValue.set(history);
        }
        Map<String,Object> thisObjectHistiory=history.get(object);
        if (thisObjectHistiory==null){
            thisObjectHistiory=new HashMap<>();
            history.put(object,thisObjectHistiory);
        }
        thisObjectHistiory.put(field,value);
    }

    public static void recoveryAll(){
        try {
            Map<Object, Map<String, Object>> history = originalValue.get();
            if (history == null) {
                return;
            }
            for (Map.Entry<Object, Map<String, Object>> entry : history.entrySet()) {
                Object object = entry.getKey();
                for (Map.Entry<String, Object> eachObjectMap : entry.getValue().entrySet()) {
                    BeanAccessUtil.writeValue(object.getClass(), eachObjectMap.getKey(), object, eachObjectMap.getValue());
                }
            }
        }catch (Exception e){
            log.error("自动加解密拦截器原值恢复失败:{}", Throwables.getStackTraceAsString(e));
        }finally {
            clear();
        }
    }

}
