package cn.bit.facade.vo.business;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * Created by fxiao
 * on 2018/4/4
 */
@Data
public class SlideVO implements Serializable{
    /**
     * ID
     */
    private ObjectId id;
    /**
     * 标题
     */
    private String title;
    /**
     * 图片
     */
    private String photo;
    /**
     * 跳转类型（1：本地商店；2：外来连接）
     */
    private Integer gotoType;
    /**
     * 外来连接
     */
    private String href;
    /**
     * 店铺ID
     */
    private ObjectId shopId;
}
