package cn.bit.facade.vo.fees;

import cn.bit.framework.data.common.Page;
import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * 房间业主信息、收费模板及账单实体
 */
@Data
public class RoomTemplateBillVO implements Serializable {

    /**
     * 房间ID
     */
    private ObjectId roomId;

    /**
     * 业主
     */
    private String proprietor;

    /**
     * 收费模板ID
     */
    private ObjectId templateId;

    /**
     * 收费模板名称
     */
    private String templateName;

    /**
     * 待缴费账单数量
     */
    private Integer unpaymentNum = 0;

    /**
     * 待缴费账单金额
     */
    private Long unpaymentMoney = 0L;

    /**
     * 账单列表
     */
    private Page<BillVO> bills;
}
