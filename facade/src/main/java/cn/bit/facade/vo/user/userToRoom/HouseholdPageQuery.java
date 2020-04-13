package cn.bit.facade.vo.user.userToRoom;

import cn.bit.common.facade.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;

/**
 * 住户档案分页查询实体类
 *
 * @author decai.liu
 * @date 2018-11-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class HouseholdPageQuery extends PageQuery {
    /**
     * 住户姓名
     */
    private String userName;

    /**
     * 住户手机号
     */
    private String phone;

    /**
     * 社区ID
     */
    private ObjectId communityId;

    /**
     * 楼栋ID
     */
    private ObjectId buildingId;

    /**
     * 房间ID
     */
    private ObjectId roomId;

    /**
     * 房屋名字
     */
    private String roomName;

    /**
     * {@link cn.bit.facade.enums.RelationshipType}
     */
    private Integer relationship;

    /**
     * 激活状态
     */
    private Boolean activated;
}
