package com.github.lxn.mybatisEncryptor.core;

import com.github.lxn.mybatisEncryptor.util.AESCoder;
import com.github.lxn.mybatisEncryptor.util.XmlUtil;
import com.google.common.base.Throwables;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.util.StringUtils;

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static java.util.Locale.ENGLISH;

/**
 * db自动加解密拦截器
 * Created by lxn on 2018/9/16.
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class }),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class }) })
public class DBInterceptor implements Interceptor {


    private Map<Class,Map<String,MethodBox>> cache =new HashMap<>();

    private Settings settings;


//
//    /**
//     * 注解扫描
//     * @param basePackage 扫描的路径
//     * @return List<ScanResult> 结果
//     */
//    private List<ScanResult> scanAnnotationFrom(String basePackage){
//        List<ScanResult> results=new ArrayList<>();
//
//        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
//        final String resourcePattern = "/**/*.class";
//        String pattern = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(basePackage)
//                + resourcePattern;
//        try {
//            Resource[] resources = resourcePatternResolver.getResources(pattern);
//            MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
//            for (Resource resource : resources) {
//                if (resource.isReadable()) {
//                    MetadataReader reader = readerFactory.getMetadataReader(resource);
//                    //扫描到的class
//                    String className = reader.getClassMetadata().getClassName();
//                    Class<?> clazz = Class.forName(className);
//                    Field[] fields = clazz.getDeclaredFields();
//                    if (fields==null){
//                        continue;
//                    }
//                    for (Field eachField:fields){
//                        AutoEncrypt autoEncrypt=eachField.getAnnotation(AutoEncrypt.class);
//                        if (autoEncrypt!=null){
//                            //找到注解，加入配置
//                            results.add(new ScanResult(clazz,eachField.getName(),eachField.getType()));
//                        }
//                    }
//                }
//            }
//        } catch (Exception e) {
//            log.error("自动加解密扫描包异常basePackage:{},e:{}",basePackage,e);
//        }
//
//        return results;
//    }






    /**
     * 扫描注解结果
     */

    @AllArgsConstructor
    @Data
    class ScanResult{
        private Class aClass;
        private String field;
        private Class fieldClass;
    }

    /**
     * 批量添加映射
     * @param list
     */
    private void addBeanPropertyList(List<ScanResult> list){
        for (ScanResult each:list){
            addBeanProperty(each.getAClass(),each.getField(),each.getFieldClass());
        }
    }

    /**
     * Returns a String which capitalizes the first letter of the string.
     */
    public static String capitalize(String name) {
        if (name == null || name.length() == 0) {
            return name;
        }
        return name.substring(0, 1).toUpperCase(ENGLISH) + name.substring(1);
    }

    /**
     * 添加字段到映射
     * @param clazz
     * @param field
     */
    private void addBeanProperty(Class clazz,String field,Class fieldClass){
        addBeanProperty(clazz,clazz,field,fieldClass);
    }

    /**
     *  添加字段到映射
     * @param clazz
     * @param methodClass
     * @param field
     * @param fieldClass
     */
    private void addBeanProperty(Class clazz,Class methodClass,String field,Class fieldClass){
        log.info("db自动加解密拦截器注册字段：{}-->{}",clazz,field);
        if (!fieldClass.equals(String.class)&&!fieldClass.equals(List.class)&&!clazz.getName().endsWith("UpdateBuilder")){
            log.error("db 自动加解密字段仅支持String 或 List<String> 字段，class:{},field:{}",clazz,field);
            return;
        }

        Method readMethod = null;
        try {
            readMethod=methodClass.getDeclaredMethod("get"+capitalize(field),null);
        } catch (NoSuchMethodException e) {
            log.error("db 解密字段加载异常,class:{},field:{},e:{}", clazz, field,Throwables.getStackTraceAsString(e));
        }
        Method writeMethod=null;
        try {
            writeMethod=methodClass.getDeclaredMethod("set"+capitalize(field),fieldClass);
        } catch (NoSuchMethodException e) {
            //List<String> 特殊处理
            try {
                writeMethod=methodClass.getDeclaredMethod(field,fieldClass);
            } catch (NoSuchMethodException e1) {
                log.error("db 解密字段加载异常,class:{},field:{},e:{}", clazz, field,Throwables.getStackTraceAsString(e));
            }
        }

        if (readMethod==null||writeMethod==null){
            return;
        }

        if(cache.get(clazz)==null){
            cache.put(clazz,new HashMap<>());
        }
        Map<String,MethodBox> propertyDescriptorMap=cache.get(clazz);
        propertyDescriptorMap.put(field,new MethodBox(readMethod,writeMethod));
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        Object parameter=invocation.getArgs()[1];


        if(parameter instanceof Map) {
            //浅复制字段去重，避免同一个字段被加密多次
            Set<Object> disctinct = new HashSet<>();
            for (Object each : ((Map) parameter).values()) {
                boolean result = disctinct.add(each);
                if (result) {
                    if (each instanceof List){
                        List listParam=(List) each;
                        for (Object eachItem:listParam){
                            modifyField(eachItem, true);
                        }
                    }else {
                        modifyField(each, true);
                    }
                }
            }
        //特殊处理
        }else if (parameter.getClass().getName().endsWith("UpdateBuilder")){
            Class updateBuilderClazz=  Class.forName(parameter.getClass().getName());
            Map<String,MethodBox> cacheMethod=cache.get(updateBuilderClazz);
            if (cacheMethod!=null){
                //读取set 和 where
                Object setObj=cacheMethod.get("set").getReadMethod().invoke(parameter);
                Object whereObj=cacheMethod.get("where").getReadMethod().invoke(parameter);
                modifyField(setObj,true);
                modifyField(whereObj,true);
            }
        }else {
            if (parameter instanceof List){
                List listParam=(List) parameter;
                for (Object eachItem:listParam){
                    modifyField(eachItem, true);
                }
            }else {
                modifyField(parameter, true);
            }
        }

        Object object = invocation.proceed();

        if ("query".equals(methodName)) {
            //解密
            if(object instanceof List){
                for (Object each:(List)object){
                    modifyField(each,false);
                }
            }else {
                modifyField(object,false);
            }
        }

        return object;
    }





    /**
     * 结果解密
     * @param object
     */
    private void modifyField(Object object,boolean encript){

        Map<String,MethodBox> map= cache.get(object.getClass());
        if(map==null){
            return;
        }
        for (Map.Entry<String,MethodBox> each:map.entrySet()){
            MethodBox methodBox=each.getValue();
            try {
                Object value=methodBox.getReadMethod().invoke(object,null);
                if(value==null){
                   continue;
                }
                //String
                if (value instanceof String){
                   Object after = dealString(value,encript,object.getClass(),each.getKey());
                   methodBox.getWriteMethod().invoke(object,new Object[]{after});
                   continue;
                }
                //List
                if (value instanceof List) {
                  List list=  (List)value;
                  for (int i=0;i<list.size();i++ ){
                      Object after=  dealString(list.get(i),encript,object.getClass(),each.getKey());
                      list.set(i,after);
                  }
                  continue;
                }

                log.error("classs:{},field:{} 不能访问,exception:{}",object.getClass(),each.getKey(),"不是String或 List<String>");
            } catch (Exception e) {
                log.error("classs:{},field:{} 不能访问,exception:{}",object.getClass(),each.getKey(),e);
            }
        }
    }



    private Object dealString(Object value,boolean encript,Class clazz,String field){
        if(!(value instanceof String)){
            log.error("classs:{},field:{} 不能访问,exception:{}",clazz,field,"不是String或 List<String>");
            return value;
        }
        if(!StringUtils.hasText((String)value)){
            return value;
        }
        String after;
        if (encript){
            //加密
            after=encrypt((String)value);
        }else {
            //解密
            after=decrypt((String)value);
        }

        return after;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        String configLocation=properties.getProperty("configLocation");
        if (!StringUtils.hasText(configLocation)){
            throw new RuntimeException("db加解密拦截器 configLocation 配置丢失");
        }
        try {
           this.settings= XmlUtil.readXml(Settings.class,configLocation);
        } catch (FileNotFoundException e) {
           throw new RuntimeException("db加解密拦截器配置文件加载异常",e.fillInStackTrace());
        }

        init();
    }

    /**
     * 初始化
     */
    private void init(){
        List<Mapper> mappers=settings.getMappers();
        if (mappers==null||mappers.isEmpty()){
            return;
        }

        specialProcess();

        //读取配置
        for (Mapper eachMapper:settings.getMappers()){
            Class<?> clazz = null;
            try {
                clazz = Class.forName(eachMapper.getClassName());
            } catch (ClassNotFoundException e) {
                log.error(Throwables.getStackTraceAsString(e));
            }

            for (String eachField:eachMapper.getPropertys()){
                Field field;
                Class currClazz=clazz;
                do {
                    try {
                        field = currClazz.getDeclaredField(eachField);
                        //找到注解，加入配置
                        addBeanProperty(clazz,currClazz,field.getName(),field.getType());
                        break;
                    } catch (NoSuchFieldException e) {
                        currClazz=currClazz.getSuperclass();
                        if (currClazz==Object.class){
                            log.error(Throwables.getStackTraceAsString(e));
                        }
                    }
                }while (currClazz!=Object.class);


            }
        }
    }

    /**
     * 针对自动生成po的特殊处理
     */
    void specialProcess(){
        List<Mapper> specialMappers=new ArrayList<>();
        List<Mapper> mappers=settings.getMappers();
        if (mappers==null){
            return;
        }
        for (Mapper eachMapper:mappers){
            if (!eachMapper.isSpecial()){
                continue;
            }
            Mapper queryBuilder=new Mapper();
            queryBuilder.setClassName(eachMapper.getClassName().concat("$QueryBuilder"));
            Mapper conditionBuilder=new Mapper();
            conditionBuilder.setClassName(eachMapper.getClassName().concat("$ConditionBuilder"));

            List<String> pros=new ArrayList<>();
            for (String orgPro :eachMapper.getPropertys()) {
                pros.add(orgPro.concat("List"));
            }

            queryBuilder.setPropertys(new ArrayList<>());
            queryBuilder.getPropertys().addAll(eachMapper.getPropertys());
            queryBuilder.getPropertys().addAll(pros);
            conditionBuilder.setPropertys(pros);
            specialMappers.add(queryBuilder);
            specialMappers.add(conditionBuilder);
            //updateBuilder 缓存读写方法
            try {
                Class updateBuilder = Class.forName(eachMapper.getClassName().concat("$UpdateBuilder"));
                Class setClass=Class.forName(eachMapper.getClassName());
                Class conditionClass=Class.forName(eachMapper.getClassName().concat("$ConditionBuilder"));
                addBeanProperty(updateBuilder,"set",setClass );
                addBeanProperty(updateBuilder,"where",conditionClass);
            }catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }
        mappers.addAll(specialMappers);
    }



    /**远程调用接口实现解密
     * 若处理失败 ，则返回空串,防止错误数据传播
     * @param decryptData
     * @return
     */
    public String decrypt(String decryptData) {
        if(!StringUtils.hasText(decryptData)){
            return decryptData;
        }
        String deData = "";
        try {
            deData=  AESCoder.decrypt(decryptData,settings.getSetting().getAeskey());
            return deData;
        } catch (Exception e) {
            log.error("数据解密异常:{}",decryptData);
            throw new RuntimeException("数据解密异常");
        }
    }
    /**远程调用接口实现加密
     * 若处理失败 ，则把原请求数据返回，防止数据丢失
     * @param encryptData
     * @return
     */
    public String encrypt(String encryptData) {
        if(!StringUtils.hasText(encryptData)){
            return encryptData;
        }
        try {
            String enData=  AESCoder.encrpt(encryptData,settings.getSetting().getAeskey());
            return enData;
        } catch (Exception e) {
            log.error("数据加密异常:{}",encryptData);
            throw new RuntimeException("数据加密异常");
        }
    }



    @AllArgsConstructor
    @Data
    class MethodBox{


        private Method readMethod;

        private Method writeMethod;

    }


}