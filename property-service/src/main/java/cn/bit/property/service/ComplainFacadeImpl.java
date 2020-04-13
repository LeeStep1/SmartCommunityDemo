package cn.bit.property.service;

import cn.bit.facade.enums.ComplainStatusEnum;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.UserStatus;
import cn.bit.facade.model.property.Complain;
import cn.bit.facade.service.property.ComplainFacade;
import cn.bit.facade.vo.property.ComplainRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.property.dao.ComplainRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

import static cn.bit.facade.exception.CommonBizException.AUTHENCATION_FAILD;
import static cn.bit.facade.exception.CommonBizException.DATA_INVALID;
import static cn.bit.facade.exception.property.PropertyBizException.*;

@Service("complainFacade")
@Slf4j
public class ComplainFacadeImpl implements ComplainFacade {

    @Autowired
    private ComplainRepository complainRepository;

    @Autowired
    private EsTemplate esTemplate;

    private static final String INDEX_NAME = "cm_accident";

    private static final String TYPE_NAME = "complain";

    /**
     * 新增用户投诉
     * @param entity
     * @return
     */
    @Override
    public Complain addComplain(Complain entity) {
        if (entity.getStatus() == null) {
            entity.setStatus(ComplainStatusEnum.TO_ACCEPT.value);
        }

        if (entity.getAnonymity() == null) {
            entity.setAnonymity(Boolean.FALSE);
        }
        entity.setHidden(Boolean.FALSE);
        entity.setInvalid(Boolean.FALSE);
        Date now = new Date();
        entity.setCreateAt(now);
        entity.setUpdateAt(now);
        entity.setDataStatus(DataStatusType.VALID.KEY);
        entity = complainRepository.insert(entity);
        upsertComplain2Es(entity);
        return complainAnonymity(entity);
    }

    /**
     * 删除用户投诉
     * @param id
     * @return
     */
    @Override
    public void deleteComplainById(ObjectId id) {
        Complain toDelete = new Complain();
        toDelete.setDataStatus(DataStatusType.INVALID.KEY);
        toDelete.setUpdateAt(new Date());
        complainRepository.updateById(toDelete, id);
        removeComplainFromEs(Collections.singleton(id));
    }

    /**
     * 获取用户投诉详细
     * @param id
     * @return
     */
    @Override
    public Complain getComplainById(ObjectId id) {
        return complainAnonymity(complainRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY));
    }

    /**
     * 分页获取用户投诉
     * @param request
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Complain> getComplainPage(ComplainRequest request, int page, int size) {
        Date startAt = request.getStartAt();
        if (startAt != null) {
            startAt = DateUtils.getStartTime(startAt);
        }
        Date endAt = request.getEndAt();
        if(endAt != null){
            endAt = DateUtils.getEndTime(endAt);
        }
        if (request.getStatus() != null && request.getStatus().isEmpty()) {
            request.setStatus(null);
        }
        // 非匿名
        Boolean nonAnonymity = null;
        if (StringUtil.isNotBlank(request.getUserName())) {
            nonAnonymity = true;
        }
        Pageable pageable = new PageRequest(page - 1, size, new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<Complain> complainPage =
                complainRepository.findByCommunityIdAndUserIdAndUserNameRegexAndMessageSourceAndStatusInAndHiddenNotAndInvalidAndAnonymityNotAndCreateAtGreaterThanEqualAndCreateAtLessThanEqualAndDataStatusAllIgnoreNull(
                        request.getCommunityId(), request.getUserId(),
                        StringUtil.makeQueryStringAllRegExp(request.getUserName()), request.getMessageSource(),
                        request.getStatus(), request.getHidden(), request.getInvalid(), nonAnonymity,
                        startAt, endAt, DataStatusType.VALID.KEY, pageable);
        if (complainPage.getTotalElements() > 0) {
            complainPage.getContent().forEach(complain -> complainAnonymity(complain));
        }
        return PageUtils.getPage(complainPage);
    }

    /**
     * 获取指定社区及指定size获取住户投诉报事列表
     *
     * @param communityId
     * @param size
     * @return
     */
    @Override
    public List<Complain> listComplainsForScreen(ObjectId communityId, Integer size) {
        Pageable pageable = new PageRequest(0, size);
        List<Complain> complains = complainRepository.findByCommunityIdAndMessageSourceAndDataStatusOrderByCreateAtDesc(
                communityId, UserStatus.RESIDENT.key, DataStatusType.VALID.KEY, pageable);
        if (complains.isEmpty()) {
            return complains;
        }
        for (Complain complain : complains) {
            // 用户名隐藏第2位
            if (StringUtil.isNotBlank(complain.getUserName())) {
                complain.setUserName(new StringBuffer(complain.getUserName()).replace(1, 2, "*").toString());
            }
            // 手机号隐藏中间4位，从第4位开始
            if (StringUtil.isNotBlank(complain.getPhone()) && complain.getPhone().length() > 3) {
                complain.setPhone(new StringBuffer(complain.getPhone()).replace(3, 7, "****").toString());
            }
        }
        return complains;
    }

    /**
     * 处理投诉
     *
     * @param complain
     * @return
     */
    @Override
    public Complain processComplain(Complain complain) {
        if (complain.getId() == null) {
            throw COMPLAIN_ID_NULL;
        }
        if (complain.getStatus() == null || StringUtil.isBlank(complain.getResult())) {
            throw COMPLAIN_RESULT_NULL;
        }
        if (!Arrays.asList(ComplainStatusEnum.REJECTED.value, ComplainStatusEnum.PROCESSED.value)
                .contains(complain.getStatus())) {
            throw COMPLAIN_STATUS_INVALID;
        }
        Complain toGet = complainRepository.findByIdAndDataStatus(complain.getId(), DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw DATA_INVALID;
        }
        if (!Arrays.asList(ComplainStatusEnum.TO_ACCEPT.value, ComplainStatusEnum.PENDING.value)
                .contains(toGet.getStatus())) {
            throw COMPLAIN_STATUS_CHANGED;
        }
        Complain process = new Complain();
        process.setStatus(complain.getStatus());
        process.setResult(complain.getResult());
        process.setProcessAt(new Date());
        process.setInvalid(complain.getInvalid());
        process.setUpdateAt(process.getProcessAt());
        process = complainRepository.updateById(process, toGet.getId());
        upsertComplain2Es(process);
        return complainAnonymity(process);
    }

    /**
     * 评价投诉处理结果
     *
     * @param complain
     * @return
     */
    @Override
    public Complain evaluateComplain(Complain complain) {
        if (complain.getId() == null) {
            throw COMPLAIN_ID_NULL;
        }
        if (complain.getEvaluation() == null) {
            // 默认5星好评
            complain.setEvaluation(5);
        }
        Complain toGet = complainRepository.findByIdAndDataStatus(complain.getId(), DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw DATA_INVALID;
        }
        if (ComplainStatusEnum.PROCESSED.value != toGet.getStatus()) {
            throw COMPLAIN_STATUS_CHANGED;
        }
        Complain evaluate = new Complain();
        evaluate.setEvaluation(complain.getEvaluation());
        evaluate.setEvaluationContent(complain.getEvaluationContent());
        evaluate.setStatus(ComplainStatusEnum.EVALUATED.value);
        evaluate.setUpdateAt(new Date());
        evaluate = complainRepository.updateById(evaluate, toGet.getId());
        upsertComplain2Es(evaluate);
        return complainAnonymity(evaluate);
    }

    /**
     * 隐藏工单
     *
     * @param id
     * @param uid
     * @return
     */
    @Override
    public Complain hiddenComplainById(ObjectId id, ObjectId uid) {
        Complain toGet = complainRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
        if (toGet == null) {
            throw DATA_INVALID;
        }
        // 只能隐藏自己提交的投诉工单
        if (!toGet.getUserId().equals(uid)) {
            throw AUTHENCATION_FAILD;
        }
        // 已经是隐藏状态，直接返回
        if (toGet.getHidden() != null && toGet.getHidden()) {
            return toGet;
        }
        if (!Arrays.asList(
                ComplainStatusEnum.REJECTED.value,
                ComplainStatusEnum.PROCESSED.value,
                ComplainStatusEnum.EVALUATED.value).contains(toGet.getStatus())) {
            throw COMPLAIN_NOT_FINISHED;
        }
        Complain toHidden = new Complain();
        toHidden.setHidden(Boolean.TRUE);
        toHidden.setUpdateAt(new Date());
        return complainAnonymity(complainRepository.updateById(toHidden, id));
    }

    /**
     * 需要处理匿名的返回实体
     * @param complain
     * @return
     */
    private Complain complainAnonymity(Complain complain) {
        if (complain != null && complain.getAnonymity() != null && complain.getAnonymity()) {
            complain.setUserId(null);
            complain.setUserName(null);
            complain.setPhone(null);
            complain.setRoomInfo(null);
        }
        return complain;
    }

    /**
     * 更新es
     * @param complain
     */
    private void upsertComplain2Es(Complain complain) {
        cn.bit.facade.data.property.Complain toUpsert = new cn.bit.facade.data.property.Complain();
        toUpsert.setCommunityId(complain.getCommunityId());
        toUpsert.setUserId(complain.getUserId());
        toUpsert.setSource(complain.getMessageSource());
        toUpsert.setStatus(complain.getStatus());
        toUpsert.setScore(complain.getEvaluation());
        toUpsert.setAnonymity(complain.getAnonymity() == null ? false : complain.getAnonymity());
        toUpsert.setInvalid(complain.getInvalid() == null ? false : complain.getInvalid());
        toUpsert.setCreateAt(complain.getCreateAt());
        esTemplate.upsertAsync(INDEX_NAME, TYPE_NAME, complain.getId().toString(), toUpsert);
    }

    /**
     * 根据id，移除es记录
     * @param complainIds
     */
    private void removeComplainFromEs(Collection<ObjectId> complainIds) {
        complainIds.forEach(id -> esTemplate.deleteAsync(INDEX_NAME, TYPE_NAME, id.toString()));
    }
}
