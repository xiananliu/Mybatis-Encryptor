package com.github.lxn.mybatisEncryptor.core;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Data;

import java.util.List;

/**
 * Created by lxn on 2018/10/20.
 */
@Data
@XStreamAlias("settings")
public class Settings {


    private  Setting setting;


    @XStreamImplicit(itemFieldName="mapper")
    private List<Mapper> mappers;
}
