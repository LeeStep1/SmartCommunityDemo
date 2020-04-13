package cn.bit.facade.model.system;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

/**
 * 反馈
 */
@Data
@Document(collection = "SYS_FEEDBACK")
@CompoundIndex(def = "{'appId': 1, 'createAt': -1}", background = true)
public class Feedback implements Serializable {
    /**
     * 反馈信息id
     */
    @Id
    private ObjectId id;

    /**
     * app标识
     */
    @NotNull(message = "appId不能为空")
    private ObjectId appId;

    /**
     *反馈内容
     */
    private String content;

    /**
     *反馈者名称
     */
    private String userName;

    /**
     *联系电话
     */
    private String phone;

    /**
     *创建人id
     */
    private ObjectId creatorId;

    /**
     *创建时间
     */
    private Date createAt;

    /**
     *修改时间
     */
    private Date updateAt;

    /**
     *数据状态
     */
    private Integer dataStatus;

}
