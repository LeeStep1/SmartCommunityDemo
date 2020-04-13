package cn.bit.facade.model.system;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

@Data
@Document(collection = "SYS_APP_VERSION")
@CompoundIndex(def = "{'appId': 1, 'sequence': -1}", background = true)
public class Version implements Serializable {
    @Id
    private ObjectId id;
    /**
     * app标识
     */
    @NotNull(message = "appId不能为空")
    private ObjectId appId;

    /**
     * 序号
     */
    @NotBlank(message = "版本号不能为空")
    @Pattern(regexp = "^\\d{1}\\.{1}\\d{1}\\.{1}\\d{1}$")
    private String sequence;

    /**
     * 发布状态
     */
    private Boolean published;

    /**
     * 强制升级
     */
    @NotNull(message = "是否强制升级不能为空")
    private Boolean forceUpgrade;
    /**
     * 版本错误
     */
    private Boolean hasError;

    /**
     * 下载路径
     */
    @NotBlank(message = "安装包路径不能为空")
    private String url;

    /**
     * app安装包名
     */
    private String appName;

    /**
     * 安装包大小
     */
    private Double appSize;

    /**
     * 描述
     */
    private String details;

    /**
     * 发布时间
     */
    private Date publishAt;

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
