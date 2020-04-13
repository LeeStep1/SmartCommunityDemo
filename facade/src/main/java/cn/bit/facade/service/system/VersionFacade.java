package cn.bit.facade.service.system;

import cn.bit.facade.model.system.Version;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

public interface VersionFacade {

    /**
     * 新增版本
     * @param version
     * @return
     */
    Version addVersion(Version version) throws BizException;

    /**
     * 更新版本
     * @param version
     * @return
     */
    Version updateVersion(Version version) throws BizException;

    /**
     * 根据appId获取所有版本信息
     * @param appId
     * @return
     */
    List<Version> getAllVersionsByAppId(ObjectId appId) throws BizException;

    /**
     * 根据appId分页获取版本信息
     * @param appId
     * @param page
     * @param size
     * @return
     */
    Page<Version> getVersionsByAppId(ObjectId appId, int page, int size) throws BizException;

    /**
     * 根据appId获取已发布的版本信息
     * @param appId
     * @return
     */
    List<Version> getAllPublishedVersionsByAppId(ObjectId appId) throws BizException;

    /**
     * 根据appId分页获取已发布的版本信息
     * @param appId
     * @param page
     * @param size
     * @return
     */
    Page<Version> getPublishedVersionsByAppId(ObjectId appId, int page, int size) throws BizException;

    /**
     * 根据ID获取版本信息
     * @param id
     * @return
     */
    Version getVersionById(ObjectId id) throws BizException;

    /**
     * 根据id删除版本
     * @param id
     */
    Version deleteVersionById(ObjectId id) throws BizException;

    /**
     * 根据id发布版本
     * @param id
     * @return
     * @throws BizException
     */
    Version publishVersionById(ObjectId id) throws BizException;

    /**
     * 根据当前版本获取最新版本
     * @param appId
     * @param currentVersion
     */
    Version getNewVersionByAppIdAndSequence(ObjectId appId, String currentVersion) throws BizException;

}
