package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;

/**
 * 位长
 */
@Data
public class BitlongerParams implements Serializable {

    private String TenantCode;

    private Integer StructureID;

    private Integer Building = 3;

    private Integer Floor = 3;

    private Integer Room = 3;

}
