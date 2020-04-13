package cn.bit.facade.vo.vehicle;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class WritingCarRequest implements Serializable {
    private ObjectId communityId;

    private String brand;

    private String json;

    private String opt;
}
