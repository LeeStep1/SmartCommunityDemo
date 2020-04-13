package cn.bit.facade.vo.communityIoT.door;

import cn.bit.facade.model.communityIoT.Door;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * Created by xiaoxi.lao
 *
 * @description
 * @create: 2018/3/19
 **/
@NoArgsConstructor
public class DoorInfo implements Serializable {
    /**
     * 门禁信息
     */
    private Door door;
    /**
     * 凭证卡号
     */
    private String keyNo;
    /**
     * 写入硬件是否成功
     */
    private Boolean writeSuccess;

    /**
     * 写入状态
     */
    private Integer writeStatus;

    private String devDigest;

    private String AppDigest;

    private Boolean hasBluetooth;

    public DoorInfo(Door door, String keyNo, Boolean writeSuccess) {
        super();
        this.door = door;
        this.keyNo = keyNo;
        this.writeSuccess = writeSuccess;
    }

    public DoorInfo(Door door, String keyNo, Integer writeStatus) {
        this.door = door;
        this.writeStatus = writeStatus;
    }

    public Boolean isDoorExist() {
        return this.door == null;
    }

    public ObjectId getId() {
        return door.getId();
    }

    public ObjectId getCommunityId() {
        return door.getCommunityId();
    }

    public String getName() {
        return door.getName();
    }

    public ObjectId getBuildingId() {
        return door.getBuildingId();
    }

    public String getMac() {
        return door.getMac();
    }

    public String getPin() {
        return door.getPin();
    }

    public int getRank() {
        return door.getRank();
    }

    public String getTerminalCode() {
        return door.getTerminalCode();
    }

    public Integer getTerminalPort() {
        return door.getTerminalPort();
    }

    public Integer getGuardSwitch() {
        return door.getGuardSwitch();
    }

    public Integer getDoorStatus() {
        return door.getDoorStatus();
    }

    public Integer getDoorType() {
        return door.getDoorType();
    }

    public Long getDeviceId() {
        return door.getDeviceId();
    }

    public String getDeviceName() {
        return door.getDeviceName();
    }

    public String getSerialNo() {
        return door.getSerialNo();
    }

    public Set<Integer> getServiceId() {
        return door.getServiceId();
    }

    public String getDeviceCode() {
        return door.getDeviceCode();
    }

    public Integer getOnlineStatus() {
        return door.getOnlineStatus();
    }

    public Long getYunDeviceId() {
        return door.getYunDeviceId();
    }

    public String getBrand() {
        return door.getBrand();
    }

    public Integer getBrandNo() {
        return door.getBrandNo();
    }

    public ObjectId getCreatorId() {
        return door.getCreatorId();
    }

    public Date getCreateAt() {
        return door.getCreateAt();
    }

    public Date getUpdateAt() {
        return door.getUpdateAt();
    }

    public Integer getDataStatus() {
        return door.getDataStatus();
    }

    public String getKeyNo() {
        return keyNo;
    }

    public Boolean getWriteSuccess() {
        return writeSuccess;
    }

    public Integer getWriteStatus() {
        return writeStatus;
    }

    public String getDevDigest() {
        return devDigest;
    }

    public void setDevDigest(String devDigest) {
        this.devDigest = devDigest;
    }

    public String getAppDigest() {
        return AppDigest;
    }


    public Boolean getHasBluetooth() {
        return hasBluetooth;
    }

    public void setHasBluetooth(Boolean hasBluetooth) {
        this.hasBluetooth = hasBluetooth;
    }

    public void setAppDigest(String appDigest) {
        AppDigest = appDigest;
    }

    public void setMac(String mac) {
        if (this.door == null) {
            this.door = new Door();
        }
        this.door.setMac(mac);
    }

    public void setDeviceCode(String deviceCode) {
        if (this.door == null) {
            this.door = new Door();
        }
        this.door.setDeviceCode(deviceCode);
    }


    public void setWriteStatus(Integer writeStatus) {
        this.writeStatus = writeStatus;
    }

    public void setDoor(Door door) {
        this.door = door;
    }

    public void setDoorId(String doorId) {
        if (this.door == null) {
            this.door = new Door();
        }
        this.door.setId(new ObjectId(doorId));
    }

    public void setTerminalPort(Integer terminalPort) {
        if (this.door == null) {
            this.door = new Door();
        }
        this.door.setTerminalPort(terminalPort);
    }

    public void setTerminalCode(String terminalCode) {
        if (this.door == null) {
            this.door = new Door();
        }
        this.door.setTerminalCode(terminalCode);
    }

    public void setOnlineStatus(Integer onlineStatus) {
        if (this.door == null) {
            this.door = new Door();
        }
        this.door.setOnlineStatus(onlineStatus);
    }

    public void setKeyNo(String keyNo) {
        if (this.door == null) {
            this.door = new Door();
        }
        this.keyNo = keyNo;
    }

    public void setWriteSuccess(Boolean writeSuccess) {
        this.writeSuccess = writeSuccess;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof DoorInfo)) {
            return false;
        }

        DoorInfo doorInfo = (DoorInfo) o;
        return Objects.equals(writeSuccess, doorInfo.writeSuccess)
                && Objects.equals(keyNo, doorInfo.keyNo)
                && Objects.equals(door.getId(), doorInfo.door.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(door.getId(), keyNo, writeSuccess);
    }
}
