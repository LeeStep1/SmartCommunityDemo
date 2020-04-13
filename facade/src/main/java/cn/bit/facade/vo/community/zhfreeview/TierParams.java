package cn.bit.facade.vo.community.zhfreeview;

import lombok.Data;

import java.io.Serializable;

@Data
public class TierParams implements Serializable {

    private String tenantCode;

    private String parentDirectory;

    private Integer nodeNum;

    private String nodeDisplay;

    private Integer nodeNumStart;

}
