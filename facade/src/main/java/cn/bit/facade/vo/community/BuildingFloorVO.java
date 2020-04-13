package cn.bit.facade.vo.community;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

/**
 * @author xiaoxi.lao
 * @Description :
 * @Date ： 2018/11/8 10:20
 */
@Data
public class BuildingFloorVO implements Serializable {
    /**
     * 名称
     */
    private String name;
    /**
     * 号码（物理层号，线性递增，增幅为1）
     */
    private Integer no;
    /**
     * 楼栋ID
     */
    private ObjectId buildingId;
    /**
     * 是否公共的
     */
    private Boolean communal;
}
