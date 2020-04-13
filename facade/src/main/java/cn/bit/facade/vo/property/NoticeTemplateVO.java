package cn.bit.facade.vo.property;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class NoticeTemplateVO implements Serializable {

    /**
     * 模板id
     */
    private ObjectId id;

    /**
     * 标题
     */
    private String name;
}
