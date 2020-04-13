package cn.bit.facade.service.vehicle;

import cn.bit.facade.model.vehicle.Apply;
import cn.bit.facade.model.vehicle.Identity;
import cn.bit.facade.vo.vehicle.CarRequest;
import cn.bit.facade.vo.vehicle.IdentityQuery;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * 车辆接口(Mongodb)
 */
public interface CarFacade
{
    /**
     * 根据id获取车辆信息
     * @param id
     * @return
     * @throws BizException
     */
    Apply findCar(ObjectId id) throws BizException;

    /**
     * 根据userId获取车辆信息
     * @param userId
     * @return
     * @throws BizException
     */
    List<Apply> findCarByUserIdAndCommunityId(ObjectId userId, ObjectId communityId) throws BizException;

    /**
     * 根据车牌号获取车辆信息
     * @param carNo
     * @return
     * @throws BizException
     */
    Apply findCarByCarNo(String carNo) throws BizException;

    /**
     * 车辆信息录入
     * @param entity
     * @return
     * @throws BizException
     */
    Apply addCar(Apply entity) throws BizException;

    /**
     * 车辆信息审核
     * @param carId
     * @param auditStatus
     * @param auditorId
     * @return
     * @throws BizException
     */
    boolean auditCar(ObjectId carId, String phoneNum, int auditStatus, ObjectId auditorId) throws BizException;

    /**
     * 解绑车辆信息
     * @param carId
     * @param operatorId
     * @return
     * @throws BizException
     */
    boolean unboundCar(ObjectId carId, ObjectId operatorId) throws BizException;

    /**
     * 判断该车牌是否属于该业主
     * @param uid
     * @param carId
     * @return
     * @throws BizException
     */
    boolean isOwnerCar(ObjectId uid, ObjectId carId) throws BizException;

    /**
     * 物业分页查询车辆列表
     * @param communityId
     * @param carRequest
     * @param page
     * @param size
     * @return
     */
    Page<Apply> queryCarPage(ObjectId communityId, CarRequest carRequest, Integer page, Integer size);

    /**
     * 查询用户的常用车辆列表
     * @param userId
     * @param communityId
     * @param dataStatus
     * @return
     */
    List<Apply> findCarByUserIdAndCommunityIdAndAuditStatus(ObjectId userId, ObjectId communityId, int dataStatus);

    /**
     * 物业申请车牌
     * @param apply
     * @param phone
     * @return
     */
    Apply addCarByProperty(Apply apply, String phone);

    /**
     * 车库管理 (分页)
     * @param communityId
     * @param identityQuery
     * @param page
     * @param size
     * @return
     */
    Page<Identity> queryIdentityPage(ObjectId communityId, IdentityQuery identityQuery, Integer page, Integer size);

    /**
     * 只是删除mongo的数据，并不删除实际车库的数据
     * @param identityId
     */
    Identity removeCarIdentity(ObjectId identityId);
}
