package cn.bit.facade.vo.user;

import java.io.Serializable;

/**
 * 设备运行状态
 */
public class DeviceData implements Serializable {
    /**
     * 服务id
     */
    private Long id;
    /**
     * 服务名称
     */
    private String service_name;
    /**
     * 使用次数统计
     */
    private String use_count;
    /**
     * 使用失败次数统计
     */
    private String se_fail_count;
    /**
     * 异常次数统计
     */
    private String abnormal_count;
    /**
     * 最后正常使用时间戳
     */
    private String last_use_time;
    /**
     * 设备当前状态
     */
    private String status;
    /**
     * 异常数据
     */
    private Abnormal abnormal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getService_name() {
        return service_name;
    }

    public void setService_name(String service_name) {
        this.service_name = service_name;
    }

    public String getUse_count() {
        return use_count;
    }

    public void setUse_count(String use_count) {
        this.use_count = use_count;
    }

    public String getSe_fail_count() {
        return se_fail_count;
    }

    public void setSe_fail_count(String se_fail_count) {
        this.se_fail_count = se_fail_count;
    }

    public String getAbnormal_count() {
        return abnormal_count;
    }

    public void setAbnormal_count(String abnormal_count) {
        this.abnormal_count = abnormal_count;
    }

    public String getLast_use_time() {
        return last_use_time;
    }

    public void setLast_use_time(String last_use_time) {
        this.last_use_time = last_use_time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Abnormal getAbnormal() {
        return abnormal;
    }

    public void setAbnormal(Abnormal abnormal) {
        this.abnormal = abnormal;
    }
}
