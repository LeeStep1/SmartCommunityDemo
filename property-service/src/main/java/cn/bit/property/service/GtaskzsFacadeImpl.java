package cn.bit.property.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.property.Gtaskzs;
import cn.bit.facade.service.property.GtaskzsFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.property.dao.GtaskzsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by fxiao
 * on 2018/3/26
 */
@Service("gtaskzsFacade")
@Slf4j
public class GtaskzsFacadeImpl implements GtaskzsFacade {

    @Autowired
    private GtaskzsRepository gtaskzsRepository;

    @Override
    public Gtaskzs addGtaskzs(Gtaskzs gtaskzs) {
        gtaskzs.setCreateAt(new Date());
        gtaskzs.setDataStatus(DataStatusType.VALID.KEY);
        return gtaskzsRepository.insert(gtaskzs);
    }

    @Override
    public Page<Gtaskzs> queryPage(Gtaskzs gtaskzs, int page, int size) {
        gtaskzs.setDataStatus(DataStatusType.VALID.KEY);
        return gtaskzsRepository.findPage(gtaskzs, page, size, XSort.desc("createAt"));
    }

    @Override
    public Gtaskzs deleteGtaskzs(Gtaskzs gtaskzs) {
        Gtaskzs toUpdate = new Gtaskzs();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        return gtaskzsRepository.updateByOtherIdAndDataStatus(toUpdate, gtaskzs.getOtherId(), DataStatusType.VALID.KEY);
    }
}
