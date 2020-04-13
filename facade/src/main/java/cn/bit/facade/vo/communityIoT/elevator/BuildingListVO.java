package cn.bit.facade.vo.communityIoT.elevator;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
public class BuildingListVO implements Serializable {
    /**
     * 楼栋ID
     */
    private ObjectId buildingId;
    /**
     * 楼层
     */
    private Set<ObjectId> rooms;

    public Set<ObjectId> getRooms() {
        if(rooms == null || rooms.isEmpty()){
            rooms = new HashSet<>();
        }
        return rooms;
    }
}
