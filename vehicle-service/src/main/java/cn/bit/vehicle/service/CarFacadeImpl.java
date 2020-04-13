package cn.bit.vehicle.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.IdentityStatus;
import cn.bit.facade.enums.VerifiedType;
import cn.bit.facade.model.vehicle.Apply;
import cn.bit.facade.model.vehicle.Identity;
import cn.bit.facade.service.vehicle.CarFacade;
import cn.bit.facade.vo.vehicle.CarIdentityRequest;
import cn.bit.facade.vo.vehicle.CarRequest;
import cn.bit.facade.vo.vehicle.IdentityQuery;
import cn.bit.facade.vo.vehicle.WritingCarRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.redis.RedisTemplateUtil;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.vehicle.dao.ApplyRepository;
import cn.bit.vehicle.dao.IdentityRepository;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static cn.bit.facade.exception.CommonBizException.DATA_INVALID;
import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_ID_NULL;
import static cn.bit.facade.exception.vehicle.CarBizException.*;

@Service("carFacade")
@Slf4j
public class CarFacadeImpl implements CarFacade
{
    @Autowired
    private ApplyRepository applyRepository;

    @Autowired
    private IdentityRepository identityRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${car.agent.url}")
    private String agentUrl;

    private final static String INSERT = "insert";

    private final static String DELETE = "delete";

    @Override
    public Apply findCar(ObjectId id) throws BizException {
        return applyRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public List<Apply> findCarByUserIdAndCommunityId(ObjectId userId, ObjectId communityId) throws BizException {
        return applyRepository.findByUserIdAndCommunityIdAndDataStatusOrderByCreateAtDesc(userId, communityId, DataStatusType.VALID.KEY);
    }

    @Override
    public Apply findCarByCarNo(String carNo) throws BizException{
        return applyRepository.findByCarNoAndDataStatus(carNo, DataStatusType.VALID.KEY);
    }

    @Override
    public Apply addCar(Apply entity) throws BizException{
        // 加上车牌防止因重复输入车牌导致业主无法再申请其他车牌的问题
        if (!RedisTemplateUtil.setIfAbsent(entity.getUserId().toString() + entity.getCarNo(), entity.getCarNo())) {
            throw CARNO_APPLIED;
        }

        // 1分钟自动失效
        RedisTemplateUtil.expire(entity.getUserId().toString() + entity.getCarNo(), 60);
        List<Apply> toGetList = applyRepository.findByCarNoAndCommunityIdAndAuditStatusAndDataStatusOrCarNoAndCommunityIdAndAuditStatusAndUserIdAndDataStatus(
                entity.getCarNo(), entity.getCommunityId(), VerifiedType.REVIEWED.getKEY(), DataStatusType.VALID.KEY,
                entity.getCarNo(), entity.getCommunityId(), VerifiedType.UNREVIEWED.getKEY(), entity.getUserId(), DataStatusType.VALID.KEY);
        if (toGetList != null && toGetList.size() > 0) {
            throw CARNO_APPLIED_OR_REAPPLIED;
        }

        entity.setAuditStatus(VerifiedType.UNREVIEWED.getKEY());
        entity.setCreateAt(new Date());
        entity.setDataStatus(DataStatusType.VALID.KEY);
        Apply apply = applyRepository.insert(entity);

        // 删除redis
        RedisTemplateUtil.del(entity.getUserId().toString() + entity.getCarNo());
        return apply;
    }

    @Override
    public boolean auditCar(ObjectId carId, String phoneNum, int auditStatus, ObjectId auditorId)
            throws BizException {
        Apply auditApply = this.findCar(carId);
        if (auditApply == null) {
            throw DATA_INVALID;
        }
        if (auditApply.getAuditStatus() == VerifiedType.REVIEWED.getKEY()) {
            throw CAR_AUDITED;
        }
        if (auditStatus == VerifiedType.REVIEWED.getKEY()) {
            Apply toGet = applyRepository.findByCarNoAndCommunityIdAndAuditStatusAndDataStatus(auditApply.getCarNo(),
                    auditApply.getCommunityId(), VerifiedType.REVIEWED.getKEY(), DataStatusType.VALID.KEY);
            if(toGet != null){
                log.info("车牌号:{}已经被申请了", auditApply.getCarNo());
                throw CARNO_APPLIED;
            }
        }
        Apply apply = new Apply();
        apply.setAuditStatus(auditStatus);
        apply.setUpdateAt(new Date());
        apply.setDataStatus(DataStatusType.VALID.KEY);
        apply.setModifierId(auditorId);
        auditApply = applyRepository.updateById(apply, carId);
        if (auditStatus == VerifiedType.REVIEWED.getKEY()) {
            /*CarIdentify carIdentify = carIdentifyMapper.selectByCarNo(auditApply.getCarNo());
            if(carIdentify != null){
                log.info("车牌号:{}已经被申请了", auditApply.getCarNo());
                return true;
            }*/
            CarIdentityRequest carIdentityRequest = new CarIdentityRequest();
            carIdentityRequest.setPhoneNumber(phoneNum);
            carIdentityRequest.setCarNo(auditApply.getCarNo());
            carIdentityRequest.setCName(auditApply.getUserName());
            carIdentityRequest.setBeginDate(new Date());
            carIdentityRequest.setReleaseDate(carIdentityRequest.getBeginDate());
            carIdentityRequest.setClosingDate(DateUtils.addYear(carIdentityRequest.getBeginDate(), 1));
            // 可用时段调整为全天候
//            long passTime = carIdentityRequest.getClosingDate().getTime() - carIdentityRequest.getBeginDate().getTime();
            carIdentityRequest.setPassTime("000000000000");
            carIdentityRequest.setGrpNo(0);

            WritingCarRequest writingCarRequest = new WritingCarRequest();
            writingCarRequest.setJson(JSON.toJSONString(carIdentityRequest));
            writingCarRequest.setOpt(INSERT);
            writingCarRequest.setCommunityId(auditApply.getCommunityId());
            try {
                RequestEntity<Object> requestEntity = RequestEntity
                        .post(URI.create(agentUrl))
                        .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                        .body(JSON.toJSON(writingCarRequest));
                restTemplate.exchange(requestEntity, JSONObject.class);
            } catch (Exception e) {
                log.error("writing apply flume error : ", e);
            }
            //carIdentifyMapper.insert(carIdentityRequest);
        }
        return true;
    }

    @Override
    public boolean unboundCar(ObjectId carId, ObjectId operatorId) throws BizException{
        if(carId == null){
            throw CAR_ID_NULL;
        }
        Apply apply = this.findCar(carId);
        if(apply == null || apply.getAuditStatus() != VerifiedType.REVIEWED.getKEY()){
            throw DATA_INVALID;
        }
        Apply toUpdate = new Apply();
        toUpdate.setAuditStatus(VerifiedType.CANCELLED.getKEY());
        toUpdate.setUpdateAt(new Date());
        toUpdate.setModifierId(operatorId);
        toUpdate = applyRepository.updateById(toUpdate, carId);
        if (toUpdate != null) {
            CarIdentityRequest carIdentityRequest = new CarIdentityRequest();
            carIdentityRequest.setCarNo(toUpdate.getCarNo());
            WritingCarRequest writingCarRequest = new WritingCarRequest();
            writingCarRequest.setOpt(DELETE);
            writingCarRequest.setCommunityId(apply.getCommunityId());
            writingCarRequest.setJson(JSON.toJSONString(carIdentityRequest));
            try {
                RequestEntity<Object> requestEntity = RequestEntity
                        .post(URI.create(agentUrl))
                        .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                        .body(JSON.toJSON(writingCarRequest));
                restTemplate.exchange(requestEntity, JSONObject.class);
            } catch (Exception e) {
                log.error("writing apply flume error : ", e);
            }
            //int otherDevice = carIdentifyMapper.unboundCarIdentify();
        }
        return true;
    }

    @Override
    public boolean isOwnerCar(ObjectId uid, ObjectId carId) throws BizException
    {
        Apply apply = this.findCar(carId);
        if(apply == null){
            throw DATA_INVALID;
        }
        return apply.getUserId().equals(uid);
    }

    @Override
    public Page<Apply> queryCarPage(ObjectId communityId, CarRequest carRequest, Integer page, Integer size) {
        if(communityId == null){
            throw COMMUNITY_ID_NULL;
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Apply> carPage =
                applyRepository.findByCarNoRegexIgnoreNullAndCommunityIdAndAuditStatusIgnoreNullAndDataStatus(
                StringUtil.makeQueryStringAllRegExp(carRequest.getCarNo()),
                communityId, carRequest.getAuditStatus(), DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(carPage);
    }

    /**
     * 查询用户的常用车辆列表
     *
     * @param userId
     * @param communityId
     * @param auditStatus
     * @return
     */
    @Override
    public List<Apply> findCarByUserIdAndCommunityIdAndAuditStatus(ObjectId userId, ObjectId communityId, int auditStatus) {
        return applyRepository
                .findCarByUserIdAndCommunityIdAndAuditStatusAndDataStatusOrderByCreateAtDesc(userId, communityId, auditStatus, DataStatusType.VALID.KEY);
    }

    /**
     * 物业申请车牌
     *
     * @param entity
     * @param phone
     * @return
     */
    @Override
    public Apply addCarByProperty(Apply entity, String phone) {
        List<Apply> toGetList = applyRepository.findByCarNoAndCommunityIdAndAuditStatusAndDataStatusOrCarNoAndCommunityIdAndAuditStatusAndUserIdAndDataStatus(
                entity.getCarNo(), entity.getCommunityId(), VerifiedType.REVIEWED.getKEY(), DataStatusType.VALID.KEY,
                entity.getCarNo(), entity.getCommunityId(), VerifiedType.UNREVIEWED.getKEY(), entity.getUserId(), DataStatusType.VALID.KEY);
        if (toGetList != null && toGetList.size() > 0) {
            throw CARNO_APPLIED_OR_REAPPLIED;
        }

        entity.setAuditStatus(VerifiedType.REVIEWED.getKEY());
        entity.setCreateAt(new Date());
        entity.setDataStatus(DataStatusType.VALID.KEY);
        entity.setUpdateAt(new Date());
        Apply apply = applyRepository.insert(entity);
        if(apply != null){
            CarIdentityRequest carIdentityRequest = new CarIdentityRequest();
            carIdentityRequest.setCarNo(apply.getCarNo());
            carIdentityRequest.setPhoneNumber(phone);
            carIdentityRequest.setCName(apply.getUserName());
            carIdentityRequest.setBeginDate(new Date());
            carIdentityRequest.setReleaseDate(carIdentityRequest.getBeginDate());
            carIdentityRequest.setClosingDate(DateUtils.addYear(carIdentityRequest.getBeginDate(), 1));
            // 可用时段调整为全天候
//            long passTime = carIdentityRequest.getClosingDate().getTime() - carIdentityRequest.getBeginDate().getTime();
            carIdentityRequest.setPassTime("000000000000");
            carIdentityRequest.setGrpNo(0);

            WritingCarRequest writingCarRequest = new WritingCarRequest();
            writingCarRequest.setJson(JSON.toJSONString(carIdentityRequest));
            writingCarRequest.setOpt(INSERT);
            writingCarRequest.setCommunityId(apply.getCommunityId());
            try {
                log.info("录入车禁请求参数 : " + writingCarRequest);
                System.out.println(JSON.toJSONString(writingCarRequest));
                RequestEntity<Object> requestEntity = RequestEntity
                        .post(URI.create(agentUrl))
                        .header("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString())
                        .body(JSON.toJSON(writingCarRequest));
                restTemplate.exchange(requestEntity, JSONObject.class);
            } catch (Exception e) {
                log.error("writing apply flume error : ", e);
            }
            //carIdentifyMapper.insert(carIdentityRequest);
        }
        return apply;
    }

    @Override
    public Page<Identity> queryIdentityPage(ObjectId communityId, IdentityQuery identityQuery, Integer page,
                                            Integer size) {
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "beginAt"));
        org.springframework.data.domain.Page<Identity> identityPage = null;
        if (identityQuery.getStatus() == null) {
            identityPage = identityRepository.CommunityIdAndCarTypeAndTypeAndCarNoAndDataStatusOrCommunityIdAndCarTypeAndTypeAndOwnerAndDataStatusAllIgnoreNull(
                    communityId, identityQuery.getChargeType(), identityQuery.getType(), identityQuery.getName(), DataStatusType.VALID.KEY,
                    communityId, identityQuery.getChargeType(), identityQuery.getType(), identityQuery.getName(), DataStatusType.VALID.KEY, pageable);
        }
        if (Objects.equals(IdentityStatus.VALID.KEY, identityQuery.getStatus())) {
            identityPage = identityRepository.findByCommunityIdAndCarTypeAndTypeAndEndAtAfterAndCarNoAndDataStatusOrCommunityIdAndCarTypeAndTypeAndEndAtAfterAndOwnerAndDataStatusAllIgnoreNull(
                    communityId, identityQuery.getChargeType(), identityQuery.getType(), new Date(), identityQuery.getName(), DataStatusType.VALID.KEY,
                    communityId, identityQuery.getChargeType(), identityQuery.getType(), new Date(), identityQuery.getName(), DataStatusType.VALID.KEY, pageable);
        } else if (Objects.equals(IdentityStatus.INVALID.KEY, identityQuery.getStatus())){
            identityPage = identityRepository.findByCommunityIdAndCarTypeAndTypeAndEndAtBeforeAndCarNoAndDataStatusOrCommunityIdAndCarTypeAndTypeAndEndAtBeforeAndOwnerAndDataStatusAllIgnoreNull(
                    communityId, identityQuery.getChargeType(), identityQuery.getType(), new Date(), identityQuery.getName(), DataStatusType.VALID.KEY,
                    communityId, identityQuery.getChargeType(), identityQuery.getType(), new Date(), identityQuery.getName(), DataStatusType.VALID.KEY, pageable);
        }

        return PageUtils.getPage(identityPage);
    }

    @Override
    public Identity removeCarIdentity(ObjectId identityId) {
        Identity identity = new Identity();
        identity.setId(identityId);
        identity.setDataStatus(DataStatusType.INVALID.KEY);
        return identityRepository.updateByIdAndDataStatus(identity, identityId, DataStatusType.VALID.KEY);
    }
}