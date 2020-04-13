package cn.bit.facade.service.property;

import cn.bit.facade.model.property.Registration;
import cn.bit.framework.exceptions.BizException;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Set;

public interface RegistrationFacade
{
    //---------------------------------------物业员工登记-----------------------------------------------------------

    /**
     * 登记员工信息
     *
     * @param registration
     * @return
     * @throws BizException
     */
    Registration addRegistration(Registration registration) throws BizException;

    /**
     * 根据手机号查询登记信息
     *
     * @param phone
     * @return
     * @throws BizException
     */
    List<Registration> listRegistrationsByPartnerAndPhone(Integer partner, String phone) throws BizException;

    /**
     * 根据id集合批量删除员工登记信息
     *
     * @param registrationIds
     * @return
     * @throws BizException
     */
	void deleteRegistrationByIds(Set<ObjectId> registrationIds);
}
