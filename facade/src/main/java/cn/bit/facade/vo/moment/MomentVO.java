package cn.bit.facade.vo.moment;

import lombok.Data;
import org.bson.types.ObjectId;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 动态信息请求参数
 */
@Data
public class MomentVO implements Serializable {

    /**
     * 动态类型（1：邻里社交，2：悬赏求助，3：二手交易）
     */
    @NotNull(message = "动态类型不能为空")
    private Integer type;

    /**
     * 内容
     */
    @Length(max = 500, message = "动态内容最大长度为500字符")
    private String content;

    /**
     * 照片
     */
    @Size(max = 9, message = "最多只能发布9张照片")
    private List<String> photos;

    /**
     * 社区ID
     */
    private ObjectId communityId;
}
