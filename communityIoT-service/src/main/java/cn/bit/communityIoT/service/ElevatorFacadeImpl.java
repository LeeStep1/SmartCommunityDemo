package cn.bit.communityIoT.service;


import cn.bit.facade.exception.communityIoT.CommunityIoTBizException;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.service.communityIoT.ElevatorFacade;
import cn.bit.facade.vo.communityIoT.elevator.*;
import cn.bit.facade.vo.mq.KangTuElevatorAuthVO;
import cn.bit.facade.vo.property.ElevatorFaultRequest;
import cn.bit.facade.vo.statistics.ElevatorSummaryResponse;
import cn.bit.facade.vo.user.card.CardRequest;
import cn.bit.framework.utils.UUIDUitl;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Comparator;
import java.util.List;

import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.COMMUNITY_NULL;
import static cn.bit.facade.exception.communityIoT.CommunityIoTBizException.ELEVATOR_ID_NULL;

/**
 * 电梯
 */
@Service("elevatorFacade")
@Slf4j
public class ElevatorFacadeImpl implements ElevatorFacade {

    /**
     * 请求路径
     */
    @Value("${card.request.url}")
    private String url;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * 通过社区id和楼栋列表查询电梯列表
     */
    private static final String ELEVATOR_BUILD_URL = "/elevator/list/build";

    /**
     * 获取电梯凭证
     */
    private static final String ELEVATOR_AUTH_URL = "/certificate/elevator/add";

    private static final String CARD_COPY = "/certificate/copy";

    /**
     * 删除过期电梯凭证
     */
    private static final String ELEVATOR_AUTH_DELETE_URL = "/certificate/elevator/remove";
    /**
     * 更新凭证电梯权限
     */
    private static final String ELEVATOR_APPLY_URL = "/certificate/elevator/add";

    private static final String ELEVATOR_KEEP_URL = "/certificate/elevator/keep";

    private static final String ELEVATOR_FAULT_LIST_URL="/fault/view/list";

    private static final String ELEVATOR_REPAIR_LIST_URL ="/fault/record/view/list";

    private static final String ELEVATOR_ADD_FAULT = "/declare/propose";
    /**
     * 远程召梯
     */
    private static final String ELEVATOR_REMOTE_CALL = "/elevator/hallCallElevator";

    /**
     * 电梯品牌列表
     */
    private static final String ELEVATOR_BRAND_LIST = "/brand/list";

    /**
     * 电梯控制状态改变
     */
    private static final String ELEVATOR_DEVICE_OPEN = "/certificate/device/open";

    /**
     * 电梯控制状态改变
     */
    private static final String ELEVATOR_DEVICE_CLOSE = "/certificate/device/close";

    /**
     * 电梯详情查询
     */
    private static final String ELEVATOR_DETAIL = "/elevator/terminal";

    /**
     * 电梯信息修改
     */
    private static final String ELEVATOR_PART_KEEP = "/certificate/elevator/part/keep";


    /**
     * @param request
     * @return
     * @throws Exception
     */
    @Override
    public ElevatorPageResult getElevators(FindElevatorListRequest request, Integer page, Integer size) {
        if (request.getCommunityId() == null) {
            throw COMMUNITY_NULL;
        }

        if (request.getPage() == null) {
            request.setPage(page);
        }

        if (request.getSize() == null) {
            request.setSize(size);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(request), headers);

        ElevatorPageResult result = restTemplate.postForObject(url + ELEVATOR_BUILD_URL,
                                                               entity,
                                                               ElevatorPageResult.class);
        if (null != result.getData()) {
            result.getData().getRecords().sort(Comparator.comparing(ElevatorVO::getName));
        }
        return result;
    }

    @Override
    public Object findElevatorFaultList(String elevatorId, Integer page, Integer size) {
        return getElevatorList(elevatorId, page, size, ELEVATOR_FAULT_LIST_URL);
    }

    @Override
    public Object findElevatorRepairList(String elevatorId, Integer page, Integer size) {
        return getElevatorList(elevatorId, page, size, ELEVATOR_REPAIR_LIST_URL);
    }

    @Override
    public Object addElevatorFault(ElevatorFault elevatorFault) throws Exception {
        // 来源 1：C端用户提交 2：物业提交  3:维保端提交  必填
        ElevatorFaultRequest request = new ElevatorFaultRequest();
        request.setSource(2);
        request.setBuildingId(elevatorFault.getBuildingId().toString());
        request.setBuildingName(elevatorFault.getBuildingName());
        request.setCommunityId(elevatorFault.getCommunityId().toString());
        request.setCommunityName(elevatorFault.getCommunityName());
        request.setFaultDescription(elevatorFault.getFaultDescription());
        request.setImages(elevatorFault.getImages());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(request), headers);
        return restTemplate.postForObject(url + ELEVATOR_ADD_FAULT, entity, Object.class);
    }

    @Override
    public Object remoteCallElevator(Room room, CallElevatorRequest request) {
        switch (request.getRemoteType()) {
            case 1 :
                request.setCurrentFloor("@");
                request.setHallCallDirection(null);
                break;
            case 2 :
                request.setCurrentFloor(room.getFloorCode());
                request.setHallCallDirection(null);
                break;
        }
        request.setBuildingId(room.getBuildingId());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(request), headers);
        JSONObject response = restTemplate.postForObject(url + ELEVATOR_REMOTE_CALL, entity, JSONObject.class);
        if (!response.getBoolean("success")) {
            log.warn(response.getString("errorMsg"));
            throw new CommunityIoTBizException(response.getInteger("errorCode"), response.getString("errorMsg"));
        }
        return response;
    }

    @Override
    public Object remoteCallElevator(CallElevatorRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(request), headers);
        JSONObject response = restTemplate.postForObject(url + ELEVATOR_REMOTE_CALL, entity, JSONObject.class);
        if (!response.getBoolean("success")) {
            log.warn(response.getString("errorMsg"));
            throw new CommunityIoTBizException(response.getInteger("errorCode"), response.getString("errorMsg"));
        }
        return response;
    }

    @Override
    public Object findElevatorBrandList() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        JSONObject brandListRequest = JSONObject.parseObject("{'size' : 1000}");
        HttpEntity<Object> entity = new HttpEntity<>(brandListRequest, headers);
        return restTemplate.postForObject(url + ELEVATOR_BRAND_LIST, entity, Object.class);
    }

    @Override
    public boolean updateIoTElevatorAuth(KangTuElevatorAuthVO deviceAuthVO) {
        CardRequest cardRequest = new CardRequest(deviceAuthVO.getKeyType(), fixCardLength(deviceAuthVO.getKeyNo(), 12),
                deviceAuthVO.getKeyId(), deviceAuthVO.getBuilds(), null, deviceAuthVO.getProcessTime(),
                deviceAuthVO.getUsesTime(), null);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(cardRequest), httpHeaders);
        log.info("新增电梯权限");
        JSONObject jsonObject = restTemplate.postForObject(this.url + ELEVATOR_APPLY_URL, entity, JSONObject.class);
        if (!jsonObject.getBoolean("success")) {
            log.warn(jsonObject.getString("errorMsg"));
            throw new CommunityIoTBizException(jsonObject.getInteger("errorCode"), jsonObject.getString("errorMsg"));
        }
        return jsonObject.getBoolean("success");
    }

    @Override
    public boolean deleteIoTElevatorAuth(KangTuElevatorAuthVO deviceAuthVO) {
        CardRequest cardRequest = new CardRequest(deviceAuthVO.getKeyType(), fixCardLength(deviceAuthVO.getKeyNo(), 12),
                deviceAuthVO.getKeyId(), deviceAuthVO.getBuilds());

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(cardRequest), httpHeaders);
        log.info("删除电梯权限");
        JSONObject jsonObject = restTemplate.postForObject(this.url + ELEVATOR_AUTH_DELETE_URL, entity, JSONObject.class);
        if (!jsonObject.getBoolean("success")) {
            log.warn(jsonObject.getString("errorMsg"));
            throw new CommunityIoTBizException(jsonObject.getInteger("errorCode"), jsonObject.getString("errorMsg"));
        }
        return jsonObject.getBoolean("success");
    }

    @Override
    public boolean coverIoTElevatorAuth(KangTuElevatorAuthVO deviceAuthVO) {
        // 申请到电梯物联
        CardRequest cardRequest = new CardRequest(deviceAuthVO.getKeyType(), fixCardLength(deviceAuthVO.getKeyNo(), 12),
                deviceAuthVO.getKeyId(), deviceAuthVO.getBuilds(), null, deviceAuthVO.getProcessTime(),
                deviceAuthVO.getUsesTime(), true);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(cardRequest), httpHeaders);
        log.info("覆盖电梯权限");
        JSONObject jsonObject = restTemplate.postForObject(this.url + ELEVATOR_KEEP_URL, entity, JSONObject.class);
        if (!jsonObject.getBoolean("success")) {
            log.warn(jsonObject.getString("errorMsg"));
            throw new CommunityIoTBizException(jsonObject.getInteger("errorCode"), jsonObject.getString("errorMsg"));
        }
        return jsonObject.getBoolean("success");
    }

    @Override
    public boolean openElevatorControlStatus(ElevatorVO elevatorVO) {
        return editElevatorDeviceStatus(elevatorVO, ELEVATOR_DEVICE_OPEN);
    }

    @Override
    public boolean closeElevatorControlStatus(ElevatorVO elevatorVO) {
        return editElevatorDeviceStatus(elevatorVO, ELEVATOR_DEVICE_CLOSE);
    }

    @Override
    public ElevatorSummaryResponse summaryElevators(ObjectId communityId) {
        String sumElevatorUrl = this.url + "/elevator/total-fault-elevator/" + communityId + "/number";
        JSONObject jsonObject = restTemplate.getForObject(sumElevatorUrl, JSONObject.class);

        if (!jsonObject.getBoolean("success")) {
            throw new CommunityIoTBizException(jsonObject.getInteger("errorCode"), jsonObject.getString("errorMsg"));
        }

        JSONObject data = jsonObject.getJSONObject("data");
        if (data == null) {
            return null;
        }

        ElevatorSummaryResponse response = new ElevatorSummaryResponse();
        response.setTotal(data.getLong("totalNum"));
        response.setFaultCount(data.getLong("faultNum"));
        return response;
    }

    @Override
    public ElevatorDetailDTO getElevatorDetail(ElevatorDetailQO elevatorDetailQO) {
        JSONObject jsonObject = restTemplate.getForObject(
                this.url + ELEVATOR_DETAIL
                    + "/" + elevatorDetailQO.getTerminalCode()
                    + "/" + elevatorDetailQO.getTerminalPort(),
                JSONObject.class);

        if (!jsonObject.getBoolean("success")) {
            throw new CommunityIoTBizException(jsonObject.getInteger("errorCode"), jsonObject.getString("errorMsg"));
        }

        JSONObject data = jsonObject.getJSONObject("data");
        if (data == null) {
            return null;
        }

        return JSON.parseObject(data.toJSONString(), ElevatorDetailDTO.class);
    }

    @Override
    public void coverAuthByDeviceNumAndCard(List<String> deviceNum, Card card) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());

        CertificateElevatorPartKeepRequest request = new CertificateElevatorPartKeepRequest(deviceNum,
                                                                                            card);

        JSONObject jsonObject = restTemplate.postForObject(this.url + ELEVATOR_PART_KEEP,
                                                           new HttpEntity<>(JSON.toJSON(request), httpHeaders),
                                                           JSONObject.class);
        if (!jsonObject.getBoolean("success")) {
            log.warn(jsonObject.getString("errorMsg"));
            throw new CommunityIoTBizException(jsonObject.getInteger("errorCode"), jsonObject.getString("errorMsg"));
        }
    }

    private boolean editElevatorDeviceStatus(ElevatorVO elevatorVO, String url) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(elevatorVO), httpHeaders);
        JSONObject jsonObject = restTemplate.postForObject(this.url + url, entity, JSONObject.class);
        if (!jsonObject.getBoolean("success")) {
            log.warn(jsonObject.getString("errorMsg"));
            throw new CommunityIoTBizException(jsonObject.getInteger("errorCode"), jsonObject.getString("errorMsg"));
        }
        return jsonObject.getBoolean("success");
    }

    private Object getElevatorList(String elevatorId, Integer page, Integer size, String elevatorRepairListUrl) {
        if (StringUtils.isBlank(elevatorId)) {
            throw ELEVATOR_ID_NULL;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", MediaType.APPLICATION_JSON_UTF8.toString());
        FindElevatorListRequest request = new FindElevatorListRequest();
        request.setElevatorId(elevatorId);
        request.setPage(page);
        request.setSize(size);
        HttpEntity<Object> entity = new HttpEntity<>(JSON.toJSON(request), headers);
        return restTemplate.postForObject(url + elevatorRepairListUrl, entity, Object.class);
    }

    /**
     *
     * @param keyNo
     * @param length
     * @return
     */
    private String fixCardLength(String keyNo, int length) {
        //将不够指定位数位的字符串补齐指定位数位
        if (keyNo.length() < length) {
            keyNo = UUIDUitl.toFixdLengthString(keyNo, length);
        }
        //将长度大于指定位数位的字符串截取前面指定位数位
        else if (keyNo.length() > length) {
            keyNo = keyNo.substring(0, length);
        }
        keyNo = keyNo.toUpperCase();
        return keyNo;
    }
}
