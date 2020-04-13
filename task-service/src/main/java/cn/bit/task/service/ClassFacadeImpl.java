package cn.bit.task.service;

import cn.bit.facade.enums.ClassType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.exception.task.TaskBizException;
import cn.bit.facade.model.task.Class;
import cn.bit.facade.service.task.ClassFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.framework.exceptions.BizException;
import cn.bit.task.dao.ClassRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.xpath.operations.Bool;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component("classFacade")
@Slf4j
public class ClassFacadeImpl implements ClassFacade {

    @Autowired
    private ClassRepository classRepository;

    @Override
    public boolean changeDataStatus(ObjectId id) throws BizException {
        Class toUpdate = new Class();
        toUpdate.setUpdateAt(new Date());
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        toUpdate = classRepository.updateById(toUpdate, id);
        return toUpdate != null;
    }

    @Override
    public Class addTaskClass(Class entity) throws BizException {
        Boolean exist = classRepository.existsByCommunityIdAndNameAndPostCodeAndDataStatus(entity.getCommunityId(), entity.getName(), entity.getPostCode(), DataStatusType.VALID.KEY);
        if (exist){
            throw TaskBizException.CLASS_ALREADY_EXIST;
        }

        if(entity.getType() == ClassType.PEACETIME.key){
            exist = classRepository.existsByCommunityIdAndTypeAndPostCodeAndDataStatus(entity.getCommunityId(), entity.getType(), entity.getPostCode(), DataStatusType.VALID.KEY);
            if (exist){
                throw TaskBizException.CLASS_PEACETIME_ALREADY_EXIST;
            }
        }

        Date now = new Date();
        entity.setCreateAt(now);
        entity.setDataStatus(DataStatusType.VALID.KEY);
        return classRepository.insert(entity);
    }

    @Override
    public Class findOne(ObjectId id) throws BizException {
        Class entity = classRepository.findById(id);
        return entity;
    }

    @Override
    public boolean deleteTaskClass(ObjectId id) throws BizException {
        int i = classRepository.remove(id);
        if (i > 0) {
            return true;
        }
        return false;
    }

    @Override
    public Page<Class> queryPage(Class entity, int page, int size) throws BizException {
        entity.setDataStatus(DataStatusType.VALID.KEY);
        return classRepository.findPage(entity, page, size, XSort.desc("createAt"));
    }

    @Override
    public Class updateClass(Class entity) throws BizException {
        entity.setName(null);
        entity.setType(null);
        entity.setPostCode(null);
        entity.setCommunityId(null);
        entity.setDataStatus(null);

        entity.setUpdateAt(new Date());

        //return classRepository.updateOne(entity,"attendPlace","attendTime","task","offPlace","offTime","remark","restWeeks");
        return classRepository.updateWithUnsetIfNullAttendPlaceAndAttendTimeAndTaskAndOffPlaceAndOffTimeAndRestWeeksByIdAndDataStatus(entity, entity.getId(), DataStatusType.VALID.KEY);
    }

    @Override
    public List<Class> findByCommunityIdAndPostCode(ObjectId communityId, String postCode){
        return classRepository.findByCommunityIdAndPostCodeAndDataStatus(communityId, postCode, DataStatusType.VALID.KEY);
    }

    @Override
    public List<Class> queryListByCommunityIdAndPostCodeAndType(ObjectId communityId, String postCode, Integer classType) {
        return classRepository.queryListByCommunityIdAndPostCodeAndTypeAndDataStatus(communityId, postCode, classType, DataStatusType.VALID.KEY);
    }
}
