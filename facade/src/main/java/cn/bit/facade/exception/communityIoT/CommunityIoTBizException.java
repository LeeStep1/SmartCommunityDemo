package cn.bit.facade.exception.communityIoT;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by terry on 2018/1/14.
 */
@Slf4j
public class CommunityIoTBizException extends BizException {

    public static final CommunityIoTBizException COMMUNITY_NULL = new CommunityIoTBizException(8010001, "社区id不能为空");

    public static final CommunityIoTBizException KEY_IDS_EMPTY = new CommunityIoTBizException(8010003, "keyId集合不能为空");

    public static final CommunityIoTBizException USER_LOCK = new CommunityIoTBizException(8010004, "该用户申请已提交, 请勿重复提交申请");

    public static final CommunityIoTBizException ELEVATOR_ID_NULL = new CommunityIoTBizException(8010005, "电梯Id不能为空");

    public static final CommunityIoTBizException FREEVIEW_REGISTER_FAILED = new CommunityIoTBizException(8010006, "全视通代理注册失败");

    public static final CommunityIoTBizException DOOR_NOT_EXIST = new CommunityIoTBizException(8010007, "门禁设备不存在");

    public static final CommunityIoTBizException REMOTE_OPEN_DOOR_FAILED = new CommunityIoTBizException(8010008, "远程开门失败");

    public static final CommunityIoTBizException FEATURE_DELETE_FAILED = new CommunityIoTBizException(8010009, "人脸识别删除失败");

    public static final CommunityIoTBizException CAMERA_ID_NULL = new CommunityIoTBizException(8010010, "监控设备ID不能为空");

    public static final CommunityIoTBizException CAMERA_NOT_EXISTS = new CommunityIoTBizException(8010011, "监控设备不存在");

    public static final CommunityIoTBizException DOOR_ID_NULL = new CommunityIoTBizException(8010012, "门禁设备id不能为空");

    public static final CommunityIoTBizException IOT_FAILED = new CommunityIoTBizException(8010013, "电梯物联系统异常");

    public static final CommunityIoTBizException USER_FEATURE_FAILED = new CommunityIoTBizException(8010014, "人脸录入失败");

    public static final CommunityIoTBizException FEATURE_NULL = new CommunityIoTBizException(8010015, "获取人脸失败");

    public static final CommunityIoTBizException INVALID_TOKEN = new CommunityIoTBizException(8010016, "获取第三方token失败");

    public static final CommunityIoTBizException TARGET_ID_IS_NULL = new CommunityIoTBizException(8010017,
            "目标设施ID不能为空");

    public static final CommunityIoTBizException TIME_UNIT_INVALID = new CommunityIoTBizException(8010018,
            "无效的时间度量单位");

    public static final CommunityIoTBizException STARTAT_AFTER_ENDAT = new CommunityIoTBizException(8010019, "开始时间不能晚于结束时间");

    public static final CommunityIoTBizException DEVICE_BRAND_NULL = new CommunityIoTBizException(8010020, "设备厂商不能为空");

    public static final CommunityIoTBizException ROOMS_NOT_IN_THE_SAME_BUILDING = new CommunityIoTBizException(8010021, "所选房间不在同一个楼栋");

    public static final CommunityIoTBizException ROOMS_EMPTY = new CommunityIoTBizException(8010022, "至少选择一个房间");

    public static final CommunityIoTBizException LIFT_CONTROL_DATE_IS_NULL = new CommunityIoTBizException(8010023, "梯控时间不能为空");

    public CommunityIoTBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public CommunityIoTBizException(int code, String msg) {
        super(code, msg);
    }

    public CommunityIoTBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public CommunityIoTBizException newInstance(String msgFormat, Object... args) {
        return new CommunityIoTBizException(this.code, msgFormat, args);
    }

    public CommunityIoTBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new CommunityIoTBizException(this.code, this.msg);
    }
}
