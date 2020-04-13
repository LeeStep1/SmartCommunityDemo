package cn.bit.facade.service.community;

import cn.bit.facade.model.community.Parameter;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Set;

public interface ParameterFacade {

    /**
     * 新增配置参数
     * @param parameter
     * @return
     */
    Parameter addParameter(Parameter parameter);

    /**
     * 修改
     * @param parameter
     * @return
     */
    Parameter updateParameter(Parameter parameter);

    /**
     * 删除
     * @param id
     * @param uid
     * @return
     */
    boolean deleteParameter(ObjectId id, ObjectId uid);

    /**
     * 分页查询
     * @param parameter
     * @param page
     * @param size
     * @return
     */
    Page<Parameter> queryPage(Parameter parameter, Integer page, Integer size);

    /**
     * 根据type,key,value查询配置信息
     *
     * @param type
     * @param key
     * @param value
     * @param page
     * @param size
     * @return
     */
    Page<Parameter> findByTypeAndKeyAndValue(Integer type, String key, String value, int page, int size);

    /**
     * 根据社区查询key对应的value
     *
     * @param type
     * @param value
     * @param communityId
     * @return
     */
    Parameter findByTypeAndKeyAndCommunityId(Integer type, String value, ObjectId communityId);

    /**
     * 根据类型获取配置参数列表
     * @param key
     * @param communityId
     * @return
     */
    List<Parameter> findByTypeAndCommunityId(Integer key, ObjectId communityId);

    /**
     * 根据类型及key查询配置参数
     * @param type
     * @param key
     * @return
     */
    Parameter findOneByTypeAndKey(Integer type, String key);

    /**
     * 根据社区及配置类型分页获取配置参数列表
     * @param communityId
     * @param type
     * @param page
     * @param size
     * @return
     */
    Page<Parameter> queryPageByCommunityIdAndType(ObjectId communityId, Integer type, Integer page, Integer size);

    /**
     * 根据社区及type获取用户认证的配置项
     * @param communityId
     * @param type
     * @return
     */
    List<Parameter> queryByCommunityIdAndTypeForAuth(ObjectId communityId, Integer type);

    /**
     * 根据ID更新value跟updateAt
     * @param toUpdate
     * @param id
     * @return
     */
    Parameter updateWithSetValueAndUpdateAtById(Parameter toUpdate, ObjectId id);

    List<Parameter> findByCommunityIdInAndTypeAndKeyIn(Set<ObjectId> communityIds, Integer key, List<String> strings);

    /**
     * 更新对应key的value
     * @param toUpdate
     * @param communityId
     * @param key
     * @param name
     * @return
     */
    Parameter updateWithSetValueAndUpdateAtByCommunityIdAndTypeAndKey(Parameter toUpdate, ObjectId communityId, Integer key, String name);
}
