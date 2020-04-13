package cn.bit.facade.service.communityIoT;

import cn.bit.facade.model.communityIoT.Camera;
import cn.bit.facade.vo.communityIoT.camera.CameraRequest;
import cn.bit.facade.vo.communityIoT.camera.EzvizTokenVO;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

public interface CameraFacade {

    /**
     * 获取摄像头
     * @param id
     * @return
     */
    Camera getCameraById(ObjectId id);

    /**
     * 新增摄像头
     * @param camera
     * @return
     */
    Camera addCamera(Camera camera);

    /**
     * 更新摄像头
     * @param camera
     * @return
     */
    Camera updateCamera(Camera camera);

    /**
     * 删除摄像头
     * @param id
     * @return
     */
    Camera deleteCamera(ObjectId id);

    /**
     * 获取摄像头
     * @param cameraRequest
     * @return
     */
    List<Camera> getCameras(CameraRequest cameraRequest);

    /**
     * 摄像头分页
     * @param cameraRequest
     * @param page
     * @param size
     * @return
     */
    Page<Camera> getCameras(CameraRequest cameraRequest, int page, int size);

    /**
     * 根据厂商修改
     * 设备名称和设备密码
     * @return
     */
    Integer updateByBrandNo(Camera entity) throws BizException;

    /**
     * 根据厂商获取appKey和密钥
     * @param brandNo
     * @return
     */
    Camera getAppkeyAndSecretByBrandNo(Integer brandNo);

    /**
     * 获取海康token
     * @param brandNo
     * @return
     */
    EzvizTokenVO getEzvizToken(Integer brandNo);

    /**
     * 存放缓存
     * @param ezvizTokenVO
     * @param ttl
     * @return
     */
    Boolean setEzvizToken(EzvizTokenVO ezvizTokenVO, long ttl);

}
