package cn.bit.facade.model.push;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "PUSH_TEMPLATE")
public class PushTemplate implements Serializable {

    @Id
    private ObjectId id;

    @NotBlank(message = "名称不能为空")
    private String name;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Date createAt;

    private ObjectId creatorId;

    private Date updateAt;

    private Integer dataStatus;

}
