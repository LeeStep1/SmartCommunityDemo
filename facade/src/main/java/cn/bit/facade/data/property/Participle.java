package cn.bit.facade.data.property;

import cn.bit.framework.data.common.BaseEntity;
import lombok.Data;

@Data
public class Participle extends BaseEntity {
    /**
     * 分词
     */
    private String words;
}
