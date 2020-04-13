package cn.bit.system.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.ResourceType;
import cn.bit.facade.enums.VisibilityType;
import cn.bit.facade.model.system.Resource;
import cn.bit.facade.service.system.ResourceFacade;
import cn.bit.system.dao.ResourceRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service("resourceFacade")
public class ResourceFacadeImpl implements ResourceFacade {

    @Autowired
    private ResourceRepository resourceRepository;

    @Override
    public Resource addResource(Resource resource) {
        resource.setCreateAt(new Date());
        resource.setUpdateAt(resource.getCreateAt());
        resource.setDataStatus(DataStatusType.VALID.KEY);
        return resourceRepository.insert(resource);
    }

    @Override
    public Resource updateResource(Resource resource) {
        resource.setUpdateAt(new Date());
        return resourceRepository.updateOne(resource);
    }

    @Override
    public Resource getResource(ObjectId id) {
        return resourceRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public Resource deleteResource(ObjectId id) {
        Resource resource = new Resource();
        resource.setUpdateAt(new Date());
        resource.setDataStatus(DataStatusType.INVALID.KEY);
        return resourceRepository.updateOne(resource);
    }

    @Override
    public Resource getResourceByUriSignAndType(String uriSign, ResourceType type) {
        return resourceRepository.findByUriSignAndTypeAndDataStatus(uriSign, type.value(), DataStatusType.VALID.KEY);
    }

    @Override
    public void upsertResourceByUriSignAndType(Resource resource, String uriSign, Integer type) {
        resourceRepository.upsertWithSetOnInsertCreateAtByUriSignAndTypeAndDataStatus(resource, uriSign, type, DataStatusType.VALID.KEY);
    }

    @Override
    public List<Resource> getVisibleResourcesByIdsAndClient(Collection<ObjectId> ids, Integer client) {
        return resourceRepository.findByIdInAndClientsAndVisibilityNotAndDataStatus(ids, client,
                VisibilityType.INVISIBLE.value(), DataStatusType.VALID.KEY);
    }

    @Override
    public List<Resource> getUngroupedResourcesByType(Integer type) {
        return resourceRepository.findByGroupIdExistsAndTypeAndDataStatus(false, type,
                DataStatusType.VALID.KEY);
    }

    @Override
    public List<Resource> getUngroupedResourcesByTypeAndClients(Integer type, Collection<Integer> clients) {
        return resourceRepository.findByGroupIdExistsAndTypeAndClientsInAndDataStatus(false, type, clients,
                DataStatusType.VALID.KEY);
    }
}
