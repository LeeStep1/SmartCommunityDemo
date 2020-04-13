package cn.bit.facade.service.property;

import cn.bit.facade.model.property.Gtaskzs;
import cn.bit.framework.data.common.Page;

/**
 * Created by fxiao
 * on 2018/3/26
 */
public interface GtaskzsFacade {

    /**
     * 新增
     * @param gtaskzs
     * @return
     */
    Gtaskzs addGtaskzs(Gtaskzs gtaskzs);

    /**
     * 分页
     * @param gtaskzs
     * @param page
     * @param size
     * @return
     */
    Page<Gtaskzs> queryPage(Gtaskzs gtaskzs, int page, int size);

    /**
     * 删除
     * @param gtaskzs
     * @return
     */
    Gtaskzs deleteGtaskzs(Gtaskzs gtaskzs);
}
