mybatis-encryptor
====
mybatis-encryptor mybatis 自动加解密插件

**加解密拦截器配置文件**
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE settings SYSTEM "com/github/lxn/mybatisEncryptor/db-security-cfg.dtd">
<settings>
    <setting>
        <aeskey>aesKey</aeskey>      
    </setting>
 
    <mapper className="com.jd.baoxian.order.trade.dao.po.Order" special="true">
        <property>buyerPhone</property>
    </mapper>
    <mapper className="com.jd.baoxian.order.trade.dao.po.OrderExtend" special="true">
        <property>buyerName</property>
        <property>buyerMobile</property>
    </mapper>
    <mapper className="com.jd.baoxian.order.trade.dao.po.OrderVehicle" special="true">
        <property>ownerName</property>
        <property>ownerCardNo</property>
        <property>ownerMobile</property>
    </mapper>
    
</settings>
```

**属性解释**

aeskey：目前仅支持aes加密，这里填key即可  
mapper 的className属性：需要加解密的Po名  
mapper 的 special 属性：兼容Dao层生成工具生成的 QueryBuilder、UpdateBuilder等内部对象，如果你同时使用下面介绍的Dao生成工具，请设置为 special="true"，否则不需要设置  
property：需要加解密的字段名

**mybatis配置拦截器**

configLocation：字段映射配置文件的路径（上方的配置文件路径）

````
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE configuration PUBLIC "-//mybatis.org//DTD Config 3.0//EN" "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
  
    <plugins>
        <plugin interceptor="com.github.lxn.mybatisEncryptor.core.DBInterceptor">
            <property name="configLocation" value="classpath:spring/db-security-cfg.xml"></property>
        </plugin>
    </plugins>
</configuration>
````