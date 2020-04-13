package cn.bit.facade.vo.user;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Set;

@Data
public class CommunityUserRequest implements Serializable {

    private Set<ObjectId> userId;

    private ObjectId communityId;

    private Set<String> roles;

    private Set<Integer> clients;

    private Integer dataStatus;

}
