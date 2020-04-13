package cn.bit.facade.poi.handler;

import cn.afterturn.easypoi.handler.impl.ExcelDataHandlerDefaultImpl;
import cn.bit.facade.poi.entity.HouseholdEntity;
import lombok.extern.slf4j.Slf4j;

/**
 * 导入住户档案数据处理器
 *
 * @author decai.liu
 * @version 1.0.0
 * @create 2019.01.09
 */
@Slf4j
public class HouseholdEntityHandler extends ExcelDataHandlerDefaultImpl<HouseholdEntity> {

    @Override
    public Object importHandler(HouseholdEntity entity, String name, Object value) {
        log.info(entity.toString());
        return super.importHandler(entity, name, value);
    }
}
