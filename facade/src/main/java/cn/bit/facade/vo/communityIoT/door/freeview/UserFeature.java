package cn.bit.facade.vo.communityIoT.door.freeview;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Data
public class UserFeature implements Serializable {
    /**
     * 社区编码
     */
    private String tenantCode;

    /**
     * 全视通平台用户名
     */
    private String devUserName;

    /**
     * 人脸识别信息
     */
    private HumanFeature humanFeature;

    /**
     * 人脸图片base64编码
     */
    private Set<String> humanFeatureFiles;

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private ObjectId userId;

    /**
     * 人体特征类型
     */
    private Integer featureType;

    /**
     * 人体特征码
     */
    private String featureCode;

    @Getter
    @Setter
    public static class HumanFeature {
        private Integer featureType;

        private Date validStartTime;

        private Date validEndTime;

        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public Date getValidStartTime() {
            return validStartTime;
        }

        @JSONField(format = "yyyy-MM-dd HH:mm:ss")
        public Date getValidEndTime() {
            return validEndTime;
        }
    }
}
