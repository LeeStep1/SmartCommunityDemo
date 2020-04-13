package cn.bit.facade.vo.push;

import cn.bit.common.facade.query.PageQuery;
import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class PushConfigPageQuery extends PageQuery implements Serializable {

    /**
     * 物业公司id
     */
    private ObjectId companyId;

    /**
     * 名称
     */
    private String name;
}
