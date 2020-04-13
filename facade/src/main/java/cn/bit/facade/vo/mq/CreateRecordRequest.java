package cn.bit.facade.vo.mq;

import cn.bit.framework.utils.string.StringUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author : xiaoxi.lao
 * @Description :
 * @Date ： 2019/9/12 14:19
 */
@Data
public class CreateRecordRequest implements Serializable {
    /**
     * 记录编号 硬件按天记录的唯一id
     */
    private Integer recordId;
    /**
     * 终端号
     */
    private String  terminalCode;

    /**
     * 端口号
     */
    private Integer macId;
    /**
     * 钥匙id
     * 16进制 组合字段
     */
    private String  keyNo;
    /**
     * 刷卡时间
     */
    private Date    accessTime;
    /**
     * 刷卡类型 （目前只有通用梯禁在线版可区分手机蓝牙与蓝牙广播）
     * 1 手机蓝牙
     * 2 蓝牙卡
     * 4 IC卡
     * 8 二维码
     * 16 蓝牙广播
     */
    private Short   type;
    /**
     * 是否通过
     * 1代表有效凭证
     * 0 代表无效凭证
     */
    private Boolean access;

    public void setTerminalCode(String terminalCode) {
        this.terminalCode = leftTrimZero(terminalCode);
    }

    private String leftTrimZero(String keys) {
        // 如果以0开头去除0
        while (StringUtil.isNotBlank(keys) && keys.startsWith("0")) {
            keys = keys.substring(1);
        }
        return keys;
    }

    /**
     * 因为数据库保存的卡号没有补零
     * @param keys
     * @return
     */
    private String rightTrimZero(String keys) {
        // 以0000结尾的卡号去掉0
        if (StringUtil.isNotBlank(keys) && keys.endsWith("0") && "0000".equals(keys.substring(keys.length() - 4))) {
            keys = keys.substring(0, keys.length() - 4);
        }
        return keys;
    }

    public String getKeyNo() {
        return this.rightTrimZero(keyNo);
    }
}
