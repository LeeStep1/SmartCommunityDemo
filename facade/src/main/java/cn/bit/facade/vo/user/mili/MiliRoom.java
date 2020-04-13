package cn.bit.facade.vo.user.mili;

import java.io.Serializable;

/**
 * 房间
 */
public class MiliRoom implements Serializable{
    /**
     * ID
     */
    private Long id;
    /**
     * 云对讲小区ID
     */
    private Long yun_community_id;
    /**
     * 小区名称
     */
    private String name;

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
}
