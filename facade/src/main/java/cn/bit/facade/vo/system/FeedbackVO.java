package cn.bit.facade.vo.system;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.List;

/**
 * 建议反馈展示对象
 *
 * @author jianming.fan
 * @date 2018-09-20
 */
@Data
public class FeedbackVO implements Serializable {

    /**
     * 反馈内容
     */
    private String content;

    /**
     * 应用版本
     */
    private String version;

    /**
     * 使用场景ID
     */
    private List<ObjectId> useCases;

    /**
     * 图片路径列表
     */
    private List<String> images;

}
