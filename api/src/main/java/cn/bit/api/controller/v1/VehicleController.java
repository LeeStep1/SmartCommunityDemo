package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.AppSubject;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.IdentityStatus;
import cn.bit.facade.enums.VerifiedType;
import cn.bit.facade.model.user.CommunityUser;
import cn.bit.facade.model.user.Household;
import cn.bit.facade.model.vehicle.Apply;
import cn.bit.facade.model.vehicle.Gate;
import cn.bit.facade.model.vehicle.Identity;
import cn.bit.facade.model.vehicle.InOut;
import cn.bit.facade.service.user.HouseholdFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.facade.service.vehicle.CarFacade;
import cn.bit.facade.service.vehicle.InoutFacade;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.facade.vo.vehicle.*;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static cn.bit.facade.exception.CommonBizException.AUTHENCATION_FAILD;
import static cn.bit.facade.exception.CommonBizException.DATA_INVALID;
import static cn.bit.facade.exception.user.UserBizException.PROPERTY_NOT_EXIST;
import static cn.bit.facade.exception.user.UserBizException.USER_NOT_EXITS;
import static cn.bit.framework.exceptions.BizException.OPERATION_FAILURE;

@RestController
@RequestMapping(value = "/v1/vehicle", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class VehicleController {
    @Autowired
    private InoutFacade inoutFacade;

    @Autowired
    private CarFacade carFacade;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Autowired
    private HouseholdFacade householdFacade;

    /**
     * 获取所有车闸列表（物业）
     * @return
     */
    @GetMapping(name = "车闸列表", path = "/car-gate/list")
    @Authorization
    public ApiResult getAllCarGate() {
        List<Gate> gates = inoutFacade.getAllCarGate(SessionUtil.getCommunityId());
        return ApiResult.ok(gates);
    }

    /**
     * 查询车闸（物业）分页
     * @return
     */
    @PostMapping(name = "车闸分页", path = "/car-gate/page")
    @Authorization
    public ApiResult getAllCarGatePage(@RequestBody Gate gate,
                                       @RequestParam(defaultValue = "1") Integer page,
                                        @RequestParam(defaultValue = "10") Integer size) {
        gate.setCommunityId(SessionUtil.getCommunityId());
        Page<Gate> result = inoutFacade.getAllCarGatePage(gate, page, size);
        return ApiResult.ok(result);
    }

    /**
     * 根据车闸获取进出记录（物业）
     * @return
     */
    @PostMapping(name = "某车闸进出记录列表", path = "/inout/list")
    @Authorization
    public ApiResult getInoutRecordByCarGate(@RequestBody InoutRequest request) {
        List<InOut> inOuts = inoutFacade.getInoutRecordByCarGate(request, SessionUtil.getCommunityId());
        return ApiResult.ok(inOuts);
    }

    /**
     * 分页获取进出记录（大屏）
     * @return
     */
    @GetMapping(name = "进出记录分页(大屏)", path = "/inout/page")
    @Authorization
    public ApiResult getInoutRecordByCommunity(@RequestParam(defaultValue = "1") Integer page,
                                               @RequestParam(defaultValue = "10") Integer size) {
        Page<InOut> inOuts = inoutFacade.listInoutRecords(SessionUtil.getCommunityId(), page, size);
        return ApiResult.ok(inOuts);
    }

    /**
     * 根据车闸获取进出记录（物业）分页
     * @return
     */
    @PostMapping(name = "某车闸进出记录分页", path = "/inout/page")
    @Authorization
    public ApiResult getInoutRecordByCarGatePage(@RequestBody InoutRequest request,
                                                 @RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "10") Integer size) {
        Page<InOut> result = inoutFacade.getInoutRecordByCarGatePage(request, SessionUtil.getCommunityId(), page, size);
        return ApiResult.ok(result);
    }

    /**
     * 根据车牌获取进出记录（物业）分页
     * @return
     */
    @PostMapping(name = "某车牌进出记录分页", path = "/car/inout/page")
    @Authorization
    public ApiResult getCarInoutRecordByCarNo(
            @Validated(value = InoutRequest.CarInOut.class) @RequestBody InoutRequest request,
            @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer size) {

        request.setLeaveAtBefore(request.getLeaveAtBefore() != null ? DateUtils.getDayEnd(request.getLeaveAtBefore()) : null);
        request.setEnterAtBefore(request.getEnterAtBefore() != null ? DateUtils.getDayEnd(request.getEnterAtBefore()) : null);
        Page<CarInOutVO> result = inoutFacade.getInoutRecordByCarNo(request, SessionUtil.getCommunityId(), page, size);
        return ApiResult.ok(result);
    }

    /**
     * 物业获取已入闸车辆列表
     *
     * @return
     */
    @GetMapping(name = "已入闸车辆列表", path = "/findParkingLotCar")
    @Authorization
    public ApiResult findParkingLotCar() {
        return ApiResult.ok(inoutFacade.findParkingLotCar(SessionUtil.getCommunityId()));
    }

    /**
     * 物业获取已入闸车辆数量
     *
     * @return
     */
    @GetMapping(name = "已入闸车辆总数", path = "/parkingCarCount")
    @Authorization
    public ApiResult parkingCarCount() {
        return ApiResult.ok(inoutFacade.countParkingCar(SessionUtil.getCommunityId()));
    }

    /**
     * 物业根据车牌号或日期查询车辆信息
     */
    @PostMapping(name = "车辆进出记录列表", path = "/findInoutRecord")
    @Authorization
    public ApiResult findInoutRecord(@RequestBody InOut entity) {
        entity.setCommunityId(SessionUtil.getCommunityId());
        return ApiResult.ok(inoutFacade.findInoutRecord(entity));
    }

    /**
     * 业主申请车牌
     */
    @PostMapping(name = "业主申请车牌", path = "/applyCarNum")
    @Authorization
    public ApiResult applyCarNum(@RequestBody @Valid Apply apply) {
        Apply car;
        CommunityUser communityUser =
                userFacade.getCommunityUserByCommunityIdAndUserId(apply.getCommunityId(), apply.getUserId());
        if (communityUser == null || CollectionUtils.isEmpty(communityUser.getRoles())) {
            throw USER_NOT_EXITS;
        }

        // 查找业主的用户信息
        UserVO user = userFacade
                .getUserById(ClientType.HOUSEHOLD.value(), SessionUtil.getAppSubject().getPartner(), apply.getUserId());
        apply.setUserName(user.getName());
        apply.setCreatorId(SessionUtil.getTokenSubject().getUid());
        car = carFacade.addCar(apply);
        return ApiResult.ok(car);
    }

    /**
     * 物业申请车牌
     */
    @PostMapping(name = "物业申请车牌", path = "/applyCarNum/property")
    @Authorization
    public ApiResult applyCarNumForProperty(@RequestBody @Valid Apply apply) {
        Apply car;

        // 物业人员
        UserToProperty userToProperty = userToPropertyFacade.findByUserIdAndCommunityIdAndCompanyId(
                apply.getUserId(), SessionUtil.getCommunityId(), SessionUtil.getCompanyId());
        if(userToProperty == null){
            throw PROPERTY_NOT_EXIST;
        }
        String userName = userToProperty.getUserName();
        String phone = userToProperty.getPhone();

        apply.setUserName(userName);
        apply.setCommunityId(SessionUtil.getCommunityId());
        apply.setCreatorId(SessionUtil.getTokenSubject().getUid());
        car = carFacade.addCarByProperty(apply, phone);
        if (car == null) {
            throw OPERATION_FAILURE;
        }
        return ApiResult.ok(car);
    }

    /**
     * 物业审核
     */
    @PostMapping(name = "物业审核车牌", path = "/auditCar")
    @Authorization
    public ApiResult auditCar(@RequestBody @Validated VehicleAuditVo auditVo) {
        AppSubject appSubject = SessionUtil.getAppSubject();
        Apply apply = carFacade.findCar(auditVo.getCarId());
        if (apply == null || apply.getAuditStatus() != VerifiedType.UNREVIEWED.getKEY()) {
            throw DATA_INVALID;
        }
        UserVO user = userFacade.getUserById(ClientType.HOUSEHOLD.value(), appSubject.getPartner(), apply.getUserId());
        carFacade.auditCar(
                auditVo.getCarId(), user.getPhone(), auditVo.getVerifyCode(), SessionUtil.getTokenSubject().getUid());
        return ApiResult.ok();
    }

    /**
     * 业主/物业解绑车牌
     */
    @GetMapping(name = "解绑车牌", path = "/deleteCarNo/{carId}")
    @Authorization
    public ApiResult deleteCarNo(@PathVariable ObjectId carId) {
        Integer client = SessionUtil.getAppSubject().getClient();
        if (client == ClientType.HOUSEHOLD.value()) {
            boolean ownerCar = carFacade.isOwnerCar(SessionUtil.getTokenSubject().getUid(), carId);
            if (!ownerCar) {
                throw AUTHENCATION_FAILD;
            }
        }
        boolean flag = carFacade.unboundCar(carId, SessionUtil.getTokenSubject().getUid());
        return flag ? ApiResult.ok() : ApiResult.error(-1, "解绑失败");
    }

    /**
     * 查看业主车辆信息
     */
    @GetMapping(name = "业主车辆列表", path = "/{communityId}/owner-car")
    @Authorization
    public ApiResult getOwnerCars(@PathVariable("communityId") ObjectId communityId) {
        List<Apply> applies = carFacade.findCarByUserIdAndCommunityId(SessionUtil.getTokenSubject().getUid(), communityId);
        return ApiResult.ok(applies);
    }

    /**
     * 根据房间查询业主的车辆信息
     */
    @GetMapping(name = "某房间业主的车辆列表", path = "/{roomId}/proprietor-cars")
    @Authorization
    public ApiResult listOwnerCarsByRoom(@PathVariable("roomId") ObjectId roomId) {
        Household household = householdFacade.findAuthOwnerByRoom(roomId);
        if (household == null) {
            log.info("房间（{}）没有业主档案", roomId);
            return ApiResult.ok();
        }
        List<Apply> carList = carFacade.findCarByUserIdAndCommunityIdAndAuditStatus(
                household.getUserId(), household.getCommunityId(), VerifiedType.REVIEWED.getKEY());
        return ApiResult.ok(carList);
    }

    /**
     * 物业查看车辆信息（分页）
     */
    @PostMapping(name = "车辆信息分页", path = "/page")
    @Authorization
    public ApiResult getAllCars(@RequestBody CarRequest carRequest,
                                @RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer size) {
        Page<Apply> carPage = carFacade.queryCarPage(SessionUtil.getCommunityId(), carRequest, page, size);
        return ApiResult.ok(carPage);
    }

    // ==========================================[car identity start]=========================================================

    @PostMapping(name = "车牌分页", path = "/identity/page")
    @Authorization
    public ApiResult getIdentityCars(@RequestBody IdentityQuery identityQuery,
                                     @RequestParam(defaultValue = "1") Integer page,
                                     @RequestParam(defaultValue = "10") Integer size) {
        Page<Identity> identityPage =
                carFacade.queryIdentityPage(SessionUtil.getCommunityId(), identityQuery, page, size);
        Date current = new Date();
        List<IdentityVO> identityVOS = new ArrayList<>();
        for (Identity identity : identityPage.getRecords()) {
            IdentityVO vo = new IdentityVO();
            BeanUtils.copyProperties(identity, vo);
            identityVOS.add(vo);
            if (current.after(identity.getEndAt())) {
                vo.setStatus(IdentityStatus.INVALID.KEY);
                continue;
            }
            vo.setStatus(IdentityStatus.VALID.KEY);
        }
        return ApiResult.ok(new Page<>(identityPage.getCurrentPage(), identityPage.getTotal(), size, identityVOS));
    }

    @GetMapping(name = "删除车牌信息", path = "/identity/{id}/remove")
    @Authorization
    public ApiResult removeCarIdentity(@PathVariable("id") ObjectId IdentityId) {
        return ApiResult.ok(carFacade.removeCarIdentity(IdentityId));
    }
}