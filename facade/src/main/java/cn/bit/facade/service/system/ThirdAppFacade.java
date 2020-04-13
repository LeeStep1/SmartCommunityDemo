package cn.bit.facade.service.system;

import cn.bit.facade.model.system.ThirdApp;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

public interface ThirdAppFacade {

    /**
     * 新增
     * @param thirdApp
     * @return
     */
    ThirdApp addThirdApp(ThirdApp thirdApp) throws BizException;

    /**
     * 更新
     * @param thirdApp
     * @return
     */
    ThirdApp editThirdApp(ThirdApp thirdApp) throws BizException;

    /**
     * 根据ID 查询
     * @param id
     * @return
     */
    ThirdApp findById(ObjectId id) throws BizException;

    /**
     * 删除
     * @param id
     * @return
     */
    ThirdApp deleteThirdApp(ObjectId id) throws BizException;

    /**
     * 分页
     * @param thirdApp
     * @param page
     * @param size
     * @return
     * @throws BizException
     */
    Page<ThirdApp> page(ThirdApp thirdApp, int page, int size) throws BizException;

    /**
     * 获取redis的缓存信息
     * @param id
     * @return
     */
    String getRedisThirdApp(String id);

}
