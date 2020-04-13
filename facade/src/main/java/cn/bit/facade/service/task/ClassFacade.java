package cn.bit.facade.service.task;

import cn.bit.facade.model.task.Class;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

public interface ClassFacade {
    /**
     * 新增一个班次信息
     * @param entity
     * @throws BizException
     * @return
     */
    Class addTaskClass(Class entity) throws BizException;

    /**
     * 查询一个班次信息
     * @param id
     * @throws BizException
     * @return
     */
    Class findOne(ObjectId id) throws BizException;

    /**
     * 根据ID删除一个班次
     * @param id
     * @throws BizException
     * @return
     */
    boolean deleteTaskClass(ObjectId id) throws BizException;

    /**
     * 修改数据状态
     * @param id
     * @throws BizException
     * @return
     */
    boolean changeDataStatus(ObjectId id) throws BizException;

    /**
     * 分页查询
     * @param entity
     * @param page
     * @param size
     * @throws BizException
     * @return
     */
    Page<Class> queryPage(Class entity, int page, int size) throws BizException;

    /**
     *
     * @param entity
     * @return
     * @throws BizException
     */
    Class updateClass(Class entity) throws BizException;

    /**
     * 根据社区、岗位码获取
     * @param postCode
     * @return
     */
    List<Class> findByCommunityIdAndPostCode(ObjectId communityId, String postCode);

    /**
     * 查询班次列表
     * @param communityId
     * @param postCode
     * @param classType
     * @return
     */
    List<Class> queryListByCommunityIdAndPostCodeAndType(ObjectId communityId, String postCode, Integer classType);
}
