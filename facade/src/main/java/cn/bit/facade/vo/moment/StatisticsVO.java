package cn.bit.facade.vo.moment;

import lombok.Data;

import java.io.Serializable;

/**
 * 统计用户动态信息
 */
@Data
public class StatisticsVO implements Serializable {

    /**
     * 个人发布的动态数量
     */
    private Long momentAmount;

    /**
     * 个人评论的数量
     */
    private Long commentAmount;

    /**
     * 个人点赞的数量
     */
    private Long praiseAmount;
}
