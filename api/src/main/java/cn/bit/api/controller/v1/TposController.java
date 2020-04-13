package cn.bit.api.controller.v1;

import cn.bit.api.support.ApiResult;
import cn.bit.api.support.PushTarget;
import cn.bit.api.support.PushTask;
import cn.bit.api.support.WrapResult;
import cn.bit.api.support.annotation.SendPush;
import cn.bit.facade.enums.ClientType;
import cn.bit.facade.enums.ManufactureType;
import cn.bit.facade.enums.push.PushPointEnum;
import cn.bit.facade.model.community.Building;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.model.community.Room;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.communityIoT.DoorRecord;
import cn.bit.facade.model.system.ThirdApp;
import cn.bit.facade.model.user.Card;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.service.community.BuildingFacade;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.community.RoomFacade;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.service.communityIoT.DoorRecordFacade;
import cn.bit.facade.service.communityIoT.ElevatorFacade;
import cn.bit.facade.service.system.ThirdAppFacade;
import cn.bit.facade.service.user.CardFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.service.user.UserToRoomFacade;
import cn.bit.facade.vo.community.zhfreeview.*;
import cn.bit.facade.vo.communityIoT.elevator.CallElevatorRequest;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.utils.NumberUtil;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/v1", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class TposController {

    /**
     * 单位秒 超时时间（由于全视通回调缓慢，延长时间80s）
     */
    private static final int expiresSecond = 80;

    /**
     * 根据指定的字符构建正则
     */
    private static Pattern pattern = Pattern.compile("-");

    @Value("${freeView.basic.uuid}")
    private String freeViewUUID;

    private static SimpleDateFormat format;

    @Autowired
    private ThirdAppFacade thirdAppFacade;

    @Autowired
    private DoorFacade doorFacade;

    @Autowired
    private CardFacade cardFacade;

    @Autowired
    private DoorRecordFacade doorRecordFacade;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private RoomFacade roomFacade;

    @Autowired
    private UserToRoomFacade userToRoomFacade;

    @Autowired
    private BuildingFacade buildingFacade;

    @Autowired
    private CommunityFacade communityFacade;

    @Autowired
    private ElevatorFacade elevatorFacade;

    static {
        // 获取上传文件路径的配置
        try {
            format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        } catch (MissingResourceException m) {
            m.printStackTrace();
        }
    }

    /**
     * 4.3	设备状态同步接口
     *
     * @param deviceParam
     * @return
     */
    @PostMapping(name = "全视通设备状态同步", path = "/zhfreeview/PushDeviceStateMsg")
    public Map PushDeviceStateMsg(DeviceParam deviceParam) {
        ThirdApp app = new ThirdApp();
        app.setName(deviceParam.toString() + ",sign =" + deviceParam.getSign());
        app.setType(3);
        thirdAppFacade.addThirdApp(app);

        Map map = verifySign(freeViewUUID, deviceParam, deviceParam.getSign(), deviceParam.getTimestamp());
        if (map.get("Code").equals(200)) {
            DeviceParams deviceParams = getByDeviceLocalDirectory(deviceParam.getDeviceLocalDirectory());
            Door door = doorFacade.updateFreeViewDeviceState(deviceParam, deviceParams);
            communityFacade.addToSetBrandsById(door.getCommunityId(), Collections.singleton("door" + door.getBrandNo()));
            map.put("Msg", door);
        }
        return map;
    }

    /**
     * 4.4 卡下发状态同步接口
     *
     * @param cardStateParam
     * @return
     */
    @PostMapping(name = "全视通卡下发状态同步", path = "/zhfreeview/PushCardStateMsg")
    public Map PushCardStateMsg(CardStateParam cardStateParam) {
        ThirdApp app = new ThirdApp();
        app.setName(cardStateParam.toString() + ",sign =" + cardStateParam.getSign());
        app.setType(4);
        thirdAppFacade.addThirdApp(app);

        Map map = verifySign(freeViewUUID, cardStateParam, cardStateParam.getSign(), cardStateParam.getTimestamp());
        if (map.get("Code").equals(200)) {
            // 根据卡号获取卡片信息
            Card card = cardFacade.findByKeyNo(cardStateParam.getCardSerialNumber());
            if (card != null) {
                // 已读取
                card = cardFacade.updateIsProcessedById(card.getId(), 1);
                if (card != null) {
                    map.put("Msg", card);
                }
            }
        }
        return map;
    }

    /**
     * 4.5	门禁记录同步接口
     *
     * @param doorRecordParam
     * @return
     */
    @PostMapping(name = "全视通门禁记录同步", path = "/zhfreeview/PushOpenDoorMsg")
    public Map PushOpenDoorMsg(DoorRecordParam doorRecordParam) throws ParseException {
        ThirdApp app = new ThirdApp();
        app.setName(doorRecordParam.toString() + ",sign =" + doorRecordParam.getSign());
        app.setType(5);
        thirdAppFacade.addThirdApp(app);
        Map map = verifySign(freeViewUUID, doorRecordParam, doorRecordParam.getSign(), doorRecordParam.getTimestamp());
        if (map.get("Code").equals(200)) {
            DoorRecord doorRecord = new DoorRecord();
            if (StringUtil.isEmpty(doorRecordParam.getDeviceLocalDirectory()) || !NumberUtil.isDigits(doorRecordParam.getDeviceID())) {
                log.warn("必填参数缺失 {}", doorRecordParam);
                return map;
            }
            //定位小区信息
            Door door = doorFacade.getDoorByDeviceIdAndBrandNoAndDeviceCode(NumberUtil.toLong(doorRecordParam.getDeviceID()), ManufactureType.FREEVIEW_DOOR.KEY, doorRecordParam.getDeviceLocalDirectory());
            if (Objects.isNull(door)) {
                log.warn("小区未正确绑定 {}", door);
                return map;
            }
            doorRecord.setDeviceId(String.valueOf(doorRecordParam.getDeviceID()));
            doorRecord.setDeviceLocalDirectory(doorRecordParam.getDeviceLocalDirectory());
            doorRecord.setCommunityId(door.getCommunityId());
            doorRecord.setDeviceName(doorRecordParam.getDeviceName());

            // 开门成功后直接进行门梯联动
            if (doorRecordParam.getAccessResult()) {
                Building checkLinkageAuth = buildingFacade.findOne(door.getBuildingId());
                log.info("楼盘信息 {}", checkLinkageAuth);
                // 楼栋允许门梯
                if (checkLinkageAuth.getDoorElevatorLinkage()) {
                    doorElevatorLinkage(door.getBuildingId());
                }
            }
            // 根据二维码信息获取生成二维码的用户信息
            if (StringUtil.isNotEmpty(doorRecordParam.getCardSerialNumber())) {
                doorRecord.setKeyNo(doorRecordParam.getCardSerialNumber().toUpperCase());
                Card card = cardFacade.findByKeyNo(doorRecord.getKeyNo());
                if (card != null) {
                    doorRecord.setUserId(card.getUserId());
                    doorRecord.setUserName(card.getName());
                    doorRecord.setPhone(card.getPhone());
                }
            }
            // 头像
            if (StringUtil.isNotEmpty(doorRecordParam.getPhotoHost()) && StringUtil.isNotEmpty(doorRecordParam.getOpenDoorPhotoList())) {
                doorRecord.setHeadImg(doorRecordParam.getPhotoHost() + "/" + doorRecordParam.getOpenDoorPhotoList());
            }
            // 开门时间
            if (StringUtil.isNotEmpty(doorRecordParam.getOpenDoorTime())) {
                doorRecord.setTime(format.parse(doorRecordParam.getOpenDoorTime()));
            }
            // 设备厂商名称
            doorRecord.setDeviceManufacturer(ManufactureType.FREEVIEW_DOOR.VALUE);
            // 获取本系统的用户信息
            if (StringUtil.isNotNull(doorRecordParam.getPersonnelName())) {
                // 获取用户信息
                UserVO user = userFacade.findById(new ObjectId(doorRecordParam.getPersonnelName()));
                if (user != null) {
                    doorRecord.setUserId(user.getId());
                    doorRecord.setUserName(user.getName());
                    doorRecord.setPhone(user.getPhone());
                }
            }
            // 门禁ID
            if (StringUtil.isNotNull(doorRecordParam.getResquestId())) {
                doorRecord.setDoorId(new ObjectId(doorRecordParam.getResquestId()));
            }
            doorRecord.setResult(doorRecordParam.getAccessResult() ? "开门成功" : "开门失败");
            doorRecord.setResultCode(doorRecordParam.getAccessResult() ? 1 : -1);
            //全视通有几十种开锁方式
            doorRecord.setUseStyle(doorRecordParam.getAccessWay() == 26 ? OpenDoorWay.Bluetooth.key : OpenDoorWay.Remote.key);
            //第三方平台Keyno无法确定， 无法满足对象约束
            if (Objects.isNull(doorRecord.getKeyNo())) {
                doorRecord.setKeyNo("-");
            }
            doorRecordFacade.addDoorRecord(doorRecord);
        }
        return map;
    }


    private void doorElevatorLinkage(ObjectId buildingId) {
        // 根据楼栋寻找
        Room room = new Room();
        room.setBuildingId(buildingId);
        CallElevatorRequest request = new CallElevatorRequest();
        request.setRemoteType(1);
        elevatorFacade.remoteCallElevator(room, request);
    }

    /**
     * 门口机键入房号并开始向手机APP发起呼叫，
     * 全视通平台将会把收到的呼叫事件推送至客户平台
     *
     * @return
     */
    @PostMapping(name = "全视通推送呼叫事件到客户平台", path = "/zhfreeview/PushCallMsg")
    @SendPush(
            scope = SendPush.Scope.COMMUNITY,
            clientTypes = ClientType.HOUSEHOLD,
            pushData = false,
            point = PushPointEnum.FREE_VIEW_CALLING
    )
    public ApiResult pushCallMsg(CallParam callParam) {
        ThirdApp app = new ThirdApp();
        app.setName(callParam == null ? null : callParam.toString() + ",Sign = " + callParam.getSign());
        app.setType(6);
        app = thirdAppFacade.addThirdApp(app);
        if (callParam == null) {
            log.error("Free View PushCallMsg: callParam is null !!!");
            return ApiResult.error(400, "");
        }

        Map map = verifySign(freeViewUUID, callParam, callParam.getSign(), callParam.getTimestamp());
        if (map.get("Code").equals(200) && callParam.getCalledRoomDirectory() != null) {
            // 根据房间编号获取房间用户
            Room room = roomFacade.findByOutId(callParam.getCalledRoomDirectory());
            if (room == null) {
                log.info("没有找到对应的房间:{}", callParam.getCalledRoomDirectory());
                return ApiResult.ok();
            }
            // 获取用户信息推送信息
            List<UserToRoom> userToRooms = userToRoomFacade.findByRoomId(room.getId());
            if (userToRooms != null && userToRooms.size() > 0) {
                Set<ObjectId> userIds = userToRooms.stream().map(UserToRoom::getUserId).collect(Collectors.toSet());
                log.info("推送全视通视频给房间的用户：{}", userIds);
                // 推送......
                Map<String, String> pushData = new HashMap<>();
                // 呼叫的CID
                pushData.put("roomLocation", userToRooms.get(0).getRoomLocation());
                pushData.put("callMsgId", callParam.getCallMsgID());
                pushData.put("communityId", room.getCommunityId().toString());

                PushTask pushTask = new PushTask();
                PushTarget pushTarget = new PushTarget();
                pushTarget.setUserIds(userIds);
                pushTask.setPushTarget(pushTarget);
                pushTask.setDataObject(pushData);
                return WrapResult.create(ApiResult.ok(), pushTask);
            }
        }
        log.info("推送失败，不符合推送条件 code:{}, callParam.getCalledRoomDirectory() = {}", map.get("Code"), callParam.getCalledRoomDirectory());
        return ApiResult.ok();
    }

    /**
     * 4.7	设备进入读卡模式的结果通知接口
     *
     * @param cardReadingResultParam
     * @return
     */
    @PostMapping(name = "全视通设备进入读卡模式的结果通知", path = "/zhfreeview/PushCardReadingModeResult")
    public Map PushCardReadingModeResult(CardReadingResultParam cardReadingResultParam) {
        Map map = new HashMap();
        map.put("Code", 200);
        map.put("Msg", "设备状态消息已接收");
        return map;
    }

    /**
     * TODO 4.8 推送卡片信息
     * 4.8	设备读卡模式下上报读到的卡信息
     *
     * @param cardReadingParam
     * @return
     */
    @PostMapping(name = "全视通推送卡片信息", path = "/zhfreeview/PushCardReadingInfo")
    public Map PushCardReadingInfo(CardReadingParam cardReadingParam) {
        Map map = new HashMap();
        try {
            ThirdApp app = new ThirdApp();
            app.setName(cardReadingParam.toString());
            app.setType(8);
            thirdAppFacade.addThirdApp(app);
        } catch (Exception e) {
            log.error("Exception:", e);
            map.put("Code", 401);
            map.put("Msg", e.getMessage());
            return map;
        }
        map.put("Code", 200);
        map.put("Msg", "设备状态消息已接收");
        return map;
    }

    /**
     * 4.9	人体特征信息下发状态同步接口
     *
     * @param featureParam
     * @return
     */
    @PostMapping(name = "人体特征信息下发状态同步", path = "/zhfreeview/PushFeatureStateMsg")
    public Map PushFeatureStateMsg(FeatureParam featureParam) {
        ThirdApp app = new ThirdApp();
        app.setName(featureParam.toString() + ",sign =" + featureParam.getSign());
        app.setType(9);
        thirdAppFacade.addThirdApp(app);

        Map map = verifySign(freeViewUUID, featureParam, featureParam.getSign(), featureParam.getTimestamp());
        // code为200，成功
        if (map.get("Code").equals(200)) {
            userFacade.updateCMUsersFaceInfo(featureParam.getFeatureCode(), featureParam.getHumanFeatureState());
        }
        return map;
    }


    private static Map verifySign(String uuid, Object obj, String sign, Integer timestamp) {
        Map result = new HashMap();
        if (timestamp == null || sign == null) {
            result.put("Code", 400);
            result.put("Msg", "时间戳或签名为空");
            return result;
        }
        // 校验时间是否在有效范围（超系统开始的80秒后，视为过时操作）
        Long nowMillis = Long.valueOf(Time2Date());
        long expMillis = Long.valueOf(timestamp) + expiresSecond;
        // 判断是否请求超时
        if (expMillis < nowMillis) {
            result.put("Code", 403);
            result.put("Msg", "签名已过期");
            return result;
        }
        StringBuffer nameValue = new StringBuffer().append(uuid);
        Class cls = obj.getClass();
        Field[] fields = cls.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            try {
                Field field = fields[i];
                field.setAccessible(true);
                String name = field.getName();
                Object value = field.get(obj);
                if (StringUtil.isNotNull(value)) {
                    try {
                        value = URLEncoder.encode(value.toString(), "utf-8");
                    } catch (UnsupportedEncodingException e) {
                        log.error("UnsupportedEncodingException:", e);
                    }
                    nameValue.append(name).append("=").append(value).append("&");
                }
            } catch (IllegalAccessException e) {
                log.error("IllegalAccessException:", e);
            }
        }
        String signNow = EncoderByMd5(nameValue.toString().substring(0, nameValue.toString().length() - 1).toLowerCase());
        if (!sign.equals(signNow)) {
            result.put("Code", 401);
            result.put("Msg", "签名验证失败");
            return result;
        }
        result.put("Code", 200);
        result.put("Msg", "设备状态消息已接收");
        return result;
    }

    private static String EncoderByMd5(String str) {
        log.info("start EncoderByMd5 str:{}", str);
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(str.getBytes("GBK"));
            StringBuffer buf = new StringBuffer();
            for (byte b : md.digest()) {
                buf.append(String.format("%02x", b & 0xff));
            }
            return buf.toString();
        } catch (Exception e) {
            log.error("Exception:", e);
            return null;
        }
    }

    private DeviceParams getByDeviceLocalDirectory(String deviceLocalDirectory) {
        // 构建字符串和正则的匹配
        Matcher matcher = pattern.matcher(deviceLocalDirectory);
        int count = 0;
        // 循环依次往下匹配
        // 如果匹配,则数量+1
        while (matcher.find()) {
            count++;
        }
        DeviceParams deviceParams = new DeviceParams();
        String[] directories = deviceLocalDirectory.split("-", -1);
        if (count == 2) {
            // 获取楼栋编号
            String buildingCode = String.format("%s-%s", directories[0], directories[1]);
            Building building = buildingFacade.findByOutId(buildingCode);

            deviceParams.setCommunityId(building.getCommunityId());
            deviceParams.setBuildingId(building.getId());
            deviceParams.setType(count);
            deviceParams.setName(building.getName());
            deviceParams.setCode(buildingCode);

        }
        // 社区
        else if (count == 1) {
            // 获取社区编号
            String communityCode = directories[0];
            Community community = communityFacade.findByCode(communityCode);

            deviceParams.setCommunityId(community.getId());
            deviceParams.setName(community.getName());
            deviceParams.setType(count);
            deviceParams.setCode(communityCode);
        }
        return deviceParams;
    }

    private static String Time2Date() {
        Long date = System.currentTimeMillis() / 1000;
        return String.valueOf(date);
    }

    private enum OpenDoorWay {

        Bluetooth(1, "蓝牙开门"), Remote(2, "远程开门");

        public Integer key;

        public String value;

        OpenDoorWay(Integer key, String value) {
            this.key = key;
            this.value = value;
        }
    }
}