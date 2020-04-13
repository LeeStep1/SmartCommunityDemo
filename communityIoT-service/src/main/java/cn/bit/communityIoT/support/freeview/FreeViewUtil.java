package cn.bit.communityIoT.support.freeview;

import cn.bit.framework.utils.string.StringUtil;

/**
 * @Description : 全视通协议工具类
 * @Author : xiaoxi.lao
 * @Date ： 20180613 20点09分
 * @version 1.0
 */
public class FreeViewUtil {
    public static String getCheckSUMByte(String cmd, String data) {
        int sum = 0;
        sum += Integer.valueOf(cmd, 16);
        if (data != null) {
            for (int i = 0; i < data.length() / 2; i++) {
                sum += Integer.valueOf(data.substring(i * 2, (i + 1) * 2), 16);
            }
        }

        return getLeastByteStr(Integer.toHexString(sum));
    }

    private static String getLeastByteStr(String value) {
        if (!StringUtil.isEmpty(value)) {
            return value.length() > 2 ? value.substring(value.length() - 2) : value;
        } else {
            return "00";
        }
    }
}
