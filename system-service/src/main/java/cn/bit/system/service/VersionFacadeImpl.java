package cn.bit.system.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.system.Version;
import cn.bit.facade.service.system.VersionFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.system.dao.VersionRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service("versionFacade")
@Slf4j
public class VersionFacadeImpl implements VersionFacade {

    @Autowired
    private VersionRepository versionRepository;

    /**
     * 新增版本信息
     * @param version
     * @return
     */
    @Override
    public Version addVersion(Version version) throws BizException {
        if (version.getPublished() == null) {
            version.setPublished(false);
        }
        if(version.getHasError() == null) {
            version.setHasError(false);
        }
        version.setCreateAt(new Date());
        version.setUpdateAt(version.getCreateAt());
        version.setDataStatus(DataStatusType.VALID.KEY);
        return versionRepository.insert(version);
    }

    /**
     * 修改版本信息
     * @param version
     * @return
     */
    @Override
    public Version updateVersion(Version version) throws BizException {
        version.setUpdateAt(new Date());
        version.setDataStatus(DataStatusType.VALID.KEY);
        return versionRepository.updateOne(version);
    }

    /**
     * 根据appId获取所有版本信息
     * @param appId
     * @return
     */
    @Override
    public List<Version> getAllVersionsByAppId(ObjectId appId) throws BizException {
        return versionRepository.findByAppIdAndDataStatusOrderBySequenceDesc(appId, DataStatusType.VALID.KEY);
    }

    /**
     * 根据appId分页获取版本信息
     * @param appId
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Version> getVersionsByAppId(ObjectId appId, int page, int size) {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "sequence"));
        org.springframework.data.domain.Page<Version> resultPage = versionRepository.findByAppIdAndDataStatus(appId, DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    /**
     * 获取已发布的版本信息
     * @param appId
     * @return
     */
    @Override
    public List<Version> getAllPublishedVersionsByAppId(ObjectId appId) throws BizException {
        return versionRepository.findAllByAppIdAndPublishedOrderBySequenceDesc(appId, true);
    }

    /**
     * 分页获取已发布的版本信息
     * @param appId
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Version> getPublishedVersionsByAppId(ObjectId appId, int page, int size) throws BizException {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "sequence"));
        org.springframework.data.domain.Page<Version> resultPage = versionRepository.findByAppIdAndPublished(appId, true, pageable);
        return PageUtils.getPage(resultPage);
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Override
    public Version getVersionById(ObjectId id) throws BizException {
        return versionRepository.findOne(id);
    }

    /**
     * 根据id删除
     * @param id
     * @return
     */
    @Override
    public Version deleteVersionById(ObjectId id) throws BizException {
        Version version = new Version();
        version.setPublished(false);
        version.setUpdateAt(new Date());
        version.setDataStatus(DataStatusType.INVALID.KEY);
        return versionRepository.updateById(version, id);
    }

    /**
     * 根据id发布版本
     * @param id
     * @return
     * @throws BizException
     */
    @Override
    public Version publishVersionById(ObjectId id) throws BizException {
        Date now = new Date();
        Version version = new Version();
        version.setPublished(true);
        version.setPublishAt(now);
        version.setUpdateAt(now);
        return versionRepository.updateById(version, id);
    }

    /**
     * 根据当前版本获取最新版本
     * @param appId
     * @param currentSequence
     */
    @Override
    public Version getNewVersionByAppIdAndSequence(ObjectId appId, String currentSequence) throws BizException {
        // 先获取该app已发布且没有错误且序号大于当前序号的最新版本信息
        Version newVersion = versionRepository.findTop1ByAppIdAndPublishedAndHasErrorAndSequenceGreaterThanOrderByCreateAtDesc(
                appId, true, false, currentSequence
        );
        // 不存在则当前版本已为最新
        if (newVersion == null) {
            return null;
        }

        // 获取该app已发布且序号大于等于当前版本的最新错误版本信息
        Version errorVersion = versionRepository.findTop1ByAppIdAndPublishedAndHasErrorAndSequenceGreaterThanEqualOrderByCreateAtDesc(
                appId, true, true, currentSequence
        );
        // 存在则当前版本需要强制更新
        if (errorVersion != null) {
            newVersion.setForceUpgrade(true);
        }
        return newVersion;
    }

}
