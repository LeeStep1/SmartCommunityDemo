package cn.bit.facade.service.system;

import cn.bit.facade.model.system.Role;
import cn.bit.framework.data.common.Page;

/**
 * Created by Administrator on 2018/2/4 0004.
 */
public interface RoleFacade {

    Role addRole(Role role);

    Page<Role> findPage(int page, int size);

    void removeRole(String roleKey);

    void addPermission2Role(String roleKey, String permissionKey);


}
