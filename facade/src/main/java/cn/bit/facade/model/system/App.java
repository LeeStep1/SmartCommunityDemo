package cn.bit.facade.model.system;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "SYS_APP")
public class App implements Serializable {
    @Id
    private ObjectId id;

    /**
     * 名称
     */
    @NotBlank(message = "应用名称不能为空")
    private String name;

    /**
     * 英文名称
     */
    private String nameEn;

    /**
     * 介绍
     */
    @NotBlank(message = "应用简介不能为空")
    private String introduction;

    /**
     * 英文介绍
     */
    private String introductionEn;

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

}
