package cn.bit.facade.vo.user.mili;

import java.io.Serializable;

/**
 * 社区
 */
public class MiliCommunity implements Serializable{

    /**
     * ID
     */
    private Long id;

    /**
     * 社区名称
     */
    private String name;

    /**
     * 云对讲小区ID
     */
    private Long yun_community_id;

    /**
     * 联系人
     */
    private String contact_name;

    /**
     * 联系电话
     */
    private String tel;

    /**
     * 小区状态
     */
    private Integer state;

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

    public Long getYun_community_id() {
        return yun_community_id;
    }

    public void setYun_community_id(Long yun_community_id) {
        this.yun_community_id = yun_community_id;
    }

    public String getContact_name() {
        return contact_name;
    }

    public void setContact_name(String contact_name) {
        this.contact_name = contact_name;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }
}
