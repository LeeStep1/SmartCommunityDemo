package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;

/**
 * 全视通社区整体结构
 */
@Data
public class CommunityParams implements Serializable {
    /**
     * 全视通社区编号
     */
    private String tenantCode;
    /**
     * 快速创建小区类型
     */
    private Integer pattern = 3;
    /**
     * 社区属性
     */
    private Structure structure;
}
