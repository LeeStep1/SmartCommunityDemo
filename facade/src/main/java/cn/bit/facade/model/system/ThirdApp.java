package cn.bit.facade.model.system;

import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "SYS_THIRD_APP")
public class ThirdApp implements Serializable{

    /**
     * APPID
     */
    @Id
    @NotNull(message = "ID不能为空", groups = {update.class})
    private ObjectId id;

    /**
     * 第三方app名称
     */
    @NotNull(message = "第三方app名称不能为空", groups = {add.class, update.class})
    private String name;

    /**
     * 密钥
     */
    private String secret;

    /**
     * app类型，现在为3，表示第三方
     */
    private Integer type;

    /**
     * 创建人id
     */
    private ObjectId creatorId;

    /**
     * 创建时间
     */
    private Date createAt;

    /**
     * 修改时间
     */
    private Date updateAt;

    /**
     * 数据状态
     */
    private Integer dataStatus;

    public interface add{}

    public interface update{}

}
