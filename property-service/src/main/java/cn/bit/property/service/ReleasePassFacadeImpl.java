package cn.bit.property.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.UseStatusType;
import cn.bit.facade.exception.property.PropertyBizException;
import cn.bit.facade.model.property.ReleasePass;
import cn.bit.facade.service.property.ReleasePassFacade;
import cn.bit.facade.vo.property.ReleasePassRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.common.XSort;
import cn.bit.property.dao.ReleasePassRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service("releasePassFacade")
@Slf4j
public class ReleasePassFacadeImpl implements ReleasePassFacade {

    @Autowired
    private ReleasePassRepository releasePassRepository;

    /**
     * 添加放行条
     * @param releasePass
     * @return
     */
    @Override
    public ReleasePass addReleasePass(ReleasePass releasePass) {
        releasePass.setCreateAt(new Date());
        releasePass.setReleaseStatus(UseStatusType.UNUSED.key);
        releasePass.setDataStatus(DataStatusType.VALID.KEY);
        return releasePassRepository.insert(releasePass);
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public boolean deleteReleasePass(ObjectId id) {
        int i = releasePassRepository.remove(id);
        return i > 0;
    }

    /**
     * 删除放行条（更改数据状态）
     * @param id
     * @return
     */
    @Override
    public ReleasePass changeStatus(ObjectId id) {
        ReleasePass toUpdate = new ReleasePass();
        toUpdate.setDataStatus(DataStatusType.INVALID.KEY);
        toUpdate.setUpdateAt(new Date());
        return releasePassRepository.updateById(toUpdate, id);
    }

    /**
     * 修改放行条
     * @param releasePass
     * @return
     */
    @Override
    public ReleasePass updateReleasePass(ReleasePass releasePass) {
        releasePass.setUpdateAt(new Date());
        return releasePassRepository.updateOne(releasePass);
    }

    /**
     * 根据id获取放行条
     * @param id
     * @param communityId
     * @return
     */
    @Override
    public ReleasePass getReleasePassByIdAndCommunityId(ObjectId id, ObjectId communityId) {
        ReleasePass releasePass =
                releasePassRepository.findByIdAndCommunityIdAndDataStatus(id, communityId, DataStatusType.VALID.KEY);
        if(releasePass == null){
            throw PropertyBizException.RELEASE_BAR_NOT_EXISTS;
        }

        // 未使用的是否在有效时间外
        if (releasePass.getEndAt() != null
                && releasePass.getEndAt().before(new Date())
                && releasePass.getReleaseStatus() == UseStatusType.UNUSED.key) {
            releasePass.setReleaseStatus(UseStatusType.EXPIRED.key);
        }
        return releasePass ;
    }

    /**
     * 列表
     * @param communityId
     * @return
     */
    @Override
    public List<ReleasePass> getReleasePassList(ObjectId communityId) {
        List<ReleasePass> list = releasePassRepository
                .findAllByCommunityIdAndDataStatusOrderByUpdateAtDesc(communityId, DataStatusType.VALID.KEY);
        list.stream().filter(releasePass ->
                releasePass.getEndAt() != null
                        && releasePass.getEndAt().before(new Date())
                        && releasePass.getReleaseStatus() == UseStatusType.UNUSED.key)
                .forEach(releasePass -> releasePass.setReleaseStatus(UseStatusType.EXPIRED.key));
        return list;
    }

    /**
     * 分页
     * @param request
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<ReleasePass> getReleasePassPage(ReleasePassRequest request, int page, int size) {
        Page<ReleasePass> resultPage = releasePassRepository.getReleasePassPage(request, page, size, XSort.desc("createAt"));;
        resultPage.getRecords().stream().filter(releasePass ->
                releasePass.getEndAt() != null
                        && releasePass.getEndAt().before(new Date())
                        && releasePass.getReleaseStatus() == UseStatusType.UNUSED.key)
                .forEach(releasePass -> releasePass.setReleaseStatus(UseStatusType.EXPIRED.key));
        return resultPage;
    }

    /**
     * 放行条是否通过
     * @param releasePass
     * @return
     */
    @Override
    public ReleasePass checkReleasePass(ReleasePass releasePass) {
        ReleasePass entity = releasePassRepository.findById(releasePass.getId());
        //未使用的放行条是否在有效时间外
        if (entity.getEndAt() != null
                && entity.getEndAt().before(new Date())
                && releasePass.getReleaseStatus() == UseStatusType.UNUSED.key) {
            entity.setReleaseStatus(UseStatusType.EXPIRED.key);
            return entity;
        }
        releasePass.setId(null);
        releasePass.setReleaseStatus(UseStatusType.USED.key);
        releasePass.setUpdateAt(new Date());
        return releasePassRepository.updateById(releasePass, entity.getId());
    }
}
