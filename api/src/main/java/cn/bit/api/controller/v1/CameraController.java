package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.facade.model.communityIoT.Camera;
import cn.bit.facade.service.community.BuildingFacade;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.communityIoT.CameraFacade;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.facade.service.user.UserToRoomFacade;
import cn.bit.facade.vo.communityIoT.camera.CameraRequest;
import cn.bit.facade.vo.communityIoT.camera.EzvizTokenVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import com.alibaba.fastjson.JSONObject;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static cn.bit.facade.exception.CommonBizException.AUTHENCATION_FAILD;

@RestController
@RequestMapping(value = "/v1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
public class CameraController {

    @Autowired
    private CameraFacade cameraFacade;

    @Autowired
    private UserToRoomFacade userToRoomFacade;

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Autowired
    private CommunityFacade communityFacade;

    @Autowired
    private BuildingFacade buildingFacade;

    /**
     * restTemplate请求
     */
    @Autowired
    private RestTemplate restTemplate;

    @Value("${ezviz.url}")
    private String ezvizUrl;

    /**
     * 新增摄像头
     * @param camera
     * @return
     */
    @PostMapping(name = "新增摄像头", path = "/communityIoT/camera/add")
    @Authorization
    public ApiResult addCamera(@Validated @RequestBody Camera camera) {
        camera.setCreatorId(SessionUtil.getTokenSubject().getUid());
        camera.setCommunityId(SessionUtil.getCommunityId());
        Camera saved = cameraFacade.addCamera(camera);
        // addToSet brands
        communityFacade.addToSetBrandsById(saved.getCommunityId(), Collections.singleton("camera" + saved.getBrandNo()));
        return ApiResult.ok(saved);
    }

    /**
     * 修改摄像头
     * @param camera
     * @return
     */
    @PostMapping(name = "编辑摄像头", path = "/communityIoT/camera/edit")
    @Authorization
    public ApiResult updateCamera(@RequestBody Camera camera) {
        camera = cameraFacade.updateCamera(camera);
        if (camera == null) {
            return ApiResult.error(-1, "更新监控设备失败");
        }
        return ApiResult.ok(camera);
    }

    /**
     * 删除摄像头
     * @param id
     * @return
     */
    @GetMapping(name = "删除摄像头", path = "/communityIoT/camera/{id}/delete")
    @Authorization
    public ApiResult deleteCamera(@PathVariable ObjectId id) {
        Camera camera = cameraFacade.deleteCamera(id);
        // 校验社区是否还存在相同品牌的设备
        CameraRequest request = new CameraRequest();
        request.setCommunityId(camera.getCommunityId());
        request.setBrandNo(camera.getBrandNo());
        List<Camera> cameras = cameraFacade.getCameras(request);
        if (cameras.isEmpty()) {
            communityFacade.pullAllBrandsById(camera.getCommunityId(), Collections.singleton("camera" + camera.getBrandNo()));
        }
        return ApiResult.ok();
    }

    /**
     * 摄像头详细
     * @param id
     * @return
     */
    @GetMapping(name = "摄像头详情", path = "/communityIoT/camera/{id}/detail")
    @Authorization
    public ApiResult getCamera(@PathVariable ObjectId id) {
        return ApiResult.ok(cameraFacade.getCameraById(id));
    }

    /**
     * 摄像头分页（业主）
     * @param cameraRequest
     * @param page
     * @param size
     * @return
     */
    @PostMapping(name = "业主摄像头分页", path = "/communityIoT/camera/auth/page")
    @Authorization
    public ApiResult getAuthorizedCameras(@RequestBody CameraRequest cameraRequest,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "10") Integer size) {
        cameraRequest.setCommunityId(SessionUtil.getCommunityId());
        Set<ObjectId> buildingIds = userToRoomFacade.getBuildingsByUserId(
                cameraRequest.getCommunityId(), SessionUtil.getTokenSubject().getUid());
        if (buildingIds.isEmpty()) {
            throw AUTHENCATION_FAILD;
        }
        cameraRequest.setBuildingId(buildingIds);
        return ApiResult.ok(cameraFacade.getCameras(cameraRequest, page, size));
    }

    /**
     * 获取摄像头列表（业主）
     * @param cameraRequest
     * @return
     */
    @PostMapping(name = "摄像头列表", path = "/communityIoT/camera/auth/list")
    @Authorization
    public ApiResult getAuthorizedCameras(@RequestBody CameraRequest cameraRequest) {
        cameraRequest.setCommunityId(SessionUtil.getCommunityId());
        Set<ObjectId> buildingIds = userToRoomFacade.getBuildingsByUserId(
                cameraRequest.getCommunityId(), SessionUtil.getTokenSubject().getUid());
        if (buildingIds.isEmpty()) {
            throw AUTHENCATION_FAILD;
        }
        cameraRequest.setBuildingId(buildingIds);
        return ApiResult.ok(cameraFacade.getCameras(cameraRequest));
    }

    /**
     * 摄像头分页（物业）
     * @param cameraRequest
     * @param page
     * @param size
     * @return
     */
    @PostMapping(name = "摄像头分页", path = "/communityIoT/camera/page")
    @Authorization
    public ApiResult getCameras(@RequestBody CameraRequest cameraRequest,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size) {
        cameraRequest.setCommunityId(SessionUtil.getCommunityId());
        UserToProperty userToProperty = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                SessionUtil.getTokenSubject().getUid(), cameraRequest.getCommunityId(), SessionUtil.getCompanyId());
        if (userToProperty == null) {
            throw AUTHENCATION_FAILD;
        }
        cameraRequest.setBuildingId(userToProperty.getBuildingIds());

        return ApiResult.ok(cameraFacade.getCameras(cameraRequest, page, size));
    }

    /**
     * 获取摄像头列表（物业）
     * @param cameraRequest
     * @return
     */
    @PostMapping(name = "摄像头列表", path = "/communityIoT/camera/list")
    @Authorization
    public ApiResult getCameras(@RequestBody CameraRequest cameraRequest) {
        cameraRequest.setCommunityId(SessionUtil.getCommunityId());
        UserToProperty userToProperty = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                SessionUtil.getTokenSubject().getUid(), cameraRequest.getCommunityId(), SessionUtil.getCompanyId());
        if (userToProperty == null) {
            throw AUTHENCATION_FAILD;
        }

        cameraRequest.setBuildingId(userToProperty.getBuildingIds());

        return ApiResult.ok(cameraFacade.getCameras(cameraRequest));
    }

    /**
     * 根据摄像头厂商修改设备名称或设备密码
     * @param entity
     * @return
     */
    @PostMapping(name = "根据摄像头厂商修改设备名称或设备密码", path = "/communityIoT/camera/editByBrandNo")
    @Authorization
    public ApiResult getCamerasTest(@RequestBody Camera entity) {
        return ApiResult.ok(cameraFacade.updateByBrandNo(entity));
    }

    /**
     * 根据厂商编号获取appkey和密钥
     * @param brandNo
     * @return
     */
    /*@GetMapping(name = "", path = "/communityIoT/camera/{brandNo}/get-appkey-secret")
    @Authorization
    public ApiResult getAppKeyAndSecretByBrandNo(@PathVariable("brandNo") Integer brandNo){
        Camera camera = cameraFacade.getAppkeyAndSecretByBrandNo(brandNo);
        Map<String, String> map = new HashMap<>();
        map.put("appKey", camera.getAppKey());
        map.put("secret", camera.getSecret());
        return ApiResult.ok(map);
    }*/

    /**
     * 获取token
     * @param brandNo
     * @return
     */
    @GetMapping(name = "获取萤石摄像头token", path = "/communityIoT/camera/{brandNo}/getEzvizToken")
    @Authorization
    public ApiResult getToken(@PathVariable("brandNo") Integer brandNo) throws InterruptedException {
        // 从缓存中获取token
        EzvizTokenVO ezvizTokenVO = cameraFacade.getEzvizToken(brandNo);
        if (ezvizTokenVO != null) {
            return ApiResult.ok(ezvizTokenVO);
        }
        // 如果缓存没有则从接口获取
        Camera camera = cameraFacade.getAppkeyAndSecretByBrandNo(brandNo);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 赋值
        MultiValueMap<String, String> map= new LinkedMultiValueMap<String, String>();
        map.add("appKey", camera.getAppKey());
        map.add("appSecret", camera.getSecret());
        //
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        ResponseEntity<Object> response = restTemplate.postForEntity(ezvizUrl, request , Object.class );
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(response.getBody());
        Object code = jsonObject.get("code");

        // LinkedHashMap linkedHashMap = (LinkedHashMap) response.getBody();
        // 200表示成功
        if ("200".equals(code)) {
            JSONObject data = (JSONObject) jsonObject.get("data");
            Object n_token = data.get("accessToken");
            if (n_token == null) {
                return ApiResult.error(-1, "token为空");
            }

            long expireTime = Long.parseLong(data.get("expireTime").toString());
            ezvizTokenVO = new EzvizTokenVO(n_token.toString(), expireTime);
            long ttl = (expireTime - System.currentTimeMillis())/1000;

            // 放置缓存
            Boolean flag = cameraFacade.setEzvizToken(ezvizTokenVO, ttl);
            if (flag) {
                return ApiResult.ok(ezvizTokenVO);
            } else {
                return ApiResult.error(-1, "请重新获取token");
            }
        }
        return ApiResult.error(-1, jsonObject.get("msg").toString());
    }
}
