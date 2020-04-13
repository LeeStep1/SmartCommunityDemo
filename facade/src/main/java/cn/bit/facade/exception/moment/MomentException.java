package cn.bit.facade.exception.moment;

import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by decai.liu
 * on 2018/4/13
 */
@Slf4j
public class MomentException extends BizException {

    public static final MomentException MOMENT_ID_IS_NULL = new MomentException(1320001, "动态ID不能为空");

    public static final MomentException DATA_NOT_EXIST = new MomentException(1320002, "数据不存在");

    public static final MomentException MOMENT_TYPE_IS_NULL = new MomentException(1320003, "动态类型不能为空");

    public static final MomentException CONTENT_IS_NULL = new MomentException(1320004, "内容不能为空");

    public static final MomentException COMMENT_ID_IS_NULL = new MomentException(1320005, "评论ID不能为空");

    public static final MomentException TYPE_IS_NULL = new MomentException(1320006, "言论类型不能为空");

    public static final MomentException REASON_IS_NULL = new MomentException(1320007, "原因不能为空");

    public static final MomentException AUDIT_STATUS_IS_NULL = new MomentException(1320008, "审核状态不能为空");

    public static final MomentException CAN_NOT_DELETE = new MomentException(1320009, "不能删除他人言论");

    public static final MomentException CAN_NOT_REPORT_ONESELF = new MomentException(1320010, "只能举报他人言论");

    public static final MomentException REPORT_LIMIT = new MomentException(1320011, "举报次数已达上限");

    public static final MomentException ALREADY_REPORTED = new MomentException(1320012, "已经举报过此言论");

    public static final MomentException PHOTO_COUNT_OUTOFRANGE = new MomentException(1320013, "照片数量过多");

    public static final MomentException ILLEGAL_PARAMETER = new MomentException(1320014, "请求参数错误");

    public static final MomentException SILENT_USER_ID_IS_NULL = new MomentException(1320016, "被禁言的用户ID为空");

    public static final MomentException SPEECH_ID_IS_NULL = new MomentException(1320017, "言论ID不能为空");

    public static final MomentException SILENT_MINUTES_IS_NULL = new MomentException(1320018, "禁言有效时长不能为空");

    public static final MomentException SILENT_ID_IS_NULL = new MomentException(1320019, "禁言记录ID为空");
    public static final MomentException DATA_SORT_IS_NULL = new MomentException(1320021, "数据排序方式不能为空");
    public static final MomentException LENGTH_OF_CONTENT_TO_LARGE200 = new MomentException(1320022, "内容长度超过200字符");
    public static final MomentException SENSITIVE_WORDS_EXIST = new MomentException(1320023, "言论内容不符合社区文明规范，请检查");
    public static final MomentException MOMENT_IS_DELETED = new MomentException(1320024, "该动态已经删除");
    public static final MomentException COMMENT_IS_DELETED = new MomentException(1320025, "该评论已经删除");
    public static final MomentException LENGTH_OF_CONTENT_TO_LARGE500 = new MomentException(1320026, "内容长度超过500字符");

    public MomentException(Integer code, String msgFormat, Object... args) {
        super(code, msgFormat, args);
    }

    public MomentException(Integer code, String msgFormat) {
        super(code, msgFormat);
    }

    public MomentException() {
        super();
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public MomentException newInstance(String msgFormat, Object... args) {
        return new MomentException(this.code, msgFormat, args);
    }

    public MomentException print() {
        log.info(" ==> MomentException, code:" + this.code + ", msg:" + this.msg);
        return new MomentException(this.code, this.msg);
    }
}
