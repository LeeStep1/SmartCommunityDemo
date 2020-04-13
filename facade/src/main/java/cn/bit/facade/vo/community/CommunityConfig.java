package cn.bit.facade.vo.community;

import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
public class CommunityConfig implements Serializable {

    private Set<String> menus;

    private Set<String> roles;

}
