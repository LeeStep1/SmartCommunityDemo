package cn.bit.facade.vo.vehicle;

import lombok.Data;

import java.io.Serializable;

@Data
public class CarRequest implements Serializable {
    private String carNo;
    private Integer auditStatus;
}
