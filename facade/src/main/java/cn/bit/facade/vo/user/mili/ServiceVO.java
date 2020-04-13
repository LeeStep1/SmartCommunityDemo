package cn.bit.facade.vo.user.mili;

/**
 * 社区服务
 */
public class ServiceVO {

    private Long id;

    private Long yun_community_id;

    private String name;

    /**
     * 坐标
     */
    private Location localtion;
    /**
     * 小区开通的平台服务
     */
    private Service service;
    /**
     * 服务项目
     */
    private Item item;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getYun_community_id() {
        return yun_community_id;
    }

    public void setYun_community_id(Long yun_community_id) {
        this.yun_community_id = yun_community_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Location getLocaltion() {
        return localtion;
    }

    public void setLocaltion(Location localtion) {
        this.localtion = localtion;
    }
}
