package cn.bit.facade.vo.user;

import cn.bit.facade.model.community.Community;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Transient;

@Data
public class UserCommunityVO extends Community {

    @Transient
    private ObjectId userId;

    /**
     * 记录社区下有多少套有效房屋
     */
    @Transient
    private Integer roomsAmount;

}
