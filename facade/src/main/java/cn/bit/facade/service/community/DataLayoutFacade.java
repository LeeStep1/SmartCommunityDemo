package cn.bit.facade.service.community;

import cn.bit.facade.model.community.DataLayout;
import cn.bit.facade.vo.community.DataLayoutQuery;

import java.util.List;

public interface DataLayoutFacade {

    /**
     * 查询布局列表
     * @param query
     * @return
     */
    List<DataLayout> listDataLayouts(DataLayoutQuery query);

    /**
     * 更新布局
     * @param dataLayout
     * @return
     */
    DataLayout modifyDataLayout(DataLayout dataLayout);

    /**
     * 保存布局
     * @param dataLayout
     * @return
     */
    DataLayout saveDataLayout(DataLayout dataLayout);

    /**
     * 批量保存布局
     * @param dataLayouts
     */
    void saveDataLayouts(List<DataLayout> dataLayouts);
}
