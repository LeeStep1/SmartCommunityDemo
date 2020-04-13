package cn.bit.api.support.aop;

import cn.bit.api.support.*;
import cn.bit.api.support.annotation.SendPush;
import cn.bit.common.facade.data.Page;
import cn.bit.common.facade.enums.OsEnum;
import cn.bit.common.facade.system.dto.AppDTO;
import cn.bit.common.facade.system.query.AppPageQuery;
import cn.bit.common.facade.system.service.SystemFacade;
import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.push.PushPointEnum;
import cn.bit.facade.model.push.PushConfig;
import cn.bit.facade.model.system.Client;
import cn.bit.facade.service.push.PushFacade;
import cn.bit.facade.service.system.ClientFacade;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.push.facade.dubbo.dto.PushTemplateMessagesDTO;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Aspect
@Component
@Slf4j
public class SendPushAspect {

    private ClientFacade clientFacade;

    private PushFacade pushFacade;

    private cn.bit.push.facade.dubbo.PushFacade commonPushFacade;

    private SystemFacade commonSystemFacade;

    private final String CLIENT = "client";
    private final String PARTNER = "partner";
    private final String ROLES = "roles";

    @Autowired
    public SendPushAspect(ClientFacade clientFacade, PushFacade pushFacade,
                          cn.bit.push.facade.dubbo.PushFacade commonPushFacade,
                          SystemFacade commonSystemFacade) {
        this.clientFacade = clientFacade;
        this.pushFacade = pushFacade;
        this.commonPushFacade = commonPushFacade;
        this.commonSystemFacade = commonSystemFacade;
    }

    @Around(value = "@annotation(sendPush)")
    public Object doAround(ProceedingJoinPoint pjp, SendPush sendPush) throws Throwable {
        Object result = proceedWithSendPush(pjp, sendPush);
        return unwrapResult(result);
    }

    private Object proceedWithSendPush(ProceedingJoinPoint pjp, SendPush sendPush) throws Throwable {
        String signature = getSignature(sendPush, pjp.getSignature());

        List<Integer> types = Arrays.stream(ClientType.values()).map(ClientType::value).collect(Collectors.toList());
        Map<Integer, Client> clientMap = getClientMapSafely(types);
        if (clientMap.isEmpty()) {
            log.warn("{} [annotated with {}] is disable to send push. Cause by invalid CLIENT [{}].",
                    signature, SendPush.class.getName(), types);
            return pjp.proceed();
        }
        PushPointEnum pushPointEnum = sendPush.point();
        PushConfig pushConfig =
                pushFacade.findPushConfigByCompanyIdAndPointId(SessionUtil.getCompanyId(), pushPointEnum.name());
        if (pushConfig == null) {
            log.warn("{} [annotated with {}] is disable to send push. Cause by pushPoint({}) and companyId({}) not exist PushConfig.",
                    signature, SendPush.class.getName(), pushPointEnum.value(), SessionUtil.getCompanyId());
            return pjp.proceed();
        }

        Object result = pjp.proceed();
        List<Map<String, Object>> dataList = getDataObjectsFromResult(result, sendPush.pushData());
        if (CollectionUtils.isEmpty(dataList)) {
            log.warn("{} [annotated with {}] is disable to send push. Cause by getDataObjectsFromResult return null.",
                    signature, SendPush.class.getName());
            return result;
        }

        for (Map<String, Object> dataMap : dataList) {
            if (dataMap.isEmpty()) {
                continue;
            }
            Set<Integer> clients = new HashSet<>();
            ObjectMapper mapper = new ObjectMapper();
            if (dataMap.get("clients") != null) {
                clients =  (HashSet<Integer>) mapper.readValue(dataMap.get("clients").toString(), HashSet.class);
            }

            if (CollectionUtils.isEmpty(clients)) {
                clients = new HashSet<>();
                ClientType[] clientTypes = sendPush.clientTypes();
                for (ClientType clientType : clientTypes) {
                    clients.add(clientType.value());
                }
            }
            if (CollectionUtils.isEmpty(clients)) {
                log.warn("{} [annotated with {}] is disable to send push. Cause by client is null.",
                        signature, SendPush.class.getName());
                return result;
            }

            AppSubject appSubject = SessionUtil.getAppSubject();
            dataMap.put(PARTNER, appSubject.getPartner());
            AppPageQuery pageQuery = new AppPageQuery();
            pageQuery.setSize(10);
            pageQuery.setPage(1);
            pageQuery.setClients(clients);
            pageQuery.setPartner(appSubject.getPartner());
            pageQuery.setOss(Arrays.asList(OsEnum.IOS.value()));
            Page<AppDTO> apps = commonSystemFacade.listApps(pageQuery);
            if (apps.getTotal() == 0) {
                log.warn("{} [annotated with {}] is disable to send push. Cause by app is not exist.",
                        signature, SendPush.class.getName());
                return result;
            }
            Map<Integer, ObjectId> pushAppIdMap =
                    apps.getRecords().stream().filter(appDTO -> appDTO.getPushAppId() != null)
                            .collect(Collectors.toMap(AppDTO::getClient, AppDTO::getPushAppId));
            PushTemplateMessagesDTO messagesDTO = new PushTemplateMessagesDTO();
            // 推送模板ID
            messagesDTO.setTemplateId(pushConfig.getTmplId());
            for (Integer client : clients) {
                if (pushAppIdMap.get(client) == null) {
                    log.warn("{} [annotated with {}] at client({}) is disable to send push. Cause by pushAppId is null.",
                            signature, SendPush.class.getName(), client);
                    continue;
                }
                messagesDTO.setAppId(pushAppIdMap.get(client));
                dataMap.put(CLIENT, client);
                dataMap.put(ROLES, pushConfig.getTargets());
                messagesDTO.setValueMap(dataMap);
                try {
                    commonPushFacade.pushTemplateMessages(messagesDTO);
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
            }
        }
        return result;
    }

    private String getSignature(SendPush sendPush, Signature signature) {
        return StringUtil.isNotBlank(sendPush.value()) ? sendPush.value() : signature.toString();
    }

    private Map<Integer, Client> getClientMapSafely(List<Integer> types) {
        try {
            AppSubject appSubject = SessionUtil.getAppSubject();
            return clientFacade.getClientByTypes(types, appSubject.getPartner()).stream()
                    .collect(Collectors.toMap(Client::getType, client -> client));
        } catch (Exception e) {
            return Collections.emptyMap();
        }
    }

    private List<Map<String, Object>> getDataObjectsFromResult(Object result, boolean pushData) {
        List<Map<String, Object>> maps = new ArrayList<>();
        Map<String, Object> datas = new HashMap<>();
        if (result instanceof ApiResult) {
            if (pushData) {
                Object data = ((ApiResult) result).getData();
                if (data != null && !data.getClass().isArray()
                        && !(Collection.class.isAssignableFrom(data.getClass()))) {
                    datas.putAll(JSONObject.parseObject(JSON.toJSONString(data)));
                }
            }

            if (result instanceof WrapResult) {
                Object[] objects = ((WrapResult) result).getDataArray();
                if (objects.length == 1 && objects[0] instanceof List) {
                    for (Object object : (List)objects[0]) {
                        Map<String, Object> dataMap = new HashMap<>();
                        dataMap.putAll(datas);
                        if (object instanceof PushTask) {
                            dataMap.putAll(JSONObject.parseObject(JSON.toJSONString(((PushTask) object).getPushTarget())));
                            dataMap.putAll(JSONObject.parseObject(JSON.toJSONString(((PushTask) object).getDataObject())));
                            dataMap.putAll(JSONObject.parseObject(JSON.toJSONString(object)));
                            maps.add(dataMap);
                            continue;
                        }
                        dataMap.putAll(JSONObject.parseObject(JSON.toJSONString(object)));
                        maps.add(dataMap);
                    }
                    return maps;
                }
                for (Object object : objects) {
                    if (object instanceof PushTask) {
                        datas.putAll(JSONObject.parseObject(JSON.toJSONString(((PushTask) object).getPushTarget())));
                        datas.putAll(JSONObject.parseObject(JSON.toJSONString(((PushTask) object).getDataObject())));
                        continue;
                    }
                    datas.putAll(JSONObject.parseObject(JSON.toJSONString(object)));
                }
            }
            maps.add(datas);
            return maps;
        }
        Object data = BeanUtils.getProperty(result, "data");
        if (data != null) {
            datas.putAll(JSONObject.parseObject(JSON.toJSONString(data)));
        } else {
            datas.putAll(JSONObject.parseObject(JSON.toJSONString(result)));
        }
        maps.add(datas);
        return maps;
    }

    private Object unwrapResult(Object result) {
        if (result instanceof WrapResult) {
            return ((WrapResult) result).getActualResult();
        }
        return result;
    }

}
