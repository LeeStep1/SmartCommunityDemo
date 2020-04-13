package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class DeviceParams implements Serializable{

    private ObjectId buildingId;

    private ObjectId communityId;

    private Integer type;

    private String name;

    private String code;

}
