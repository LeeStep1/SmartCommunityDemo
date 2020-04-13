package cn.bit.facade.model.statistics;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * Created by fxiao
 * on 2018/3/26
 */
@Data
@Document(collection = "STAT_DATA")
public class Statistics implements Serializable{

    @Id
    private String id;
    /**
     * 统计类型（1：住户；2：故障；3：营收；4：门禁）
     */
    private Integer statisticsType;
    /**
     * 社区ID
     */
    private ObjectId communityId;
    /**
     * 年月
     */
    private String dateTime;
    /**
     * 键值对
     */
    private Map<String, Long> keyValue;
    /**
     * 创建时间
     */
    private Date createAt;
}
