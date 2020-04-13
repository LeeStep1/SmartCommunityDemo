package cn.bit.facade.vo.fees;

import cn.bit.common.facade.query.PageQuery;
import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;

@Data
public class FeesPageQuery extends PageQuery implements Serializable {

    /**
     * 社区id
     */
    private ObjectId communityId;

    /**
     * 房间ID
     */
    private ObjectId roomId;

    /**
     * 业主ID
     */
    private ObjectId proprietorId;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 名字
     */
    private String name;

    /**
     * 开始时间
     */
    private Date startAt;

    /**
     * 结束时间
     */
    private Date endAt;
}
