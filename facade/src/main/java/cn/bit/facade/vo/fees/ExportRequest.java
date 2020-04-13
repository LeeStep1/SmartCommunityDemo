package cn.bit.facade.vo.fees;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
public class ExportRequest implements Serializable {

    /**
     * 社区id
     */
    private ObjectId communityId;

    /**
     * 楼栋ID集合
     */
    private Set<ObjectId> buildingIds;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 开始时间
     */
    private Date startAt;

    /**
     * 结束时间
     */
    private Date endAt;
}
