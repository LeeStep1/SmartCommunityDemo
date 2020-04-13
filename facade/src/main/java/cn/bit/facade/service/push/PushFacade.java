package cn.bit.facade.service.push;

import cn.bit.facade.model.push.PushAccount;
import cn.bit.facade.model.push.PushConfig;
import cn.bit.facade.model.push.PushPoint;
import cn.bit.facade.model.push.PushTemplate;
import cn.bit.facade.vo.push.PushConfigPageQuery;
import cn.bit.facade.vo.push.PushConfigVO;
import cn.bit.facade.vo.push.PushResult;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface PushFacade {

    PushAccount addPushAccount(PushAccount pushAccount);

    PushAccount deletePushAccount(ObjectId id);

    PushAccount getPushAccount(ObjectId id);

    PushAccount updatePushAccount(PushAccount pushAccount);

    Page<PushAccount> getPushAccounts(int page, int size);

    PushTemplate addPushTemplate(PushTemplate pushTemplate);

    PushTemplate deletePushTemplateById(ObjectId id);

    PushTemplate getPushTemplateById(ObjectId id);

    PushTemplate updatePushTemplate(PushTemplate pushTemplate);

    PushPoint addPushPoint(PushPoint pushPoint);

    PushPoint deletePushPointById(ObjectId id);

    PushPoint getPushPointById(ObjectId id);

    List<PushPoint> getPushPointsBySignature(String signatureO);

    Page<PushPoint> getPushPointsByScopeIn(Object[] scopes, int page, int size);

    PushPoint updatePushPoint(PushPoint pushPoint);

    PushResult sendPush(ObjectId accountId, ObjectId templateId, Map data) throws BizException;

    void setDeviceTagAlias(ObjectId accountId, String registrationId, String alias, Set<String> tagsToAdd,
                           Set<String> tagsToRemove) throws BizException;

    /**
     * 分页查询推送功能节点配置
     *
     * @param query 查询条件
     * @return 返回分页实体
     */
    Page<PushConfig> listPushConfigs(PushConfigPageQuery query);

    /**
     * 新增
     *
     * @param pushConfigVO
     * @return
     */
    PushConfig addPushConfig(PushConfigVO pushConfigVO);

    /**
     * 编辑
     *
     * @param pushConfigVO
     * @return
     */
    PushConfig modifyPushConfig(PushConfigVO pushConfigVO);

    /**
     * 详情
     *
     * @param id
     * @return
     */
    PushConfig findPushConfigById(ObjectId id);

    /**
     * 根据物业公司ID及功能节点获取配置详情
     *
     * @param companyId
     * @param pointId
     * @return
     */
    PushConfig findPushConfigByCompanyIdAndPointId(ObjectId companyId, String pointId);

    /**
     * 公司角色查询配置列表
     * @param roleId
     * @return
     */
    List<PushConfig> listPushConfigsByRoleId(ObjectId roleId);

    /**
     * 将角色ID加入某些推送节点中
     * @param roleId
     * @param pointIds
     * @param companyId
     */
    void updatePushConfigWithAddToSetTargetsByPointIdsAndCompanyId(ObjectId roleId, Collection<String> pointIds, ObjectId companyId);

    /**
     * 将角色ID从某些推送节点中移除
     * @param roleId
     * @param pointIds
     * @param companyId
     */
    void updatePushConfigWithPullAllTargetsByPointIdsAndCompanyId(ObjectId roleId, Collection<String> pointIds, ObjectId companyId);
}
