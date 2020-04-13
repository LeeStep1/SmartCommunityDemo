package cn.bit.framework.vo.mq;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terry on 2016/7/13.
 */
public abstract class MessageVo implements Serializable {

    private Long id;
    private Date createTime = new Date();

    public MessageVo() {}

    public MessageVo(Long id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
