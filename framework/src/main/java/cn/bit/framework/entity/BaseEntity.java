package cn.bit.framework.entity;


import cn.bit.framework.utils.string.StrUtil;

import java.io.Serializable;
import java.util.Date;

/**
 * 实体基类，定义基本字段
 *
 * @author terry
 * @create 2016-07-28 15:57
 **/
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String guid = StrUtil.get32UUID();

    /**
     * 版本号，update时做乐观锁使用
     */
    private Integer version = 0;

    /**
     * 创建时间
     */
    protected Date createdAt = new Date();

    /**
     * 最后修改时间
     */
    protected Date updatedAt = new Date();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        this.guid = guid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
