package cn.bit.facade.vo.trade;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class CommunityTradePlatformVO implements Serializable {

    private Set<Integer> platforms;

}
