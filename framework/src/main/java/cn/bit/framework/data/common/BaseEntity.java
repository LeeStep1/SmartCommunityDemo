package cn.bit.framework.data.common;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by terry on 2018/1/22.
 */
@Data
public abstract class BaseEntity implements Serializable {

    @JSONField(serialize = false)
    private String id;

    private Date createAt = new Date();
}
