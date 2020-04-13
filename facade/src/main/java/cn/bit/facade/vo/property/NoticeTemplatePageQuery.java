package cn.bit.facade.vo.property;

import cn.bit.common.facade.query.PageQuery;
import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class NoticeTemplatePageQuery extends PageQuery implements Serializable {

    /**
     * 社区id
     */
    private ObjectId communityId;

    /**
     * 名称
     */
    private String name;

    /**
     * 标题
     */
    private String title;
}
