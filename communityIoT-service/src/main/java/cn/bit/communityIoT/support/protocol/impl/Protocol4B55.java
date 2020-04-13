package cn.bit.communityIoT.support.protocol.impl;

import cn.bit.communityIoT.support.protocol.DeviceProtocol;
import cn.bit.communityIoT.support.protocol.data.Message;
import cn.bit.communityIoT.support.protocol.data.Message4B55;
import cn.bit.communityIoT.support.protocol.data.MsgHouse;
import cn.bit.framework.utils.DateUtils;
import freemarker.template.utility.StringUtil;
import lombok.Data;
import org.apache.http.util.TextUtils;

import java.util.Date;
import java.util.List;

/**
 * @author decai.liu
 * @desc 设备离线协议
 * @date 2018-07-05 15:26
 */

@Data
public class Protocol4B55 implements DeviceProtocol {

    private static final String MSG_HEAD = "4B55";
    private final static String ZERO = "0";
    private final static String FORMAT_YMDHM = "yyMMddHHmm";
    private final static String COMMA = ",";

    @Override
    public String encode(Message message) {
        return getProtocolString((Message4B55) message);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    private String getProtocolString(Message4B55 message) {
        StringBuffer sb = new StringBuffer(MSG_HEAD);

        /**
         * 协议类型
         * 00临时通行协议，01区域管理员协议，02单元管理员协议，03楼栋管理员协议，04房屋信息协议，05读取设备MAC，
         * 06终端id写入协议，07广播前时间刷新协议，09小区管理员协议, 10~14 IC卡离线通行协议
         * 90禁用房间号协议，91开启已禁用过的房间号协议，128刷新禁用梯控时间协议
         */
        Integer msgCMD = message.getMsgCMD();
        if (msgCMD == null) {
            return null;
        }
        // 读取设备MAC
        if (msgCMD == 5) {
            // 长度9
            sb.append("09");
            sb.append("05");
            sb.append("00000000");
            sb.append("05");
            return sb.toString();
        }
        // 广播前时间刷新协议
        if (msgCMD == 7) {
            // 长度11
            sb.append("0B");
            // 类型
            sb.append("07");
            // 和校验
            sb.append("07");
            return sb.toString();
        }
        // IC卡离线通行协议
		/*
		楼层：0x0A  10
		小区：0x0B  11
		区域：0x0C  12
		单元：0x0D  13
		楼栋：0x0E  14
		*/
        if (msgCMD >= 10 && msgCMD <= 14) {
            return icCardProtocol(message);
        }
        // 物业禁卡、解除禁卡
        if (msgCMD == 80 || msgCMD == 81) {
            return banCardProtocol(message);
        }
        if (msgCMD == 128) {
            return liftControlProtocol(message);
        }
        /**
         * 区域
         */
        List<MsgHouse> msgHouses = message.getMsgHouses();

        /**
         * 小区id
         */
        Integer msgCID = message.getMsgCID();

        /**
         * 节点信息，终端id写入使用
         * 00为小区设备,01为区域设备,02为单元设备,03为楼栋设备
         */
        Integer msgType = message.getMsgType();

        /**
         * 广播系统类型
         * 1：ios广播，2：安卓广播，6：蓝牙卡广播
         */
        Integer msgSys = message.getMsgSys();

        /**
         * 协议有效时间，临时通行使用
         */
        Date msgTime = message.getMsgTime();

        sb.append(getMsgLengthAndCMDOrSys(msgCMD, msgSys, msgHouses.size()));
        // 终端id写入
        if (msgCMD == 6 && msgType != null) {
            sb.append(change10to16(msgType, 2));
        }
        sb.append(littleEndian(msgCID));

        if (!msgHouses.isEmpty()) {
            sb.append(getMsgHouse(msgHouses, msgCMD));
        }
        // 临时通行
        if (msgCMD == 0 && msgTime != null) {
            sb.append(getMsgTime(msgTime));
        }
        // 和校验
        sb.append(getCheckSUMByte(sb.toString().substring(6)));
        return sb.toString().toUpperCase();
    }

    /**
     * 梯控时间协议
     *
     * @param message
     * @return
     */
    private String liftControlProtocol(Message4B55 message) {
        Integer msgCMD = message.getMsgCMD();
        StringBuffer block1 = new StringBuffer(MSG_HEAD);
        block1.append(change10to16(msgCMD, 2));

        /**
         * 区域
         */
        MsgHouse msgHouse = message.getMsgHouses().get(0);

        /**
         * 小区id
         */
        Integer msgCID = message.getMsgCID();

        StringBuffer sum = new StringBuffer();
        sum.append(littleEndian(msgCID));
        sum.append(change10to16(msgHouse.getUnitId(), 1));
        sum.append(change10to16(msgHouse.getDistrictId(), 1));
        sum.append(change10to16(msgHouse.getBuildingId(), 2));
        block1.append(sum);
        StringBuffer floor = new StringBuffer(change10to16(0, 18));
        block1.append(floor);

        StringBuffer block2 = new StringBuffer(MSG_HEAD);
        block2.append(change10to16(msgCMD, 2));
        StringBuffer from = new StringBuffer(getMsgTime(message.getMsgTime()));
        block2.append(from);
        StringBuffer to = new StringBuffer(getMsgTime(message.getMsgTimeTo()));
        block2.append(to);
        // byte 13 补0
        block2.append(change10to16(0, 2));
        // 楼层跟时间的异或校验
        block2.append(getBCC(toByteArray(floor.toString() + from.toString() + to.toString())));
        // 社区id 到楼栋id 的和校验
        block2.append(getCheckSUMByte(sum.toString()));
        // 逗号分隔两个block
        return block1.append(",").append(block2).toString().toUpperCase();
    }

    /**
     * 物业房间禁卡、解除房间禁卡协议
     *
     * @param message
     * @return
     */
    private String banCardProtocol(Message4B55 message) {
        Integer msgCMD = message.getMsgCMD();
        StringBuffer block1 = new StringBuffer(MSG_HEAD);
        block1.append(change10to16(msgCMD, 2));

        /**
         * 区域
         */
        MsgHouse msgHouse = message.getMsgHouses().get(0);

        /**
         * 小区id
         */
        Integer msgCID = message.getMsgCID();
        StringBuffer sum = new StringBuffer();
        sum.append(littleEndian(msgCID));
        sum.append(change10to16(msgHouse.getUnitId(), 1));
        sum.append(change10to16(msgHouse.getDistrictId(), 1));
        sum.append(change10to16(msgHouse.getBuildingId(), 2));
        block1.append(sum);
        // byte7~15 补0
        block1.append(change10to16(0, 18));
        // block2 不需要写数据
        return block1.toString();
    }

    /**
     * IC卡通行协议
     *
     * @param message
     * @return
     */
    private String icCardProtocol(Message4B55 message) {
        Integer msgCMD = message.getMsgCMD();
        StringBuffer block1 = new StringBuffer(MSG_HEAD);
        block1.append(change10to16(msgCMD, 2));

        /**
         * 区域
         */
        MsgHouse msgHouse = message.getMsgHouses().get(0);

        /**
         * 小区id
         */
        Integer msgCID = message.getMsgCID();
        StringBuffer sum = new StringBuffer();
        sum.append(littleEndian(msgCID));
        sum.append(change10to16(msgHouse.getUnitId(), 1));
        sum.append(change10to16(msgHouse.getDistrictId(), 1));
        sum.append(change10to16(msgHouse.getBuildingId(), 2));
        block1.append(sum);
        StringBuffer floor = new StringBuffer(getFloor(msgHouse.getFloorId(), message.getMsgType()));
        block1.append(floor);
        // 新增房间号(floorNo + roomNo) at 20190508 by decai
//		block1.append(change10to16(0, 6));
        // 不参与异或校验
        block1.append(StringUtil.leftPad(msgHouse.getFloorId() + "", 2, ZERO));
        block1.append(StringUtil.leftPad(msgHouse.getRoomId() + "", 2, ZERO));
        block1.append(change10to16(0, 2));

        StringBuffer block2 = new StringBuffer(MSG_HEAD);
        block2.append(change10to16(msgCMD, 2));
        StringBuffer time = new StringBuffer(getMsgTime(message.getMsgTime()));
        time.append(change10to16(0, 12));
        block2.append(time);
        // 楼层跟时间的异或校验
        block2.append(getBCC(toByteArray(floor.toString() + time.toString())));
        // 社区id到楼栋id的和校验
        block2.append(getCheckSUMByte(sum.toString()));
        // 逗号分隔两个block
        return block1.append(",").append(block2).toString().toUpperCase();
    }

    // 16进制字符串转成byte[]
    private static byte[] toByteArray(String hexString) {
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        // 因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
        for (int i = 0; i < byteArray.length; i++) {
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }

    /**
     * BCC 异或校验
     *
     * @param data
     * @return
     */
    private static String getBCC(byte[] data) {
        String ret = "";
        byte BCC[] = new byte[1];
        for (int i = 0; i < data.length; i++) {
            BCC[0] = (byte) (BCC[0] ^ data[i]);
        }
        String hex = Integer.toHexString(BCC[0] & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        ret += hex.toUpperCase();
        return ret;
    }

    private static String getFloor(int floorId, Integer msgType) {
        // 最高支持48层，住户在哪一层就点亮那层
        if (msgType == 1) {
            long floorBitCode = 0;
            int x = floorId / 8;
            int y = floorId % 8;
            if (y == 0) {
                floorBitCode = (floorBitCode | (1L << (48 - 8 * (x - 1) - 1)));
            } else {
                floorBitCode = (floorBitCode | (1L << (48 - 8 * (x + 1) + (y - 1))));
            }

            return String.format("%012x", floorBitCode);
        } else {
            return "FFFFFFFFFFFF";
        }
    }

    private static String getMsgTime(Date msgTime) {
        return DateUtils.formatDate(msgTime, FORMAT_YMDHM);
    }

    private static String getMsgHouse(List<MsgHouse> msgHouses, int msgCMD) {
        // 临时通行协议
        if (msgCMD == 0) {
            MsgHouse msgHouse = msgHouses.get(0);
            return getHouse(msgHouse, msgCMD);
        }
        StringBuffer sb = new StringBuffer();
        msgHouses.forEach(msgHouse -> {
            sb.append(getHouse(msgHouse, msgCMD));
        });
        return sb.toString();
    }

    private static String getHouse(MsgHouse msgHouse, Integer msgCMD) {
        if (msgCMD == null) {
            msgCMD = 0;
        }
        StringBuffer sb = new StringBuffer();
        if (msgCMD == 0 || msgCMD == 2 || msgCMD == 3 || msgCMD == 4 || msgCMD == 6) {
            // 单元管理员协议(5~8字节)
            sb.append(change10to16(msgHouse.getUnitId(), 1));
        }
        // 区域管理员协议(1~4字节)
        if (msgCMD == 1) {
            // 高位补0，补足一个byte
            sb.append(ZERO);
        }
        sb.append(change10to16(msgHouse.getDistrictId(), 1));

        // 后面2byte不需要按高低位顺序调节
        if (msgCMD == 0 || msgCMD == 3 || msgCMD == 4 || msgCMD == 6) {
            // 楼栋管理员协议(9~16字节)
            sb.append(change10to16(msgHouse.getBuildingId(), 2));
        }
        if (msgCMD == 0 || msgCMD == 4) {
            // 房屋信息协议(17~24字节)
            sb.append(change10to16(msgHouse.getFloorId(), 2));
        }
        return sb.toString();
    }

    // 10进制转16进制补位取得字符串
    private static String change10to16(Integer decimal, int padNum) {
        return StringUtil.leftPad(Integer.toHexString(decimal == null ? 0 : decimal), padNum, ZERO);
    }

    // 根据协议类型转换成16进制字符串
    private static String getMsgLengthAndCMDOrSys(Integer msgCMD, Integer msgSys, int houseSize) {
        // 临时通行协议
        if (msgCMD == 0) {
            return "0F00";
        }

        // 协议能确定的长度
        int temp = 7;
        Integer length = 0;
        switch (msgCMD) {
            // 区域管理协议 高位补0，补足一个byte
            case 1:
                // 单元管理协议
            case 2:
                length = houseSize + temp + 1;
                break;
            // 楼栋管理协议
            case 3:
                length = houseSize * 2 + temp + 1;
                break;
            // 房屋信息协议
            case 4:
                length = houseSize * 3 + temp + 1;
                break;
            // 终端id写入协议,msgType占一个byte
            case 6:
                length = houseSize * 2 + temp + 1;
                break;
            // 小区管理员协议
            case 9:
                length = temp + 1;
                break;
            default:
                break;
        }

        if (length == 0) {
            throw new RuntimeException(msgCMD + "_协议类型不存在");
        }
        String length16 = Integer.toHexString(length);
        String msgSysString = "";
        if (msgSys == null) {
            msgSys = -1;
        }
        switch (msgSys) {
            // IOS广播
            case 1:
                msgSysString = "1B";
                break;
            // 安卓广播
            case 2:
                msgSysString = "0B";
                break;
            // 蓝牙卡广播
            case 6:
                msgSysString = "0C";
                break;
            default:
                break;
        }
        return StringUtil.leftPad(length16, 2, ZERO) +
                StringUtil.leftPad(msgCMD.toString(), 2, ZERO) + msgSysString;
    }

    // 和校验
    private static String getCheckSUMByte(String data) {
        int sum = 0;
        if (!TextUtils.isEmpty(data)) {
            for (int i = 0; i < data.length() / 2; i++) {
                sum += Integer.valueOf(data.substring(i * 2, (i + 1) * 2), 16);
            }
        }
        String sumStr = getLowestByteStr(Integer.toHexString(sum));
        return sumStr;
    }

    // 超过256的溢出部分舍去
    private static String getLowestByteStr(String value) {
        if (TextUtils.isEmpty(value)) {
            return ZERO + ZERO;
        }
        if (value.length() > 2) {
            return value.substring(value.length() - 2);
        }
        if (value.length() == 1) {
            return ZERO + value;
        }
        return value;
    }

    /**
     * 小端
     *
     * @param var0
     * @return
     */
    private static String littleEndian(int var0) {
        int var1 = 1;
        int var2 = var0 >> 8;
        int var3 = var0 & 255;
        String var4 = Integer.toHexString(var2);
        String var5 = Integer.toHexString(var3);
        if (var4.length() > 2) {
            do {
                if (var1 > 1) {
                    var2 >>= 8;
                }
                var4 = Integer.toHexString(var2 >> 8);
                var5 = var5 + Integer.toHexString(var2 & 255);
                ++var1;
            } while (var4.length() > 2);
        }
        if (var4.length() < 2) {
            var4 = ZERO + var4;
        }
        if (var5.length() < 2) {
            var5 = ZERO + var5;
        }
        return var5 + var4;
    }
}
