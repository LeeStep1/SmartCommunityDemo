package cn.bit.facade.service.communityIoT;

import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.vo.communityIoT.elevator.*;
import cn.bit.facade.vo.mq.KangTuElevatorAuthVO;
import cn.bit.facade.vo.statistics.ElevatorSummaryResponse;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;

public interface ElevatorFacade {

    /**
     * 通过楼房Id和蓝牙mac地址查询电梯列表 用于蓝牙招梯
     * @param request
     * @param page
     * @param size
     * @return
     * @throws Exception
     */
    ElevatorPageResult getElevators(FindElevatorListRequest request, Integer page, Integer size);



    /**
     * 电梯故障列表
     * @param elevatorId
     * @return
     * @throws BizException
     */
    Object findElevatorFaultList(String elevatorId, Integer page, Integer size);


    /**
     * 电梯维修记录
     * @param elevatorId
     * @return
     * @throws BizException
     */
    Object findElevatorRepairList(String elevatorId, Integer page, Integer size);


    /**
     * 故障报修同步到电梯互联网
     * @return
     * @throws Exception
     */
    Object addElevatorFault(ElevatorFault elevatorFault) throws Exception;

    /**
     * 远程召梯，兼容旧版本app
     * @param request
     * @return
     */
    Object remoteCallElevator(Room room, CallElevatorRequest request);
    /**
     * 远程召梯
     * @param request
     * @return
     */
    Object remoteCallElevator(CallElevatorRequest request);

    /**
     * 查询电梯品牌
     * @return
     */
    Object findElevatorBrandList();

    /**
     * 更新电梯权限
     * @param deviceAuthVO
     * @return
     */
    boolean updateIoTElevatorAuth(KangTuElevatorAuthVO deviceAuthVO);

    /**
     * 删除电梯权限
     * @param deviceAuthVO
     * @return
     */
    boolean deleteIoTElevatorAuth(KangTuElevatorAuthVO deviceAuthVO);

    /**
     * 覆盖电梯权限
     * @param deviceAuthVO
     * @return
     */
    boolean coverIoTElevatorAuth(KangTuElevatorAuthVO deviceAuthVO);

    boolean openElevatorControlStatus(ElevatorVO elevatorVO);

    boolean closeElevatorControlStatus(ElevatorVO elevatorVO);

    /**
     * 统计社区下的电梯数量
     * @param communityId
     * @return
     */
    ElevatorSummaryResponse summaryElevators(ObjectId communityId);

    /**
     * 查询电梯详情
     * @param elevatorDetailQO
     * @return
     */
    ElevatorDetailDTO getElevatorDetail(ElevatorDetailQO elevatorDetailQO);

    /**
     *
     * @param deviceNum
     * @param card
     */
    void coverAuthByDeviceNumAndCard(List<String> deviceNum, Card card);
}
