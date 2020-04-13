package cn.bit.facade.vo.user.mili;

import java.io.Serializable;

/**
 * 楼宇
 */
public class MiliBuilding implements Serializable {

    /**
     * ID
     */
    private Long id;

    /**
     * 楼宇名称
     */
    private String name;

    /**
     * 楼宇编号
     */
    private String code;

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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
