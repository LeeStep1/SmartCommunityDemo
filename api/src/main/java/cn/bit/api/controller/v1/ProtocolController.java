package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.facade.service.communityIoT.ProtocolFacade;
import cn.bit.facade.vo.communityIoT.protocol.BanCardVO;
import cn.bit.facade.vo.communityIoT.protocol.IcCardVO;
import cn.bit.facade.vo.communityIoT.protocol.ProtocolVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static cn.bit.facade.exception.community.CommunityBizException.BUILDING_ID_NULL;
import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.*;

@RestController
@RequestMapping(value = "/v1/protocol", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class ProtocolController {

    @Autowired
    private ProtocolFacade protocolFacade;

    // ============================================【设备离线协议】====================================================

    /**
     * 读取设备mac地址
     *
     * @return
     */
    @GetMapping(name = "读取设备mac地址", path = "/read-device-mac")
    @Authorization
    public ApiResult readDeviceMacProtocol() {
        String key = protocolFacade.encodeProtocol4ReadDevice(SessionUtil.getCommunityId());
        return ApiResult.ok(key);
    }

    /**
     * 终端id写入协议
     *
     * @param requestVO
     * @return
     */
    @PostMapping(name = "终端id写入协议", path = "/decode-4Terminal")
    @Authorization
    public ApiResult decodeProtocol4Terminal(@RequestBody ProtocolVO requestVO) {
        String key = protocolFacade.decodeProtocol4Terminal(requestVO, SessionUtil.getCommunityId());
        return ApiResult.ok(key);
    }

    /**
     * 物业发放离线IC卡
     *
     * @param icCardVO
     * @return
     */
    @PostMapping(name = "物业发放离线IC卡", path = "/encode-4ic-card")
    @Authorization
    public ApiResult applyIcCardProtocol(@Validated @RequestBody IcCardVO icCardVO) {
        String key = protocolFacade.encodeProtocol4IC(SessionUtil.getCommunityId(), icCardVO);
        return ApiResult.ok(key);
    }

    /**
     * 物业禁卡协议
     *
     * @param banCardVO
     * @return
     */
    @PostMapping(name = "物业禁卡协议", path = "/encode-4ban-card")
    @Authorization
    public ApiResult banCardProtocol(@RequestBody BanCardVO banCardVO) {
        if (banCardVO.getBuildingId() == null) {
            throw BUILDING_ID_NULL;
        }
        if (banCardVO.getRoomIds() == null || banCardVO.getRoomIds().isEmpty()) {
            throw ROOMS_EMPTY;
        }
        BanCardVO result = protocolFacade.encodeProtocol4BanCard(banCardVO.getBuildingId(), banCardVO.getRoomIds());
        if (result != null) {
            result.setBuildingId(banCardVO.getBuildingId());
            result.setRoomIds(banCardVO.getRoomIds());
        }
        return ApiResult.ok(result);
    }

    /**
     * 物业解除禁卡协议
     *
     * @param banCardVO
     * @return
     */
    @PostMapping(name = "物业解除禁卡协议", path = "/encode-4lift-ban-card")
    @Authorization
    public ApiResult liftBanCardProtocol(@RequestBody BanCardVO banCardVO) {
        if (banCardVO.getBuildingId() == null) {
            throw BUILDING_ID_NULL;
        }
        if (banCardVO.getRoomIds() == null || banCardVO.getRoomIds().isEmpty()) {
            throw ROOMS_EMPTY;
        }
        BanCardVO result = protocolFacade.encodeProtocol4LiftBanCard(banCardVO.getBuildingId(), banCardVO.getRoomIds());
        if (result != null) {
            result.setBuildingId(banCardVO.getBuildingId());
            result.setRoomIds(banCardVO.getRoomIds());
        }
        return ApiResult.ok(result);
    }

    /**
     * 开启禁用梯控时间协议
     *
     * @param banCardVO
     * @return
     */
    @PostMapping(name = "开启禁用梯控时间协议", path = "/encode-4lift-control")
    @Authorization
    public ApiResult liftControlProtocol(@RequestBody BanCardVO banCardVO) {
        if (banCardVO.getBuildingId() == null) {
            throw BUILDING_ID_NULL;
        }
        if (banCardVO.getStartAt() == null || banCardVO.getEndAt() == null) {
            throw LIFT_CONTROL_DATE_IS_NULL;
        }
        if (banCardVO.getStartAt().after(banCardVO.getEndAt())) {
            throw STARTAT_AFTER_ENDAT;
        }
        String key = protocolFacade.encodeProtocol4LiftControl(
                banCardVO.getBuildingId(), banCardVO.getStartAt(), banCardVO.getEndAt());
        return ApiResult.ok(key);
    }
}