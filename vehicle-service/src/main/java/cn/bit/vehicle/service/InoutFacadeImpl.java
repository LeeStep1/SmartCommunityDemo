package cn.bit.vehicle.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.InOutTypeEnum;
import cn.bit.facade.model.vehicle.Gate;
import cn.bit.facade.model.vehicle.InOut;
import cn.bit.facade.service.vehicle.InoutFacade;
import cn.bit.facade.vo.vehicle.CarInOutVO;
import cn.bit.facade.vo.vehicle.InoutRequest;
import cn.bit.facade.vo.vehicle.InoutVo;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.vehicle.dao.GateRepository;
import cn.bit.vehicle.dao.InOutRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service("inoutFacade")
@Slf4j
public class InoutFacadeImpl implements InoutFacade {
    @Autowired
    private GateRepository gateRepository;

    @Autowired
    private InOutRepository inOutRepository;

    @Override
    public List<InOut> findParkingLotCar(ObjectId communityId) {
        return inOutRepository.findByCommunityIdAndEnterAtNotNullAndLeaveAtNullAndTypeAndDataStatusOrderByEnterAtDesc(
                communityId, InOutTypeEnum.IN.KEY, DataStatusType.VALID.KEY);
    }

    @Override
    public List<InoutVo> findInoutRecord(InOut entity) {
        Date endAt = null;
        if (entity.getEnterAt() != null) {
            endAt = DateUtils.addDay(entity.getEnterAt(), 1);
        }
        List<InOut> record = inOutRepository
                .findByCarNoAndCommunityIdAndEnterAtAfterAndEnterAtBeforeAndDataStatusAllIgnoreNullOrderByEnterAtDesc(
                        StringUtil.isNotEmpty(entity.getCarNo()) ? entity.getCarNo() : null, entity.getCommunityId(),
                        entity.getEnterAt(), endAt, DataStatusType.VALID.KEY);
        List<InoutVo> vehicleList = new ArrayList<>();
        for (InOut inOut : record) {
            InoutVo inoutVo = new InoutVo();
            BeanUtils.copyProperties(inOut, inoutVo);
            // 计算停车时长
            if (inOut.getLeaveAt() != null) {
                inoutVo.setParkingTime(inOut.getLeaveAt().getTime() - inOut.getEnterAt().getTime());
            } else {
                inoutVo.setParkingTime(new Date().getTime() - inOut.getEnterAt().getTime());
            }
            vehicleList.add(inoutVo);
        }
        return vehicleList;
    }

    @Override
    public List<Gate> getAllCarGate(ObjectId communityId) {
        return gateRepository.findByCommunityIdAndDataStatus(communityId, DataStatusType.VALID.KEY);
    }

    @Override
    public Page<Gate> getAllCarGatePage(Gate gate, int page, int size) {
        Pageable pageable = new PageRequest(page - 1, size);
        org.springframework.data.domain.Page<Gate> resultPage =
                gateRepository.findByCommunityIdAndNameRegexIgnoreNullAndDataStatus(
                        gate.getCommunityId(), StringUtil.makeQueryStringAllRegExp(gate.getName()),
                        DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    @Override
    public List<InOut> getInoutRecordByCarGate(InoutRequest request, ObjectId communityId) {
        if (communityId == null) {
            log.info("查询车辆进出记录，社区ID不能为空，返回null");
            return Collections.emptyList();
        }
        Date endAt = null;
        if (request.getInOutDate() != null) {
            endAt = DateUtils.addDay(request.getInOutDate(), 1);
        }
        return inOutRepository.findByCommunityIdAndInGateAndEnterAtAfterAndEnterAtBeforeAndCarNoAndDataStatusOrCommunityIdAndOutGateAndLeaveAtAfterAndLeaveAtBeforeAndCarNoAndDataStatusAllIgnoreNull(
                communityId, request.getGateNO(), request.getInOutDate(), endAt, request.getCarNo(),
                DataStatusType.VALID.KEY,
                communityId, request.getGateNO(), request.getInOutDate(), endAt, request.getCarNo(),
                DataStatusType.VALID.KEY);
    }

    @Override
    public Page<InOut> getInoutRecordByCarGatePage(InoutRequest request, ObjectId communityId, int page, int size) {
        if (communityId == null) {
            log.info("查询车辆进出记录，社区ID不能为空，返回null");
            return new Page<>();
        }
        Date endAt = null;
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "enterAt", "leaveAt"));
        if (request.getInOutDate() != null) {
            endAt = DateUtils.addDay(request.getInOutDate(), 1);
        }
        org.springframework.data.domain.Page<InOut> resultPage = inOutRepository
            .findByCommunityIdAndInGateAndEnterAtAfterAndEnterAtBeforeAndCarNoAndDataStatusOrCommunityIdAndOutGateAndLeaveAtAfterAndLeaveAtBeforeAndCarNoAndDataStatusAllIgnoreNull(
                    communityId, request.getGateNO(), request.getInOutDate(), endAt, request.getCarNo(),
                    DataStatusType.VALID.KEY,
                    communityId, request.getGateNO(), request.getInOutDate(), endAt, request.getCarNo(),
                    DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }

    @Override
    public Page<CarInOutVO> getInoutRecordByCarNo(InoutRequest request, ObjectId communityId,
                                                  Integer page, Integer size) {
        Pageable pageable = new PageRequest(
                page - 1, size, new Sort(Sort.Direction.DESC, "enterAt", "leaveAt"));
        org.springframework.data.domain.Page<InOut> resultPage = inOutRepository
                .findByCommunityIdAndCarNoAndEnterAtAfterAndEnterAtBeforeAndLeaveAtAfterAndLeaveAtBeforeAndDataStatusAllIgnoreNull(
                        communityId, request.getCarNo(), request.getEnterAtAfter(), request.getEnterAtBefore(),
                        request.getLeaveAtAfter(), request.getLeaveAtBefore(), DataStatusType.VALID.KEY, pageable);
        Page<InOut> inOutPage = PageUtils.getPage(resultPage);
        Set<String> gateNos = new HashSet<>();
        List<InOut> pageRecords = inOutPage.getRecords();
        gateNos.addAll(pageRecords.stream().map(InOut::getInGate).collect(Collectors.toSet()));
        gateNos.addAll(pageRecords.stream().map(InOut::getOutGate).collect(Collectors.toSet()));

        List<Gate> gates = gateRepository.findByCommunityIdAndNoInAndDataStatus(
                communityId, gateNos, DataStatusType.VALID.KEY);

        Map<String, String> gateNoMap = gates.stream().collect(Collectors.toMap(Gate::getNo, Gate::getName));
        List<CarInOutVO> carInOutVOS = new ArrayList<>();
        for (InOut pageRecord : pageRecords) {
            CarInOutVO carInOutVO = new CarInOutVO();
            carInOutVOS.add(carInOutVO);
            BeanUtils.copyProperties(pageRecord, carInOutVO);
            if (StringUtil.isNotEmpty(pageRecord.getInGate())) {
                carInOutVO.setInGateName(gateNoMap.get(pageRecord.getInGate()));
            }
            if (StringUtil.isNotEmpty(pageRecord.getOutGate())) {
                carInOutVO.setOutGateName(gateNoMap.get(pageRecord.getOutGate()));
            }
        }
        return new Page<CarInOutVO>(inOutPage.getCurrentPage(), inOutPage.getTotal(), size, carInOutVOS);
    }

    /**
     * 统计已入闸的车辆数量
     *
     * @param communityId
     * @return
     */
    @Override
    public Long countParkingCar(ObjectId communityId) {
        return inOutRepository.countByCommunityIdAndEnterAtNotNullAndLeaveAtNullAndTypeAndDataStatus(
                communityId, InOutTypeEnum.IN.KEY, DataStatusType.VALID.KEY);
    }

    /**
     * 分页获取车辆出入记录
     *
     * @param communityId
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<InOut> listInoutRecords(ObjectId communityId, Integer page, Integer size) {
        Pageable pageable = new PageRequest(
                page - 1, size, new Sort(Sort.Direction.DESC, "enterAt", "leaveAt"));
        org.springframework.data.domain.Page<InOut> resultPage = inOutRepository.findByCommunityIdAndDataStatus(
                communityId, DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(resultPage);
    }
}
