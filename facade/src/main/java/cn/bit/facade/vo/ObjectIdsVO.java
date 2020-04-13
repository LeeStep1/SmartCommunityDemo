package cn.bit.facade.vo;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Set;

@Data
public class ObjectIdsVO implements Serializable {

    private Set<ObjectId> ids;
}
