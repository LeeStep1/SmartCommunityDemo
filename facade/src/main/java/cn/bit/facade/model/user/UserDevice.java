package cn.bit.facade.model.user;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class UserDevice implements Serializable {

    /**
     * 设备唯一标识
     */
    private String deviceId;
    /**
     * 设备型号
     */
    private String deviceType;
    /**
     * 设备系统（1：iOS；2：Android；3：h5；4：iPad；5：AndroidPad）
     */
    private Integer os;
    /**
     * 设备系统版本
     */
    private String osVersion;
    /**
     * 设备蓝牙地址
     */
    private String bdaddr;

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof UserDevice)) {
            return false;
        }
        UserDevice userDevice = (UserDevice) o;
        return Objects.equals(deviceId, userDevice.deviceId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceId);
    }

}
