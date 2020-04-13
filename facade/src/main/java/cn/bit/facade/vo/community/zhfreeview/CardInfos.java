package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;

@Data
public class CardInfos implements Serializable{
    /**
     * 卡号
     */
    private String CardSerialNumber;
    /**
     * 卡介质类型
     */
    private Byte CardMediaTypeID;
}
