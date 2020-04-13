package cn.bit.facade.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class IdsRequestVO implements Serializable {

    private Set<String> ids;
}
