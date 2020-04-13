package cn.bit.system.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.system.ThirdApp;
import cn.bit.facade.service.system.ThirdAppFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.redis.RedisTemplateUtil;
import cn.bit.system.dao.ThirdAppRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service("thirdAppFacade")
@Slf4j
public class ThirdAppFacadeImpl implements ThirdAppFacade{

    @Autowired
    private ThirdAppRepository thirdAppRepository;

    @Override
    public ThirdApp addThirdApp(ThirdApp thirdApp) throws BizException {
        // 随机生成secret
        String secret = UUID.randomUUID().toString().replace("-", "");
        thirdApp.setSecret(secret);
        thirdApp.setCreateAt(new Date());
        thirdApp.setDataStatus(DataStatusType.VALID.KEY);
        return thirdAppRepository.insert(thirdApp);
    }

    @Override
    public ThirdApp editThirdApp(ThirdApp thirdApp) throws BizException {
        return thirdAppRepository.updateById(thirdApp, thirdApp.getId());
    }

    @Override
    public ThirdApp findById(ObjectId id) throws BizException {
        return thirdAppRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public ThirdApp deleteThirdApp(ObjectId id) throws BizException {
        ThirdApp toUpdate = new ThirdApp();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        return thirdAppRepository.updateById(toUpdate, id);
    }

    @Override
    public Page<ThirdApp> page(ThirdApp thirdApp, int page, int size) throws BizException {
        return thirdAppRepository.findPage(thirdApp, page, size, XSort.desc("createAt"));
    }

    @Override
    public String getRedisThirdApp(String id) {
        return RedisTemplateUtil.getStr(id);
    }
}
