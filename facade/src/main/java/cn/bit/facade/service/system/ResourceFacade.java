package cn.bit.facade.service.system;

import cn.bit.facade.enums.ResourceType;
import cn.bit.facade.model.system.Resource;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;

public interface ResourceFacade {

    /**
     * 新增资源
     *
     * @param resource
     * @return
     */
    Resource addResource(Resource resource);

    /**
     * 更新资源
     *
     * @param resource
     * @return
     */
    Resource updateResource(Resource resource);

    /**
     * 获取指定id的资源
     *
     * @param id
     * @return
     */
    Resource getResource(ObjectId id);

    /**
     * 删除指定id的资源
     *
     * @param id
     */
    Resource deleteResource(ObjectId id);

    /**
     * 根据uri签名和资源类型获取资源
     *
     * @return
     */
    Resource getResourceByUriSignAndType(String uriSign, ResourceType type);

    /**
     * 更新或插入资源
     *
     * @param resource
     */
    void upsertResourceByUriSignAndType(Resource resource, String uriSign, Integer type);

    /**
     * 根据id集合获取资源列表
     *
     * @param ids
     * @return
     */
    List<Resource> getVisibleResourcesByIdsAndClient(Collection<ObjectId> ids, Integer client);

    /**
     * 根据type获取还未分组的资源
     *
     * @return
     */
    List<Resource> getUngroupedResourcesByType(Integer type);

    /**
     * 根据type和客户端类型获取还未分组的资源
     *
     * @return
     */
    List<Resource> getUngroupedResourcesByTypeAndClients(Integer type, Collection<Integer> clients);

}
