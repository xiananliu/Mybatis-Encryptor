package com.github.lxn.mybatisEncryptor.util;

import com.thoughtworks.xstream.XStream;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * Created by lxn on 2018/10/19.
 */
public class XmlUtil {

    public static <T> T readXml(Class<T> clazz, String path) throws FileNotFoundException {

        File file= ResourceUtils.getFile(path);
        XStream xStream=new XStream();
        xStream.denyTypesByWildcard(new String[]{"**"});//将所有类反序列化禁用
        xStream.allowTypesByWildcard(new String[]{"com.github.lxn.mybatisEncryptor.**"});//根据情况设置类白名单，这里将所有com.jd.*类设置为白名单
        xStream.denyTypes(new String[]{"org.apache.commons.collections4.functors.InstantiateTransformer","org.apache.commons.collections4.functors.ConstantTransformer","org.apache.commons.collections4.functors.ChainedTransformer","org.apache.commons.collections4.functors.InvokerTransformer","org.apache.commons.collections4.comparators.TransformingComparator","org.apache.commons.configuration.ConfigurationMap","org.apache.commons.logging.impl.NoOpLog","org.apache.commons.configuration.Configuration","org.apache.commons.configuration.JNDIConfiguration","java.util.ServiceLoader$LazyIterator","com.sun.jndi.rmi.registry.BindingEnumeration","org.apache.commons.beanutils.BeanComparator","jdk.nashorn.internal.objects.NativeString","com.sun.xml.internal.bind.v2.runtime.unmarshaller.Base64Data","sun.misc.Service$LazyIterator","com.sun.jndi.rmi.registry.ReferenceWrapper","com.sun.jndi.toolkit.dir.LazySearchEnumerationImpl","org.springframework.aop.aspectj.autoproxy.AspectJAwareAdvisorAutoProxyCreator$PartiallyComparableAdvisorHolder","org.springframework.beans.factory.BeanFactory","org.springframework.jndi.support.SimpleJndiBeanFactory",
                "org.springframework.beans.factory.support.RootBeanDefinition","org.springframework.beans.factory.support.DefaultListableBeanFactory","org.springframework.aop.support.DefaultBeanFactoryPointcutAdvisor","org.springframework.aop.aspectj.annotation.BeanFactoryAspectInstanceFactory","org.springframework.aop.aspectj.AspectJPointcutAdvisor","org.springframework.aop.aspectj.AspectJAroundAdvice","org.springframework.aop.aspectj.AspectInstanceFactory","org.springframework.aop.aspectj.AbstractAspectJAdvice","javax.script.ScriptEngineFactory","com.sun.rowset.JdbcRowSetImpl","com.rometools.rome.feed.impl.ToStringBean","com.rometools.rome.feed.impl.EqualsBean","java.beans.EventHandler","javax.imageio.ImageIO$ContainsFilter","java.util.Collections$EmptyIterator",
                "javax.imageio.spi.FilterIterator","java.lang.ProcessBuilder","java.lang.Runtime","org.codehaus.groovy.runtime.MethodClosure","groovy.util.Expando","com.sun.xml.internal.ws.encoding.xml.XMLMessage$XmlDataSource","org.apache.commons.collections.map.LazyMap","org.apache.commons.collections.functors.ChainedTransformer","org.apache.commons.collections.functors.InvokerTransformer","org.apache.commons.collections.functors.ConstantTransformer","org.apache.commons.collections.keyvalue.TiedMapEntry"});

        xStream.processAnnotations(clazz);
        return (T) xStream.fromXML(file);
    }
}
