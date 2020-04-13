package cn.bit.facade.service.system;

import cn.bit.facade.model.system.App;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

public interface AppFacade {

    /**
     * 新增应用信息
     * @param app
     * @return
     * @throws BizException
     */
    App addApp(App app) throws BizException;

    /**
     * 修改应用信息
     * @param app
     * @return
     * @throws BizException
     */
    App updateApp(App app) throws BizException;

    /**
     * 根据id获取应用信息
     * @param id
     * @return
     * @throws BizException
     */
    App getAppById(ObjectId id) throws BizException;

    /**
     * 删除应用信息（逻辑删除）
     * @param id
     * @throws BizException
     */
    App deleteAppById(ObjectId id) throws BizException;

    /**
     * 分页获取应用信息
     * @param page
     * @param size
     * @return
     */
    Page<App> getApps(int page, int size);

}
