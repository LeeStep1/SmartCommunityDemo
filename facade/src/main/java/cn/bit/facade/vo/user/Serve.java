package cn.bit.facade.vo.user;

import java.util.List;

public class Serve {

    /**
     * 服务ID
     */
    private Long id;

    private String name;

    private int status;

    private List<Device> device;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public List<Device> getDevice() {
        return device;
    }

    public void setDevice(List<Device> device) {
        this.device = device;
    }
}
