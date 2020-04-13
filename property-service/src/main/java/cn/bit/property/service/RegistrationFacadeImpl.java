package cn.bit.property.service;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.property.Registration;
import cn.bit.facade.service.property.RegistrationFacade;
import cn.bit.framework.exceptions.BizException;
import cn.bit.property.dao.RegistrationRepository;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component("registrationFacade")
@Slf4j
public class RegistrationFacadeImpl implements RegistrationFacade {

    @Autowired
    private RegistrationRepository registrationRepository;

    /**
     * 登记员工信息
     *
     * @param registration
     * @return
     * @throws BizException
     */
    @Override
    public Registration addRegistration(Registration registration) throws BizException {
        registration.setDataStatus(DataStatusType.VALID.KEY);
        registration.setCreateAt(new Date());
        registration.setUpdateAt(registration.getCreateAt());
        return registrationRepository.upsertWithAddToSetRolesThenSetOnInsertCreateAtByCommunityIdAndPhoneAndDataStatus(
                registration, registration.getCommunityId(),registration.getPhone(), DataStatusType.VALID.KEY);
    }

    /**
     * 根据手机号查询登记信息
     *
     * @param phone
     * @return
     * @throws BizException
     */
    @Override
    public List<Registration> listRegistrationsByPartnerAndPhone(Integer partner, String phone) throws BizException {
        return registrationRepository.findByPartnerAndPhoneAndDataStatus(partner, phone, DataStatusType.VALID.KEY);
    }

    /**
     * 根据id集合批量删除员工登记信息
     *
     * @param ids
     * @return
     * @throws BizException
     */
    @Override
    public void deleteRegistrationByIds(Set<ObjectId> ids) {
        if(ids == null || ids.isEmpty()){
            log.info("没有需要删除的ids");
            return;
        }
        Registration toDelete = new Registration();
        toDelete.setDataStatus(DataStatusType.INVALID.KEY);
        toDelete.setUpdateAt(new Date());
        registrationRepository.updateByIdIn(toDelete, ids);
    }
}
