package cn.bit.facade.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 备注实体
 */
@Data
public class RemarkVO implements Serializable {

    /**
     * 备注
     */
    private String remark;

    /**
     * 图片
     */
    private List<String> images;
}
