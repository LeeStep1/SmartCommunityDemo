package cn.bit.facade.service.property;

import cn.bit.facade.model.property.Complain;
import cn.bit.facade.vo.property.ComplainRequest;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.List;

public interface ComplainFacade {

    /**
     * 新增用户投诉
     * @param entity
     * @return
     */
    Complain addComplain(Complain entity);

    /**
     * 根据id删除用户投诉
     * @param id
     * @return
     */
    void deleteComplainById(ObjectId id);

    /**
     * 获取用户投诉详细
     * @param id
     * @return
     */
    Complain getComplainById(ObjectId id);

    /**
     * 分页获取用户投诉
     * @param entity
     * @param page
     * @param size
     * @return
     */
    Page<Complain> getComplainPage(ComplainRequest entity, int page, int size);

    /**
     * 获取指定社区及指定size获取住户投诉报事列表
     * @param communityId
     * @param size
     * @return
     */
    List<Complain> listComplainsForScreen(ObjectId communityId, Integer size);

    /**
     * 处理投诉
     * @param complain
     * @return
     */
    Complain processComplain(Complain complain);

    /**
     * 评价投诉处理结果
     * @param complain
     * @return
     */
    Complain evaluateComplain(Complain complain);

    /**
     * 隐藏工单
     * @param id
     * @param uid
     * @return
     */
    Complain hiddenComplainById(ObjectId id, ObjectId uid);

}
