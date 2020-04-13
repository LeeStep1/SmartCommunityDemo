package cn.bit.community.service;

import cn.bit.community.dao.ParameterRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.ParamConfigType;
import cn.bit.facade.enums.ParamKeyType;
import cn.bit.facade.model.community.Parameter;
import cn.bit.facade.service.community.ParameterFacade;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.*;

import static cn.bit.facade.exception.CommonBizException.DATA_INVALID;
import static cn.bit.facade.exception.community.CommunityBizException.*;
import static cn.bit.facade.exception.user.UserBizException.DATA_ALREADY_EXIST;
import static cn.bit.facade.exception.user.UserBizException.PARAM_INVALID;

@Component("parameterFacade")
@Slf4j
public class ParameterFacadeImpl implements ParameterFacade {

    @Autowired
    private ParameterRepository parameterRepository;
    /**
     * 新增配置参数
     *
     * @param parameter
     * @return
     */
    @Override
    public Parameter addParameter(Parameter parameter) {
        if(!ParamKeyType.checkParamKey(parameter.getKey())){
            throw PARAM_INVALID;
        }
        Parameter toCheck = new Parameter();
        toCheck.setCommunityId(parameter.getCommunityId());
        toCheck.setKey(parameter.getKey());
        toCheck.setDataStatus(DataStatusType.VALID.KEY);
        toCheck = parameterRepository.findOne(toCheck);
        if(toCheck != null){
            throw DATA_ALREADY_EXIST;
        }
        parameter.setCreateAt(new Date());
        parameter.setDataStatus(DataStatusType.VALID.KEY);
        return parameterRepository.insert(parameter);
    }

    /**
     * 修改
     * @param parameter
     * @return
     */
    @Override
    public Parameter updateParameter(Parameter parameter) {
        if(parameter.getId() == null){
            throw PARAM_ID_NULL;
        }
        if(StringUtil.isBlank(parameter.getValue())
                && StringUtil.isBlank(parameter.getName())
                && null == parameter.getIsRequired()
                && null == parameter.getIsDisplay()
                && null == parameter.getDataType()
                && StringUtil.isBlank(parameter.getInputRule())
                ){
            throw NOT_NEED_TO_UPDATED;
        }
        Parameter toCheck = parameterRepository.findById(parameter.getId());
        if(toCheck == null || toCheck.getDataStatus() == DataStatusType.INVALID.KEY){
            throw DATA_INVALID;
        }
        String nextEffectDateStr = "";
        if(ParamKeyType.BILLCREATEDAY.name().equals(toCheck.getKey())
                || ParamKeyType.CHARGINGSTANDARDS.name().equals(toCheck.getKey())){
            // 设置生效日需要判断是否满足修改条件
            int value = Integer.parseInt(parameter.getValue());
            if(value < 1){
                value = 1;
            }else if(value > 28){
                value = 28;
            }
            List<Parameter> list = parameterRepository.findByTypeAndCommunityIdAndDataStatus(
                    ParamConfigType.BILL.getKey(), toCheck.getCommunityId(), DataStatusType.VALID.KEY);
            if(list == null || list.size() == 0){
                log.info("该社区没有配置相关的账单参数");
                throw BILL_PARAM_NOT_EXIST;
            }
            Map<String, String> keyMap = new HashMap<>();
            for(Parameter param : list){
                keyMap.put(param.getKey(), param.getValue());
            }
            String lastDateStr = keyMap.get(ParamKeyType.LASTEFFECTDATE.name());
            if(!StringUtil.isNotNull(lastDateStr)){
                log.info("该社区没有配置上期账单生成日");
                throw BILL_PARAM_NOT_EXIST;
            }
            Date lastDate = DateUtils.getDateByStr(lastDateStr);
            int months = 1;
            int day = 1;
            if(ParamKeyType.BILLCREATEDAY.name().equals(toCheck.getKey())){
                // 设置账单生成日
                String chargingStandards = keyMap.get(ParamKeyType.CHARGINGSTANDARDS.name());
                if(StringUtil.isNotNull(chargingStandards)){
                    months = Integer.parseInt(chargingStandards);
                }
                day = value;
            }else if(ParamKeyType.CHARGINGSTANDARDS.name().equals(toCheck.getKey())){
                // 设置收费标准
                months = value;
                String billCreateDay = keyMap.get(ParamKeyType.BILLCREATEDAY.name());
                if(StringUtil.isNotNull(billCreateDay)){
                    day = Integer.parseInt(billCreateDay);
                }
            }
            // 计算新的下一个账单生成日期
            nextEffectDateStr = DateUtils.getNextDateByMonthsWithDay(lastDate, months, day);
            if(!DateUtils.getDateByStr(nextEffectDateStr).after(new Date())){
                throw BILL_PARAM_INVALID;
            }
        }

        // 只更新部分字段,需要 new 新对象
        Parameter toUpdate = new Parameter();
        toUpdate.setValue(parameter.getValue());
        toUpdate.setName(parameter.getName());
        toUpdate.setIsRequired(parameter.getIsRequired());
        toUpdate.setIsDisplay(parameter.getIsDisplay());
        toUpdate.setDataType(parameter.getDataType());
        toUpdate.setInputRule(parameter.getInputRule());
        toUpdate.setUpdateAt(new Date());
        toUpdate = parameterRepository.updateById(toUpdate, parameter.getId());
        // 设置账单生成日需要更新下一期生成日
        if(toUpdate != null && ParamKeyType.BILLCREATEDAY.name().equals(toUpdate.getKey())){
            this.updateNextEffectDate(toUpdate.getCommunityId(), nextEffectDateStr);
        }
        return toUpdate;
    }

    // 只支持按月收费
    private void updateNextEffectDate(ObjectId communityId, String nextEffectDate) {
        Parameter toAdd = new Parameter();
        toAdd.setCommunityId(communityId);
        toAdd.setType(ParamConfigType.BILL.getKey());
        toAdd.setKey(ParamKeyType.NEXTEFFECTDATE.name());
        toAdd.setName(ParamKeyType.NEXTEFFECTDATE.getValue());
        toAdd.setValue(nextEffectDate);
        toAdd.setCreateAt(new Date());
        toAdd.setDataStatus(DataStatusType.VALID.KEY);
        toAdd = parameterRepository.upsertWithSetOnInsertCommunityIdAndTypeAndKeyAndNameAndCreateAtAndDataStatusByCommunityIdAndTypeAndKeyAndDataStatus(
                toAdd, communityId, ParamConfigType.BILL.getKey(), ParamKeyType.NEXTEFFECTDATE.name(), DataStatusType.VALID.KEY);
    }

    /**
     * 删除
     *
     * @param id
     * @param operatorId
     * @return
     */
    @Override
    public boolean deleteParameter(ObjectId id, ObjectId operatorId) {
        Parameter toCheck = parameterRepository.findById(id);
        if(toCheck == null || toCheck.getDataStatus() == DataStatusType.INVALID.KEY){
            throw DATA_INVALID;
        }
        Parameter parameter = new Parameter();
        parameter.setModifierId(operatorId);
        parameter.setUpdateAt(new Date());
        parameter.setDataStatus(DataStatusType.INVALID.KEY);
        return parameterRepository.updateById(parameter, id) != null;
    }

     /**
     * 分页查询
     *
     * @param parameter
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Parameter> queryPage(Parameter parameter, Integer page, Integer size) {
        parameter.setDataStatus(DataStatusType.VALID.KEY);
        return parameterRepository.findPage(parameter, page, size, null);
    }

    /**
     * 根据type,type,value查询配置信息
     * @param type
     * @param key
     * @param value
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Parameter> findByTypeAndKeyAndValue(Integer type, String key, String value, int page, int size) {
        Pageable pageable = new PageRequest(page - 1, size);
        org.springframework.data.domain.Page<Parameter> parameterPage =
                parameterRepository.findByTypeAndKeyAndValueAndDataStatus(
                        type, key, value, DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(parameterPage);
    }

    /**
     * 根据社区，type,key查询配置信息
     * @param type
     * @param key
     * @param communityId
     * @return
     */
    @Override
    public Parameter findByTypeAndKeyAndCommunityId(Integer type, String key, ObjectId communityId) {
        return parameterRepository
                .findByTypeAndKeyAndCommunityIdAndDataStatus(type, key, communityId, DataStatusType.VALID.KEY);
    }

    /**
     * 根据社区及类型获取配置参数列表
     *
     * @param type
     * @param communityId
     * @return
     */
    @Override
    public List<Parameter> findByTypeAndCommunityId(Integer type, ObjectId communityId) {
        return parameterRepository.findByTypeAndCommunityIdAndDataStatus(type, communityId, DataStatusType.VALID.KEY);
    }

    /**
     * 根据类型及key查询配置参数
     *
     * @param type
     * @param key
     * @return
     */
    @Override
    public Parameter findOneByTypeAndKey(Integer type, String key) {
        return parameterRepository.findByTypeAndKeyAndDataStatus(type, key, DataStatusType.VALID.KEY);
    }

    /**
     * 根据社区及配置类型分页获取配置参数列表
     *
     * @param communityId
     * @param type
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Parameter> queryPageByCommunityIdAndType(ObjectId communityId,
                                                         Integer type, Integer page, Integer size) {
        if(communityId == null){
            throw COMMUNITY_ID_NULL;
        }
        if(type == null){
            throw PARAM_CONFIG_TYPE_NULL;
        }
        Pageable pageable = new PageRequest(page - 1, size);
        org.springframework.data.domain.Page<Parameter> pageList = parameterRepository
                .findPageByCommunityIdAndTypeAndDataStatus(communityId, type, DataStatusType.VALID.KEY, pageable);
        return PageUtils.getPage(pageList);
    }

    /**
     * 根据社区及type获取用户认证的配置项
     *
     * @param communityId
     * @param type
     * @return
     */
    @Override
    public List<Parameter> queryByCommunityIdAndTypeForAuth(ObjectId communityId, Integer type) {
        if(communityId == null){
            throw COMMUNITY_ID_NULL;
        }
        if(type == null){
            throw PARAM_CONFIG_TYPE_NULL;
        }
        List<Parameter> parameterList = parameterRepository.findByTypeAndCommunityIdAndDataStatusAndKeyNotOrderByOrderNum(
                type, communityId, DataStatusType.VALID.KEY, ParamKeyType.LEVEL2AUDIT.name());
        parameterList.forEach(item -> item.setKey(ParamKeyType.valueOf(item.getKey()).getFieldName()));
        return parameterList;
    }

    @Override
    public Parameter updateWithSetValueAndUpdateAtById(Parameter toUpdate, ObjectId id) {
        return parameterRepository.updateWithSetValueAndUpdateAtById(toUpdate, id);
    }

    @Override
    public List<Parameter> findByCommunityIdInAndTypeAndKeyIn(Set<ObjectId> communityIds,
                                                              Integer type, List<String> keys) {
        return parameterRepository
                .findByCommunityIdInAndTypeAndKeyInAndDataStatus(communityIds, type, keys, DataStatusType.VALID.KEY);
    }

    /**
     * 更新对应key的value
     *
     * @param toUpdate
     * @param communityId
     * @param type
     * @param key
     * @return
     */
    @Override
    public Parameter updateWithSetValueAndUpdateAtByCommunityIdAndTypeAndKey(Parameter toUpdate, ObjectId communityId,
                                                                             Integer type, String key) {
        return parameterRepository.updateByCommunityIdAndTypeAndKeyAndDataStatus(
                toUpdate, communityId, type, key, DataStatusType.VALID.KEY);
    }
}
