package cn.bit.facade.vo.business;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;

/**
 * Created by fxiao
 * on 2018/4/2
 */
@Data
public class BaseInfo implements Serializable {
    /**
     * 页数
     */
    @Transient
    @JSONField(serialize = false)
    private Integer page;
    /**
     * 每页大小
     */
    @Transient
    @JSONField(serialize = false)
    private Integer size;

}
