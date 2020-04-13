package cn.bit.facade.service.system;

import cn.bit.facade.model.system.ThirdAppRecord;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.Date;

public interface ThirdAppRecordFacade {

    void addThirdAppRecord(ThirdAppRecord thirdAppRecord);

    Page<ThirdAppRecord> ThirdAppRecordPage(ThirdAppRecord entity, int page, int size);
}
