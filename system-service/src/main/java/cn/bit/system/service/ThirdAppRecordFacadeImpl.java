package cn.bit.system.service;

import cn.bit.facade.model.system.ThirdAppRecord;
import cn.bit.facade.service.system.ThirdAppRecordFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.system.dao.ThirdAppRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("thirdAppRecordFacade")
public class ThirdAppRecordFacadeImpl implements ThirdAppRecordFacade {

    @Autowired
    private ThirdAppRecordRepository thirdAppRecordRepository;

    @Override
    public void addThirdAppRecord(ThirdAppRecord thirdAppRecord) {
        thirdAppRecordRepository.insert(thirdAppRecord);
    }

    @Override
    public Page<ThirdAppRecord> ThirdAppRecordPage(ThirdAppRecord entity, int page, int size) {
        return thirdAppRecordRepository.findPage(entity, page, size, null);
    }
}
