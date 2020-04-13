package cn.bit.facade.model.system;

import org.springframework.data.annotation.Id;

import java.io.Serializable;

/**
 * 系统权限
 * Created by Terry on 2018/2/4 0004.
 */
public class Permission implements Serializable {

    @Id
    private String key;

    /**
     * 权限名称
     */
    private String name;

    /**
     * 权限描述
     */
    private String descr;
}
