package cn.bit.communityIoT.service;

import cn.bit.communityIoT.dao.CameraRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.communityIoT.Camera;
import cn.bit.facade.service.communityIoT.CameraFacade;
import cn.bit.facade.vo.communityIoT.camera.CameraRequest;
import cn.bit.facade.vo.communityIoT.camera.EzvizTokenVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.redis.RedisTemplateUtil;
import cn.bit.framework.redis.lock.RedisLock;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Set;

import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.*;
import static cn.bit.facade.exception.user.UserBizException.CAMERA_BRAND_INVALID;
import static cn.bit.facade.exception.user.UserBizException.NOT_CAMERA_BRAND;

@Service("cameraFacade")
@Slf4j
public class CameraFacadeImpl implements CameraFacade {

    @Autowired
    private CameraRepository cameraRepository;

    /**
     * 海康摄像头key
     */
    public static final String EZVIZ_KEY = "ezviz";

    public static final String EZVIZ_KEY_EXPIRE = "ezviz_expire";

    public static final String EZVIZ_LOCK = "ezviz_lock";

    @Override
    public Camera getCameraById(ObjectId id) {
        return cameraRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public Camera addCamera(Camera entity) {
        // 摄像头厂商类型
        if (entity.getBrandNo() == null) {
            throw DEVICE_BRAND_NULL;
        }
        entity.setCreateAt(new Date());
        entity.setUpdateAt(entity.getCreateAt());
        entity.setDataStatus(DataStatusType.VALID.KEY);
        return cameraRepository.insert(entity);
    }

    @Override
    public Camera updateCamera(Camera camera) {
        if (camera == null || camera.getId() == null) {
            throw CAMERA_ID_NULL;
        }
        Camera item = cameraRepository.findById(camera.getId());
        if(item == null || item.getDataStatus() == DataStatusType.INVALID.KEY){
            throw CAMERA_NOT_EXISTS;
        }
        camera.setId(null);
        camera.setCommunityId(null);
        camera.setBrandNo(null);
        camera.setUpdateAt(new Date());
        return cameraRepository.updateByIdAndDataStatus(camera, item.getId(), DataStatusType.VALID.KEY);
    }

    @Override
    public Camera deleteCamera(ObjectId id) {
        Camera camera = new Camera();
        camera.setDataStatus(DataStatusType.INVALID.KEY);
        camera.setUpdateAt(new Date());
        return cameraRepository.updateByIdAndDataStatus(camera, id, DataStatusType.VALID.KEY);
    }

    @Override
    public List<Camera> getCameras(CameraRequest cameraRequest) {
        Set<ObjectId> buildingIdSet = cameraRequest.getBuildingId();
        if(buildingIdSet == null || buildingIdSet.isEmpty()){
            buildingIdSet = null;
        }
        return cameraRepository.findByCommunityIdAndBuildingIdInAndCameraCodeRegexAndBrandNoAndNameRegexAndCameraStatusAndUpdateAtGreaterThanAndDataStatusAllIgnoreNullOrderByCreateAtAsc(
                cameraRequest.getCommunityId(), buildingIdSet, StringUtil.makeQueryStringAllRegExp(cameraRequest.getCameraCode()),
                cameraRequest.getBrandNo(), StringUtil.makeQueryStringAllRegExp(cameraRequest.getName()),
                cameraRequest.getCameraStatus(), cameraRequest.getAfter(), DataStatusType.VALID.KEY);
    }

    @Override
    public Page<Camera> getCameras(CameraRequest cameraRequest, int page, int size) {
        Set<ObjectId> buildingIdSet = cameraRequest.getBuildingId();
        if(buildingIdSet == null || buildingIdSet.isEmpty()){
            buildingIdSet = null;
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.ASC, "createAt"));
        org.springframework.data.domain.Page<Camera> pageList =
                cameraRepository.findByCommunityIdAndBuildingIdInAndCameraCodeRegexAndBrandNoAndNameRegexAndCameraStatusAndUpdateAtGreaterThanAndDataStatusAllIgnoreNull(
                cameraRequest.getCommunityId(), buildingIdSet, StringUtil.makeQueryStringAllRegExp(cameraRequest.getCameraCode()),
                cameraRequest.getBrandNo(), StringUtil.makeQueryStringAllRegExp(cameraRequest.getName()),
                cameraRequest.getCameraStatus(), cameraRequest.getAfter(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(pageList);
    }

    @Override
    public Integer updateByBrandNo(Camera entity) throws BizException {
        if (entity.getBrandNo() == null) {
            throw NOT_CAMERA_BRAND;
        }
        Camera item = new Camera();
        switch (entity.getBrandNo()) {
            case 1:
                // 宇视：修改设备
                item.setDeviceName(entity.getDeviceName());
                item.setDevicePassword(entity.getDevicePassword());
                item.setUpdateAt(new Date());
                break;
            case 2:
                // 萤石：修改APPkey和密钥
                item.setAppKey(entity.getAppKey());
                item.setSecret(entity.getSecret());
                item.setUpdateAt(new Date());
                break;
            default:
                throw CAMERA_BRAND_INVALID;
        }
        return cameraRepository.updateByBrandNoAndDataStatus(item, entity.getBrandNo(), DataStatusType.VALID.KEY);
    }

    @Override
    public Camera getAppkeyAndSecretByBrandNo(Integer brandNo) {
        return cameraRepository.findByBrandNoAndDataStatus(brandNo, DataStatusType.VALID.KEY);
    }

    @Override
    public EzvizTokenVO getEzvizToken(Integer brandNo) {
        // 从缓存中获取token
        String token = RedisTemplateUtil.getStr(EZVIZ_KEY);
        Long expireTime = RedisTemplateUtil.getLong(EZVIZ_KEY_EXPIRE);
        if (StringUtil.isNotNull(token) && expireTime != null) {
            EzvizTokenVO ezvizTokenVO = new EzvizTokenVO(token,expireTime);
            return ezvizTokenVO;
        }
        return null;
    }

    @Override
    public Boolean setEzvizToken(EzvizTokenVO ezvizTokenVO, long ttl) {
        // 防止并发，true则表示可以修改数据
        RedisLock redisLock = RedisTemplateUtil.getRedisLock(EZVIZ_LOCK, null, null);
        try {
            if (redisLock.lock()) {
                RedisTemplateUtil.set(EZVIZ_KEY, ezvizTokenVO.getAccessToken(), ttl);
                RedisTemplateUtil.set(EZVIZ_KEY_EXPIRE, ezvizTokenVO.getExpireTime(), ttl);
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException e) {
            log.error("InterruptedException:", e);
        }finally {
            redisLock.unlock();
        }
        return false;
    }

}
