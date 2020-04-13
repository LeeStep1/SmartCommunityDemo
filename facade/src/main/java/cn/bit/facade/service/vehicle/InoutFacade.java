package cn.bit.facade.service.vehicle;

import cn.bit.facade.model.vehicle.Gate;
import cn.bit.facade.model.vehicle.InOut;
import cn.bit.facade.vo.vehicle.CarInOutVO;
import cn.bit.facade.vo.vehicle.InoutRequest;
import cn.bit.facade.vo.vehicle.InoutVo;
import cn.bit.framework.data.common.Page;
import org.bson.types.ObjectId;

import java.util.List;

/**
 * 车场出入记接口
 */
public interface InoutFacade
{
    /**
     * 获取当前车场出入记录
     * @return
     * @param communityId
     */
    List<InOut> findParkingLotCar(ObjectId communityId);

    /**
     * 物业端根据车牌或时间获取出入记录
     * @param vo
     * @return
     */
    List<InoutVo> findInoutRecord(InOut vo);

    /**
     * 获取当前所有的车闸
     * @return
     * @param communityId
     */
    List<Gate> getAllCarGate(ObjectId communityId);

    /**
     * 查询车闸(分页)
     * @return
     */
    Page<Gate> getAllCarGatePage(Gate gate, int page, int size);
    /**
     * 根据车闸获取进出记录
     * @return
     */
    List<InOut> getInoutRecordByCarGate(InoutRequest request, ObjectId communityId);

    /**
     * 根据车闸分页获取进出记录
     * @param request
     * @param communityId
     * @return
     */
    Page<InOut> getInoutRecordByCarGatePage(InoutRequest request, ObjectId communityId, int page, int size);

    Page<CarInOutVO> getInoutRecordByCarNo(InoutRequest request, ObjectId communityId, Integer page, Integer size);

    /**
     * 统计已入闸的车辆数量
     * @param communityId
     * @return
     */
    Long countParkingCar(ObjectId communityId);

    /**
     * 分页获取车辆出入记录
     * @param communityId
     * @param page
     * @param size
     * @return
     */
    Page<InOut> listInoutRecords(ObjectId communityId, Integer page, Integer size);
}
