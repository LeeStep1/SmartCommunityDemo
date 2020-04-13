package cn.bit.facade.exception.community;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommunityBizException extends BizException {

    public static final CommunityBizException ROOMID_NULL = new CommunityBizException(3010001, "房间ID为空");

    public static final CommunityBizException COMMUNITY_ID_NULL = new CommunityBizException(3010002, "社区ID为空");

    public static final CommunityBizException BUILDING_ID_NULL = new CommunityBizException(3010003, "楼宇ID为空");

    public static final CommunityBizException COMMUNITY_NOT_EXISTS = new CommunityBizException(3010004, "社区不存在");

    public static final CommunityBizException USER_ROOM_ID_NULL = new CommunityBizException(3010005, "用户ID或房间ID为空");

    public static final CommunityBizException NOT_SERVICE_ID = new CommunityBizException(3010006, "服务ID为空");

    public static final CommunityBizException REMOTE_OPEN_ERROR = new CommunityBizException(3010007, "远程开门服务异常");

    public static final CommunityBizException COMMUNITY_NOT_BIND_PROPERTY = new CommunityBizException(3010008, "社区未绑定物业公司");

    public static final CommunityBizException GET_AUTH_DOOR_ERROR = new CommunityBizException(3010009, "获取门禁列表异常");

    public static final CommunityBizException DEVICE_NOT_ONLINE = new CommunityBizException(3010010, "终端不在线，远程开启门禁操作失败");

    public static final CommunityBizException CARD_EMPTY = new CommunityBizException(3010011, "用户在该社区卡信息为空");

    public static final CommunityBizException COMMUNITY_USED = new CommunityBizException(3010012, "社区已处于开放状态，不能操作");

    public static final CommunityBizException BUILDING_EXIST = new CommunityBizException(3010013, "社区已配置楼宇，不能操作");

    public static final CommunityBizException BUILDING_NOT_EXISTS = new CommunityBizException(3010014, "楼宇不存在");

    public static final CommunityBizException ROOM_EXIST = new CommunityBizException(3010015, "楼宇已配置房间，不能操作");

    public static final CommunityBizException ROOM_NOT_EXISTS = new CommunityBizException(3010016, "房间不存在");

    public static final CommunityBizException BUILDING_USED = new CommunityBizException(3010017, "楼宇已处于开放状态，不能操作");

    public static final CommunityBizException NAME_IS_NULL = new CommunityBizException(3010018, "名称不能为空");

    public static final CommunityBizException NAME_EXIST = new CommunityBizException(3010019, "名称已存在");

    public static final CommunityBizException OVERGROUND_IS_NULL = new CommunityBizException(3010020, "楼面层数不能为空");

    public static final CommunityBizException UNDERGROUND_IS_NULL = new CommunityBizException(3010021, "地下层数不能为空");

    public static final CommunityBizException ROOMNUM_INVALID = new CommunityBizException(3010022, "房间数量必填且只能是正整数");

    public static final CommunityBizException FLOORCODE_IS_NULL = new CommunityBizException(3010023, "所在楼层不能为空");

    public static final CommunityBizException AREA_IS_INVALID = new CommunityBizException(3010024, "住房面积必填且只能是正数");

    public static final CommunityBizException FLOOR_NUM_INVALID = new CommunityBizException(3010025, "无效楼层数");

    public static final CommunityBizException SAME_OPENING_STATUS = new CommunityBizException(3010026, "开放状态一致，无需重复操作");

    public static final CommunityBizException DISTRICT_OPEN = new CommunityBizException(3010027, "职能区域已经开放使用，无法再次编辑");

    public static final CommunityBizException DISTRICT_BUILDING_EMPTY = new CommunityBizException(3010028, "职能区域楼栋不能为空");

    public static final CommunityBizException DISTRICT_COMMUNITY_EMPTY = new CommunityBizException(3010029, "职能区域社区不能为空");

    public static final CommunityBizException DISTRICT_NOT_EXIST = new CommunityBizException(3010030, "职能区域不存在");

    public static final CommunityBizException DISTRICT_NAME_NULL = new CommunityBizException(3010031, "职能区域名称不能为空。");

    public static final CommunityBizException DISTRICT_BUILDING_REPEAT = new CommunityBizException(3010032, "职能区域楼栋授权重复");

    public static final CommunityBizException IoT_ERROR = new CommunityBizException(3010033, "电梯物联异常");

    public static final CommunityBizException DISTRICT_NAME_REPETITION = new CommunityBizException(3010034, "职能区域名称重复");

    public static final CommunityBizException PARAM_CONFIG_TYPE_NULL = new CommunityBizException(3010035, "配置类型不能为空");
    public static final CommunityBizException PARAM_ID_NULL = new CommunityBizException(3010036, "配置项ID不能为空");
    public static final CommunityBizException NOT_NEED_TO_UPDATED = new CommunityBizException(3010037, "没有需要更新的项");

    public static final CommunityBizException DISTRICT_NAME_NOT_EXIST = new CommunityBizException(3010038, "职能区域名称为空");

    public static final CommunityBizException BILL_PARAM_INVALID = new CommunityBizException(3010039, "设置参数的值无效");

    public static final CommunityBizException BILL_PARAM_NOT_EXIST = new CommunityBizException(3010040, "缺少必要的物业费相关参数配置");

    public static final CommunityBizException FLOOR_NOT_EXISTS = new CommunityBizException(3010041, "楼层不存在");
    public static final CommunityBizException ZONE_NOT_EXISTS = new CommunityBizException(3010042, "分区不存在");
    public static final CommunityBizException MISSING_LEVEL = new CommunityBizException(3010044, "缺少层级参数");
    public static final CommunityBizException MISSING_TARGET = new CommunityBizException(3010045, "缺少目标参数");
    public static final CommunityBizException INVALID_LEVEL = new CommunityBizException(3010046, "无效的层级");
    public static final CommunityBizException COMMUNITY_IS_LOCKED = new CommunityBizException(3010047, "社区当前已锁定，无法进行操作");
    public static final CommunityBizException DEVICE_BRANDS_NULL = new CommunityBizException(3010048, "没有需要变更的设备品牌厂商");

    public static final CommunityBizException LAYOUT_ID_NULL = new CommunityBizException(3010049, "布局ID为空");

    public CommunityBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public CommunityBizException(int code, String msg) {
        super(code, msg);
    }

    public CommunityBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public CommunityBizException newInstance(String msgFormat, Object... args) {
        return new CommunityBizException(this.code, msgFormat, args);
    }

    public CommunityBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new CommunityBizException(this.code, this.msg);
    }
}
