package cn.bit.facade.vo.user;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.Set;

@Data
public class ClientUserRequest implements Serializable {

    private Integer client;

    private Integer partner;

    private Set<ObjectId> userId;

    @Field("communityIds")
    private ObjectId communityId;

    private Set<String> roles;

    private Integer loginStatus;

    private Integer dataStatus;
}
