package cn.bit.facade.vo.community;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class CommunityKv implements Serializable{

    private ObjectId id;

    private String name;

}
