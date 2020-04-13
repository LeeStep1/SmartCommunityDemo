package cn.bit.system.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.system.App;
import cn.bit.facade.service.system.AppFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.system.dao.AppRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service("appFacade")
@Slf4j
public class AppFacadeImpl implements AppFacade {

    @Autowired
    private AppRepository appRepository;

    @Override
    public App addApp(App app) throws BizException {
        app.setCreateAt(new Date());
        app.setUpdateAt(app.getCreateAt());
        app.setDataStatus(DataStatusType.VALID.KEY);
        return appRepository.insert(app);
    }

    @Override
    public App updateApp(App app) throws BizException {
        app.setUpdateAt(new Date());
        app.setDataStatus(DataStatusType.VALID.KEY);
        return appRepository.updateOne(app);
    }

    @Override
    public App getAppById(ObjectId id) throws BizException {
        return appRepository.findById(id);
    }

    @Override
    public App deleteAppById(ObjectId id) throws BizException {
        App app = new App();
        app.setDataStatus(DataStatusType.INVALID.KEY);
        app.setUpdateAt(new Date());
        return appRepository.updateById(app, id);
    }

    @Override
    public Page<App> getApps(int page, int size) {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<App> resultPage = appRepository.findByDataStatus(DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }
}
