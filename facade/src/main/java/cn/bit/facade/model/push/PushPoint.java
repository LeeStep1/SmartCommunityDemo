package cn.bit.facade.model.push;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
@Document(collection = "PUSH_POINT")
@CompoundIndex(def = "{'communityId': 1, 'signature': 1}", background = true)
public class PushPoint implements Serializable {

    @Id
    private ObjectId id;

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "签名不能为空")
    private String signature;

    @NotNull(message = "模板Id不能为空")
    private ObjectId templateId;

    @NotNull(message = "启用状态不能为空")
    private Boolean enable;

    /**
     * 是否前置推送
     */
    private Boolean beforeInvocation;

    /**
     * 范围标签
     */
    private Set<Object> scopes;

    private ObjectId creatorId;

    private Date createAt;

    private Date updateAt;

    private Integer dataStatus;

}
