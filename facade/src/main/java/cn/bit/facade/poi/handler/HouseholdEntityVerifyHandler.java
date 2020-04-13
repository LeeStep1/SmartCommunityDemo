package cn.bit.facade.poi.handler;

import cn.afterturn.easypoi.excel.entity.result.ExcelVerifyHandlerResult;
import cn.afterturn.easypoi.handler.inter.IExcelVerifyHandler;
import cn.bit.facade.poi.entity.HouseholdEntity;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;

/**
 * 导入住房档案数据校验，如果使用自定义校验规则，会跳过hibernate的验证
 *
 * @author decai.liu
 * @version 1.0.0
 * @create 2019.01.09
 */
@Slf4j
public class HouseholdEntityVerifyHandler implements IExcelVerifyHandler<HouseholdEntity> {

    /**
     * 导入自定义校验方法
     *
     * @param entity 当前对象
     * @return
     */
    @Override
    public ExcelVerifyHandlerResult verifyHandler(HouseholdEntity entity) {
        ExcelVerifyHandlerResult result = new ExcelVerifyHandlerResult();
        if (StringUtil.isNotBlank(entity.getRoomId())) {
            ObjectId roomId = new ObjectId(entity.getRoomId());
            // TODO 校验房间是否已经存在业主档案
            result.setMsg("房间已经存在业主档案");
            result.setSuccess(false);
        }
        return result;
    }
}
