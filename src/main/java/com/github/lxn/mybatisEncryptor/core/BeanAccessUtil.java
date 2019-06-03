package com.github.lxn.mybatisEncryptor.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by lxn on 2019/6/3.
 */
public class BeanAccessUtil {

    private static Map<Class, Map<String,MethodBox>> cache =new HashMap<>();


    public static void addMethod(Class clazz, String field, Method readMethod,Method writeMethod){

        if(cache.get(clazz)==null){
            cache.put(clazz,new HashMap<>());
        }
        Map<String, MethodBox> propertyDescriptorMap=cache.get(clazz);
        propertyDescriptorMap.put(field,new MethodBox(readMethod,writeMethod));
    }

    /**
     * 读取
     * @param clazz
     * @param field
     * @param object
     * @return
     */
    public static Object readValue(Class clazz, String field,Object object) throws InvocationTargetException, IllegalAccessException {
        Map<String, MethodBox> cacheMethod=cache.get(clazz);
        if (cacheMethod==null){
            return null;
        }
        MethodBox methodBox= cacheMethod.get(field);
        if (methodBox==null){
            return null;
        }
        Object value =methodBox.getReadMethod().invoke(object,null);
        return value;
    }

    /**
     * 读取
     * @param clazz
     * @param field
     * @param object
     * @return
     */
    public static void writeValue(Class clazz, String field,Object object,Object value) throws InvocationTargetException, IllegalAccessException {
        Map<String, MethodBox> cacheMethod=cache.get(clazz);
        if (cacheMethod==null){
            return;
        }
        MethodBox methodBox= cacheMethod.get(field);
        if (methodBox==null){
            return;
        }
        methodBox.getWriteMethod().invoke(object,value);
    }


    public static Set<String> allFields(Class clazz){
        Map<String, MethodBox> cacheMethod=cache.get(clazz);
        if (cacheMethod==null){
            return null;
        }
        return cacheMethod.keySet();
    }

}
