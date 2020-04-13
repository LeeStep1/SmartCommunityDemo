package cn.bit.api.support;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Collection;

@Data
public class PushTarget implements Serializable {

    private Collection<ObjectId> userIds;

    private Collection<String> tags;

    /**
     * 指定这次推送的端
     */
    private Collection<Integer> clients;

}
