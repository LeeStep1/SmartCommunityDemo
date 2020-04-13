package cn.bit.facade.vo.fees;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;

@Data
public class PublishBillRequest implements Serializable {
    /**
     * 楼栋ID
     */
    private ObjectId buildingId;

    /**
     * 需要发布账单的月份 yyyyMMdd
     */
    private Date publishDate;
}
