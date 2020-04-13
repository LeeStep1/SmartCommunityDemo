package cn.bit.community.service;

import cn.bit.community.dao.DataLayoutRepository;
import cn.bit.facade.model.community.DataLayout;
import cn.bit.facade.service.community.DataLayoutFacade;
import cn.bit.facade.vo.community.DataLayoutQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cn.bit.facade.exception.community.CommunityBizException.LAYOUT_ID_NULL;

@Component("dataLayoutFacade")
@Slf4j
public class DataLayoutFacadeImpl implements DataLayoutFacade {

    @Autowired
    private DataLayoutRepository dataLayoutRepository;

    /**
     * 查询布局列表
     *
     * @param query
     * @return
     */
    @Override
    public List<DataLayout> listDataLayouts(DataLayoutQuery query) {
        return dataLayoutRepository.findByCommunityIdAndScreenRatioTypeAndDisplayableAllIgnoreNull(
                query.getCommunityId(), query.getScreenRatioType(), query.getDisplayable());
    }

    /**
     * 更新布局
     *
     * @param dataLayout
     * @return
     */
    @Override
    public DataLayout modifyDataLayout(DataLayout dataLayout) {
        if (dataLayout.getId() == null) {
            throw LAYOUT_ID_NULL;
        }
        DataLayout toUpdate = new DataLayout();
        toUpdate.setKey(dataLayout.getKey());
        toUpdate.setName(dataLayout.getName());
        toUpdate.setPoints(dataLayout.getPoints());
        toUpdate.setDisplayable(dataLayout.getDisplayable());
        toUpdate.setAttachValue(dataLayout.getAttachValue());
        toUpdate.setRefreshInterval(dataLayout.getRefreshInterval());
        toUpdate.setUpdateAt(new Date());
        return dataLayoutRepository.updateById(toUpdate, dataLayout.getId());
    }

    /**
     * 保存布局
     *
     * @param dataLayout
     * @return
     */
    @Override
    public DataLayout saveDataLayout(DataLayout dataLayout) {
        dataLayout.setUpdateAt(new Date());
        return dataLayoutRepository.insert(dataLayout);
    }

    /**
     * 批量保存布局
     *
     * @param dataLayouts
     */
    @Override
    public void saveDataLayouts(List<DataLayout> dataLayouts) {
        if (dataLayouts == null || dataLayouts.isEmpty()) {
            return;
        }
        Date updateAt = new Date();
        List<DataLayout> insertList = new ArrayList<>();
        dataLayouts.forEach(dataLayout -> {
            dataLayout.setUpdateAt(updateAt);
            if (dataLayout.getId() == null) {
                insertList.add(dataLayout);
            } else {
                DataLayout toUpdate = new DataLayout();
                toUpdate.setKey(dataLayout.getKey());
                toUpdate.setName(dataLayout.getName());
                toUpdate.setPoints(dataLayout.getPoints());
                toUpdate.setDisplayable(dataLayout.getDisplayable());
                toUpdate.setAttachValue(dataLayout.getAttachValue());
                toUpdate.setRefreshInterval(dataLayout.getRefreshInterval());
                toUpdate.setUpdateAt(dataLayout.getUpdateAt());
                dataLayoutRepository.updateById(toUpdate, dataLayout.getId());
            }
        });
        if (!insertList.isEmpty()) {
            dataLayoutRepository.save(dataLayouts);
        }
    }
}
