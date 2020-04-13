package cn.bit.facade.vo.property;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;

@Data
public class Property implements Serializable {

    private ObjectId id;

    /**
     * 公司名称
     */
    private String name;

    /**
     * 公司头像
     */
    private String logoImg;

    /**
     * 公司简介
     */
    private String intro;

    /**
     * 公司网址
     */
    private String website;

    /**
     * 联系人
     */
    private String contact;

    /**
     * 公司电话
     */
    private String telphone;

    /**
     * 公司地址
     */
    private String address;

    /**
     * 公司规模
     */
    private Integer size;

    /**
     * 国家
     */
    private String country;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区/县
     */
    private String district;

    /**
     * 电梯数量
     */
    private Integer elevatorCount;

    /**
     * 社区数量
     */
    private Integer communityCount;

    /**
     * 政府评价
     */
    private String govComment;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建人ID
     */
    private ObjectId creatorId;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 修改人ID
     */
    private ObjectId modifierId;

    /**
     * 修改时间
     */
    private Date updateAt;

    /**
     * 数据状态（1：有效；0：无效）
     */
    private Integer dataStatus;

    /**
     * 合作伙伴
     */
    private Integer partner;
}