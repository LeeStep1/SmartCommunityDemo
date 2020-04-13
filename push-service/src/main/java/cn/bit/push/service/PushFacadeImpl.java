package cn.bit.push.service;

import cn.bit.common.facade.enums.DataStatusEnum;
import cn.bit.common.facade.system.dto.RoleBriefDTO;
import cn.bit.common.facade.system.service.SystemFacade;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.push.PushPointEnum;
import cn.bit.facade.model.push.PushAccount;
import cn.bit.facade.model.push.PushConfig;
import cn.bit.facade.model.push.PushPoint;
import cn.bit.facade.model.push.PushTemplate;
import cn.bit.facade.service.push.PushFacade;
import cn.bit.facade.vo.push.PushConfigPageQuery;
import cn.bit.facade.vo.push.PushConfigVO;
import cn.bit.facade.vo.push.PushResult;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import cn.bit.framework.utils.page.PageUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.push.dao.PushAccountRepository;
import cn.bit.push.dao.PushConfigRepository;
import cn.bit.push.dao.PushPointRepository;
import cn.bit.push.dao.PushTemplateRepository;
import cn.jiguang.common.resp.APIConnectionException;
import cn.jiguang.common.resp.APIRequestException;
import cn.jpush.api.JPushClient;
import com.alibaba.fastjson.JSON;
import org.apache.commons.collections.CollectionUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static cn.bit.facade.exception.push.PushBizException.*;

@Service("pushFacade")
public class PushFacadeImpl implements PushFacade {

    private static final Map<String, JPushClient> JPUSH_CLIENT_MAP = new HashMap<>();

    @Autowired
    private PushAccountRepository pushAccountRepository;

    @Autowired
    private PushTemplateRepository pushTemplateRepository;

    @Autowired
    private PushPointRepository pushPointRepository;

    @Autowired
    private PushConfigRepository pushConfigRepository;

    @Resource
    private SystemFacade systemFacade;

    private static final Pattern PERCENT_SPLIT_PATTERN = Pattern.compile("%[\\{%]([^\\}%]+)(\\}|%%)");

    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\t\\n\\r]");

    private static final Map<String, String> CONTROL_CHARS_MAP = new HashMap<>();

    private static final ExpressionParser SPEL_PARSER = new SpelExpressionParser();

    static {
        CONTROL_CHARS_MAP.put("\t", "\\t");
        CONTROL_CHARS_MAP.put("\n", "\\n");
        CONTROL_CHARS_MAP.put("\r", "\\r");
    }


    @Override
    public PushAccount addPushAccount(PushAccount pushAccount) {
        pushAccount.setCreateAt(new Date());
        pushAccount.setUpdateAt(pushAccount.getCreateAt());
        pushAccount.setDataStatus(DataStatusType.VALID.KEY);
        return pushAccountRepository.insert(pushAccount);
    }

    @Override
    public PushAccount deletePushAccount(ObjectId id) {
        PushAccount pushAccount = new PushAccount();
        pushAccount.setId(id);
        pushAccount.setUpdateAt(new Date());
        pushAccount.setDataStatus(DataStatusType.INVALID.KEY);
        return pushAccountRepository.updateOne(pushAccount);
    }

    @Override
    public PushAccount getPushAccount(ObjectId id) {
        return pushAccountRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public PushAccount updatePushAccount(PushAccount pushAccount) {
        pushAccount.setUpdateAt(new Date());
        return pushAccountRepository.updateOne(pushAccount);
    }

    @Override
    public Page<PushAccount> getPushAccounts(int page, int size) {
        return pushAccountRepository.findPage((PushAccount) null, page, size, null);
    }

    @Override
    public PushTemplate addPushTemplate(PushTemplate pushTemplate) {
        pushTemplate.setCreateAt(new Date());
        pushTemplate.setUpdateAt(pushTemplate.getCreateAt());
        pushTemplate.setDataStatus(DataStatusType.VALID.KEY);
        return pushTemplateRepository.insert(pushTemplate);
    }

    @Override
    public PushTemplate deletePushTemplateById(ObjectId id) {
        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setUpdateAt(new Date());
        pushTemplate.setDataStatus(DataStatusType.INVALID.KEY);
        return pushTemplateRepository.updateOne(pushTemplate);
    }

    @Override
    public PushTemplate getPushTemplateById(ObjectId id) {
        return pushTemplateRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public PushTemplate updatePushTemplate(PushTemplate pushTemplate) {
        pushTemplate.setUpdateAt(new Date());
        return pushTemplateRepository.updateOne(pushTemplate);
    }

    @Override
    public PushPoint addPushPoint(PushPoint pushPoint) {
        if (pushPoint.getBeforeInvocation() == null) {
            pushPoint.setBeforeInvocation(false);
        }

        pushPoint.setCreateAt(new Date());
        pushPoint.setUpdateAt(pushPoint.getCreateAt());
        pushPoint.setDataStatus(DataStatusType.VALID.KEY);
        return pushPointRepository.insert(pushPoint);
    }

    @Override
    public PushPoint deletePushPointById(ObjectId id) {
        PushPoint pushPoint = new PushPoint();
        pushPoint.setUpdateAt(new Date());
        pushPoint.setDataStatus(DataStatusType.INVALID.KEY);
        return pushPointRepository.updateOne(pushPoint);
    }

    @Override
    public PushPoint getPushPointById(ObjectId id) {
        return pushPointRepository.findByIdAndDataStatus(id, DataStatusType.VALID.KEY);
    }

    @Override
    public List<PushPoint> getPushPointsBySignature(String signature) {
        return pushPointRepository.findBySignatureAndEnableIsTrueAndDataStatus(signature,
                DataStatusType.VALID.KEY);
    }

    @Override
    public Page<PushPoint> getPushPointsByScopeIn(Object[] scopes, int page, int size) {
        org.springframework.data.domain.Page<PushPoint> _page =
                pushPointRepository.findByScopesInAndDataStatus(scopes, DataStatusType.VALID.KEY,
                        new PageRequest(page - 1, size));
        return PageUtils.getPage(_page);
    }

    @Override
    public PushPoint updatePushPoint(PushPoint pushPoint) {
        pushPoint.setUpdateAt(new Date());
        return pushPointRepository.updateOne(pushPoint);
    }

    @Override
    public PushResult sendPush(ObjectId accountId, ObjectId templateId, Map data) throws BizException {
        PushTemplate pushTemplate = pushTemplateRepository.findById(templateId);
        try {
            cn.jpush.api.push.PushResult result = getJPushClient(accountId).sendPush(transControlChars(
                    replaceByReplacements(pushTemplate.getContent(), data)));
            return new PushResult(String.valueOf(result.msg_id), result.sendno);
        } catch (IllegalArgumentException | APIConnectionException e) {
            throw new BizException(-1, e.getMessage());
        } catch (APIRequestException e) {
            throw new BizException(-1, e.getErrorMessage());
        }
    }

    @Override
    public void setDeviceTagAlias(ObjectId accountId, String registrationId, String alias, Set<String> tagsToAdd,
                                  Set<String> tagsToRemove) throws BizException {
        try {
            getJPushClient(accountId).updateDeviceTagAlias(registrationId, alias, tagsToAdd, tagsToRemove);
        } catch (APIConnectionException e) {
            throw new BizException(-1, e.getMessage());
        } catch (APIRequestException e) {
            throw new BizException(-1, e.getErrorMessage());
        }
    }

    @Override
    public Page<PushConfig> listPushConfigs(PushConfigPageQuery query) {
        Pageable pageable = new PageRequest(
                query.getPage() - 1, query.getSize(), new Sort(Sort.Direction.DESC, "createAt"));
        org.springframework.data.domain.Page<PushConfig> configs =
                pushConfigRepository.findByCompanyIdAndPointNameRegexIgnoreNullAndDataStatus(
                        query.getCompanyId(), query.getName(), DataStatusEnum.VALID.value(), pageable);
        return PageUtils.getPage(configs);
    }

    @Override
    public PushConfig addPushConfig(PushConfigVO pushConfigVO) {
        PushConfig config = new PushConfig();
        BeanUtils.copyProperties(pushConfigVO, config);
        try {
            PushPointEnum pushPointEnum = PushPointEnum.valueOf(pushConfigVO.getPointId());
            config.setPointName(pushPointEnum.value());
        } catch (IllegalArgumentException e) {
            throw PUSH_POINT_INVALID;
        }
        PushConfig toCheck = pushConfigRepository.findByCompanyIdAndPointIdAndDataStatus(
                config.getCompanyId(), config.getPointId(), DataStatusEnum.VALID.value());
        if (toCheck != null) {
            throw PUSH_CONFIG_EXISTS;
        }
        config.setDataStatus(DataStatusEnum.VALID.value());
        config.setCreateAt(new Date());
        config.setUpdateAt(config.getCreateAt());
        return pushConfigRepository.insert(config);
    }

    @Override
    public PushConfig modifyPushConfig(PushConfigVO pushConfigVO) {
        PushConfig toGet = pushConfigRepository.findByIdAndDataStatus(pushConfigVO.getId(), DataStatusEnum.VALID.value());
        if (toGet == null) {
            throw PUSH_CONFIG_NOT_EXISTS;
        }
        PushConfig toUpdate = new PushConfig();
        toUpdate.setTmplId(pushConfigVO.getTmplId());
        toUpdate.setTargets(pushConfigVO.getTargets());
        toUpdate.setUpdateAt(new Date());
        if (StringUtil.isNotBlank(pushConfigVO.getPointId()) && !pushConfigVO.getPointId().equals(toGet.getPointId())) {
            try {
                PushPointEnum pushPointEnum = PushPointEnum.valueOf(pushConfigVO.getPointId());
                toUpdate.setPointId(pushConfigVO.getPointId());
                toUpdate.setPointName(pushPointEnum.value());
            } catch (IllegalArgumentException e) {
                throw PUSH_POINT_INVALID;
            }
        }
        PushConfig toCheck = pushConfigRepository.findByCompanyIdAndPointIdAndDataStatus(
                toGet.getCompanyId(), toUpdate.getPointId(), DataStatusEnum.VALID.value());
        if (toCheck != null) {
            throw PUSH_CONFIG_EXISTS;
        }
        return pushConfigRepository.updateByIdAndDataStatus(toUpdate, pushConfigVO.getId(), DataStatusEnum.VALID.value());
    }

    @Override
    public PushConfig findPushConfigById(ObjectId id) {
        return pushConfigRepository.findByIdAndDataStatus(id, DataStatusEnum.VALID.value());
    }

    @Override
    public PushConfig findPushConfigByCompanyIdAndPointId(ObjectId companyId, String pointId) {

        PushConfig pushConfig =
                pushConfigRepository.findByCompanyIdAndPointIdAndDataStatus(companyId, pointId, DataStatusEnum.VALID.value());

        if (pushConfig != null && PushPointEnum.getPointIdsByType(0).contains(pointId)) {
            // 获取该公司下的所有角色列表
            List<RoleBriefDTO> roles = systemFacade.listRolesByTenantId(companyId);
            if (CollectionUtils.isNotEmpty(roles)) {
                pushConfig.setTargets(roles.stream().map(role -> role.getId().toString()).collect(Collectors.toSet()));
            }
        }
        return pushConfig;
    }

    @Override
    public List<PushConfig> listPushConfigsByRoleId(ObjectId roleId) {
        if (roleId == null) {
            return Collections.emptyList();
        }
        return pushConfigRepository.findByTargetsAndDataStatus(roleId.toString(), DataStatusEnum.VALID.value());
    }

    @Override
    public void updatePushConfigWithAddToSetTargetsByPointIdsAndCompanyId(ObjectId roleId,
                                                                          Collection<String> pointIds, ObjectId companyId) {
        PushConfig toUpdate = new PushConfig();
        toUpdate.setTargets(Collections.singleton(roleId.toString()));
        toUpdate.setUpdateAt(new Date());
        pushConfigRepository.updateWithAddToSetTargetsByCompanyIdAndPointIdInAndDataStatus(
                toUpdate, companyId, pointIds, DataStatusEnum.VALID.value());
    }

    @Override
    public void updatePushConfigWithPullAllTargetsByPointIdsAndCompanyId(ObjectId roleId,
                                                                         Collection<String> pointIds, ObjectId companyId) {
        PushConfig toUpdate = new PushConfig();
        toUpdate.setTargets(Collections.singleton(roleId.toString()));
        toUpdate.setUpdateAt(new Date());
        pushConfigRepository.updateWithPullAllTargetsByCompanyIdAndPointIdInAndDataStatus(
                toUpdate, companyId, pointIds, DataStatusEnum.VALID.value());
    }

    private JPushClient getJPushClient(ObjectId accountId) {
        JPushClient client = JPUSH_CLIENT_MAP.get(accountId.toString());
        if (client != null) {
            return client;
        }

        synchronized (JPUSH_CLIENT_MAP) {
            if ((client = JPUSH_CLIENT_MAP.get(accountId.toString())) != null) {
                return client;
            }

            PushAccount pushAccount = getPushAccount(accountId);
            if (pushAccount == null) {
                throw new BizException(-1, "推送账户不存在");
            }

            client = new JPushClient(pushAccount.getAppSecret(), pushAccount.getAppKey());
            JPUSH_CLIENT_MAP.put(accountId.toString(), client);
        }

        return client;
    }

    private static String replaceByReplacements(String template, Map replacements) {
        if (StringUtil.isBlank(template) || replacements == null || replacements.isEmpty()) {
            return template;
        }

        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariables(replacements);

        StringBuffer sb = new StringBuffer();
        try {
            Matcher matcher = PERCENT_SPLIT_PATTERN.matcher(template);
            while (matcher.find()) {
                String key = matcher.group(1);
                Object replacement = SPEL_PARSER.parseExpression(key).getValue(context);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(valueToString(replacement)));
            }
            matcher.appendTail(sb);
            return sb.toString();
        } finally {
            sb.setLength(0);
        }
    }

    private static String transControlChars(String str) {
        StringBuffer sb = new StringBuffer();
        try {
            Matcher matcher = CONTROL_CHARS.matcher(str);
            while (matcher.find()) {
                String key = matcher.group(0);
                String replacement = CONTROL_CHARS_MAP.get(key);
                matcher.appendReplacement(sb,
                        replacement == null ? "" : Matcher.quoteReplacement(replacement));
            }
            matcher.appendTail(sb);
            return sb.toString();
        } finally {
            sb.setLength(0);
        }
    }

    private static String valueToString(Object value) {
        if (value != null
                && (value.getClass().isArray()
                || value instanceof Collection
                || value instanceof Map)) {
            return JSON.toJSONString(value);
        }

        if (value instanceof Date) {
            value = ((Date) value).getTime();
        }

        return String.valueOf(value);
    }

}