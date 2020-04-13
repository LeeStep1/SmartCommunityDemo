package cn.bit.facade.vo.vehicle;

import lombok.Data;

import java.io.Serializable;

@Data
public class IdentityQuery implements Serializable {
    /**
     * 车牌类型  1=民用  2=军队    3=警用   4=武警
     */
    private Integer type;
    /**
     * 收费类型  1=月卡  2=临时车  3=免费车 4=储值卡
     */
    private Integer chargeType;
    /**
     * 状态  0 : 过期; 1 : 正常
     */
    private Integer status;
    /**
     * 姓名 / 车牌
     */
    private String name;
}
