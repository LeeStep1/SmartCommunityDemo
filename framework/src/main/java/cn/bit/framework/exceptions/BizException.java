package cn.bit.framework.exceptions;

/**
 * 业务异常基类，所有业务异常都必须继承于此异常
 *
 * @author healy
 *         <p>
 *         定义异常时，需要先确定异常所属模块。例如：账户模块报错 可以定义为 [5020001] 前三位数为模块编号，后4位为错误代码 ,唯一 <br>
 */
public class BizException extends RuntimeException {

    private static final long serialVersionUID = -5875371379845226068L;

    /*public static int EX_TYPE_BAD_REQUEST = 400;
    public static int EX_TYPE_NOT_FOUND = 404;
    public static int EX_TYPE_UNAUTHORIZED = 401;
    public static int EX_TYPE_INTERNAL_ERROR = 500;*/

    /**
     * 数据库操作,insert返回0
     */
    public static final BizException DB_INSERT_RESULT_0 = new BizException(9040001, "数据库操作," +
            "insert返回0");

    /**
     * 数据库操作,update返回0
     */
    public static final BizException DB_UPDATE_RESULT_0 = new BizException(9040002, "数据库操作," +
            "update返回0");

    /**
     * 数据库操作,selectOne返回null
     */
    public static final BizException DB_SELECTONE_IS_NULL = new BizException(9040003,
            "数据库操作,selectOne返回null");

    /**
     * 数据库操作,delete返回null
     */
    public static final BizException DB_DELETE_RESULT_0 = new BizException(9040004, "数据库操作," +
            "delete返回0");

    /**
     * 数据库操作,list返回null
     */
    public static final BizException DB_LIST_IS_NULL = new BizException(9040005, "数据库操作," +
            "list返回null");

    /**
     * Token 验证不通过
     */
    public static final BizException TOKEN_IS_ILLICIT = new BizException(9040006, "Token " +
            "验证非法");

    /**
     * 会话超时　获取session时，如果是空，throws 下面这个异常 拦截器会拦截爆会话超时页面
     */
    public static final BizException SESSION_IS_OUT_TIME = new BizException(9040007, "会话超时");

    /**
     * 获取序列出错
     */
    public static final BizException DB_GET_SEQ_NEXT_VALUE_ERROR = new BizException(9040008,
            "获取序列出错");

    /**
     * 获取序列出错
     */
    public static final BizException DB_GET_FIELD_ERROR = new BizException(9040009, "获取字段出错");

    /**
     * token无效
     */
    public static final BizException TOKEN_INVALID = new BizException(9050001, "token失效,请重新登陆");

    /**
     * header 无效
     */
    public static final BizException HEADER_INVALID = new BizException(9050002, "无效的头部参数");

    public static final BizException OPERATION_FAILURE = new BizException(9050003, "操作失败");

    /**
     * 异常信息
     */
    protected String msg;

    /**
     * 具体异常码
     */
    protected int code;

    protected int type;

    public BizException(int code, String msgFormat, Object... args) {
        super(String.format(msgFormat, args));
        this.code = code;
        this.msg = String.format(msgFormat, args);
    }

    public BizException() {
        super();
    }

    public String getMsg() {
        return msg;
    }

    public int getCode() {
        return code;
    }

    public int getType() {
        return type;
    }

    /**
     * 实例化异常
     *
     * @param msgFormat
     * @param args
     * @return
     */
    public BizException newInstance(String msgFormat, Object... args) {
        return new BizException(this.code, msgFormat, args);
    }

    public BizException(String message, Throwable cause) {
        super(message, cause);
    }

    public BizException(Throwable cause) {
        super(cause);
    }

    public BizException(String message) {
        super(message);
    }
}
