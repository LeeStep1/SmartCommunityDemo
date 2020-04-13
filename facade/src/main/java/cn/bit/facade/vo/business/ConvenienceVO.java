package cn.bit.facade.vo.business;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * Created by fxiao
 * on 2018/4/4
 */
@Data
public class ConvenienceVO implements Serializable{
    /**
     * ID
     */
    private ObjectId id;
    /**
     * 服务名称
     */
    private String name;
    /**
     * 图标
     */
    private String icon;
    /**
     * 服务类型（1：生活服务；2：家政服务）
     */
    private Integer serviceType;
    /**
     * 服务方式
     * 1:热线服务,2:在线服务
     */
    private Integer serviceWay;
    /**
     * 热线电话
     */
    private String contact;
    /**
     * 外部链接
     */
    private String url;
}
