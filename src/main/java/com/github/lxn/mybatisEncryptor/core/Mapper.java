package com.github.lxn.mybatisEncryptor.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Created by lxn on 2018/10/20.
 */
@Data
@XStreamAlias("mapper")
public class Mapper {

    @XStreamAsAttribute
    private String className;

    @XStreamAsAttribute
    private boolean special;

    @XStreamImplicit(itemFieldName="property")
    private List<String> propertys;

}
