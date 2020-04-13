package cn.bit.facade.exception.user;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by terry on 2018/1/14.
 */
@Slf4j
public class UserBizException extends BizException {
    public static final UserBizException USER_NOT_EXITS = new UserBizException(2010001, "用户不存在");
    public static final UserBizException PASSWORD_NOT_CORRECT = new UserBizException(2010002, "密码错误");

    public static final UserBizException CODE_NOT_CORRECT = new UserBizException(2010003, "验证码输入错误");

    public static final UserBizException NO_PROPRIETOR = new UserBizException(2010004, "业主不存在");

    public static final UserBizException USER_EXIST = new UserBizException(2010005, "用户已存在");

    public static final UserBizException REGISTER_USER = new UserBizException(2010006, "该手机号还没注册，请下载APP注册");

    public static final UserBizException PHONE_NULL = new UserBizException(2010007, "手机号为空");

    public static final UserBizException USER_ID_NULL = new UserBizException(2010008, "用户ID为空");

    public static final UserBizException USER_INFO_INCOMPLETE = new UserBizException(2010009, "请先完善用户信息");

    public static final UserBizException IDENTITY_CARD_ILLEGAL = new UserBizException(2010010, "身份证不合法");

    public static final UserBizException ROOM_BE_AUTHENTICATED = new UserBizException(2010011, "房间已被认证");

    public static final UserBizException APPLICATION_AUDITED = new UserBizException(2010012, "申请已被审核");

    public static final UserBizException USER_TO_ROOM_ID_NULL = new UserBizException(2010013, "用户认证id为空");

    public static final UserBizException PHONE_REGISTERED = new UserBizException(2010014, "手机号已被注册，请直接登录");

    public static final UserBizException DATA_LOCKED = new UserBizException(2010015, "当前数据被锁定，请稍后再试");

    public static final UserBizException CAN_NOT_APPLY = new UserBizException(2010016, "业主尚未开放申请");

    public static final UserBizException PROPRIETOR_EXIST = new UserBizException(2010017, "业主已存在");

    public static final UserBizException PROPRIETOR_MISMATCH = new UserBizException(2010018, "业主不匹配");

    public static final UserBizException APPLY_EXIST = new UserBizException(2010019, "您已是该房间成员或已提交申请");

    public static final UserBizException ROOM_STATUS_EXCEPTION = new UserBizException(2010020, "房间状态异常,请联系物业");

    public static final UserBizException BUILDING_ID_NULL = new UserBizException(2010022, "楼栋ID为空");

    public static final UserBizException NEW_PASSWORD_NULL = new UserBizException(2010023, "新密码为空");

    public static final UserBizException CLIENT_NULL = new UserBizException(2010024, "客户端类型为空");

    public static final UserBizException ADMIN_NULL = new UserBizException(2010025, "该物业区域缺少管理员，请添加管理员");

    public static final UserBizException REACH_SIGN_IN_FAIL_TIMES_PRE_DAY = new UserBizException(2010026, "该账户登录密码错误已达5次，账户已被锁定");

    public static final UserBizException REACH_SIGN_IN_BY_CODE_FAIL_TIMES_PRE_DAY = new UserBizException(2010027, "该账户登录验证码错误已达5次，账户已被锁定");

    public static final UserBizException REACH_RESET_PASSWORD_FAIL_TIMES_PRE_DAY = new UserBizException(2010028, "该账户重置密码验证码错误已达5次，账户已被锁定");

    public static final UserBizException REACH_CHANGE_PASSWORD_FAIL_TIMES_PRE_DAY = new UserBizException(2010029, "该账户修改密码错误已达5次，账户已被锁定");

    public static final UserBizException REACH_CHANGE_PHONE_FAIL_TIMES_PRE_DAY = new UserBizException(2010030, "该账户修改手机号错误已达5次，账户已被锁定");

    public static final UserBizException ADMIN_EXIST = new UserBizException(2010031, "该物业区域管理员已存在，无法重复申请");

    public static final UserBizException CAMERA_BRAND_INVALID = new UserBizException(2010034, "无效的厂商编号");

    public static final UserBizException NOT_CAMERA_BRAND = new UserBizException(2010035, "厂商编号不能为空");

    public static final UserBizException PROPRIETOR_RELEASE_ONLY = new UserBizException(2010036, "只能由业主解绑用户");

    public static final UserBizException SAME_DATA = new UserBizException(2010037, "数据一致，无需更新");

    public static final UserBizException STATUS_INVALID = new UserBizException(2010038, "无效的状态");

    public static final UserBizException DATA_ALREADY_EXIST = new UserBizException(2010039, "数据已存在");

    public static final UserBizException PARAM_INVALID = new UserBizException(2010040, "无效的参数");

    public static final UserBizException PROPERTY_NOT_EXIST = new UserBizException(2010041, "物业人员不存在");

    public static final UserBizException SAME_ROLE_USER_EXIST = new UserBizException(2010043, "同一用户不能重复新增相同岗位");

    public static final UserBizException ROOMS_AUTH_NOT_EXIST = new UserBizException(2010044, "缺少房间认证，请联系管理员");

    public static final UserBizException BIZCODE_INVALID = new UserBizException(2010045, "无效的业务类型");

    public static final UserBizException IDENTITY_INVALID = new UserBizException(2010046, "请选择正确的身份");

    public static final UserBizException INVALID_BIRTHDAY = new UserBizException(2010047, "出生日期必须小于当前时间");

    public static final UserBizException SIGN_IN_FAILURE = new UserBizException(2010048, "账号或密码错误，登录失败");

    public static final UserBizException ALREADY_IN_COMMON_USE = new UserBizException(2010049, "已经是常住房间，无需重复设置");

    public static final UserBizException EMPLOYEE_PHONE_REGISTERED = new UserBizException(2010051,
            "手机号已被当前企业注册");
    public static final UserBizException EMPLOYEE_NOT_EXIST = new UserBizException(2010053,
            "员工不存在");
    public static final UserBizException WAITING_USER_REGISTER = new UserBizException(2010054,
            "用户已拥有一个岗位，需要等待用户注册后，才能添加更多的岗位");
    public static final UserBizException EMPLOYEE_NOT_REGISTER = new UserBizException(2010055, "企业员工未注册");
    public static final UserBizException EMPLOYEE_NO_EXIST = new UserBizException(2010056, "工号已存在");
    public static final UserBizException COMPANY_NOT_EXIST = new UserBizException(2010057, "企业不存在");
    public static final UserBizException EMPLOYEE_EXIST = new UserBizException(2010058, "企业员工已经存在，请从企业员工列表新增该物业人员");

    public static final UserBizException HOUSEHOLD_MISSING_PARAM = new UserBizException(2010059, "缺失参数");
    public static final UserBizException HOUSEHOLD_EXIST = new UserBizException(2010060, "住户档案已存在");
    public static final UserBizException INPUT_INFO_ERROR = new UserBizException(2010061, "录入信息错误，有且只能有一个业主");
    public static final UserBizException CONTACTS_OVER_MAX = new UserBizException(2010062, "紧急联系人最多只能填写3个");

    public static final UserBizException PHONE_ILLEGAL = new UserBizException(2010063, "手机号码格式有误");
    public static final UserBizException HOUSEHOLD_NOT_EXIST = new UserBizException(2010064, "住户档案不存在");
    public static final UserBizException HOUSEHOLD_USERNAME_REPEAT = new UserBizException(2010065, "住户姓名重复");
    public static final UserBizException HOUSEHOLD_USERNAME_ILLEGAL = new UserBizException(2010066, "姓名最大长度为16");

    public static final UserBizException DEVICE_LISENCE_CANCEL = new UserBizException(2010067, "设备许可已取消");

    public UserBizException(int code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public UserBizException(int code, String msg) {
        super(code, msg);
    }

    public UserBizException() {
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public UserBizException newInstance(String msgFormat, Object... args) {
        return new UserBizException(this.code, msgFormat, args);
    }

    public UserBizException print() {
        log.info(" ==> BizException, code:" + this.code + ", msg:" + this.msg);
        return new UserBizException(this.code, this.msg);
    }
}
