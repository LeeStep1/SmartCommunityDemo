package cn.bit.property.service;

import cn.bit.common.facade.company.constant.CodeConstants;
import cn.bit.common.facade.company.dto.CommunityCompanyDTO;
import cn.bit.common.facade.company.enums.CompanyTypeEnum;
import cn.bit.common.facade.company.model.Company;
import cn.bit.common.facade.company.model.CompanyToCommunity;
import cn.bit.common.facade.company.service.CompanyFacade;
import cn.bit.facade.data.property.PropertyDTO;
import cn.bit.facade.service.property.PropertyFacade;
import cn.bit.facade.vo.property.Property;
import cn.bit.framework.exceptions.BizException;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static cn.bit.facade.exception.CommonBizException.UNKNOWN_ERROR;
import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_NOT_BIND_PROPERTY;
import static cn.bit.facade.exception.property.PropertyBizException.COMPANY_NOT_EXIST;

@Service("propertyFacade")
@Slf4j
public class PropertyFacadeImpl implements PropertyFacade {
    @Resource
    private CompanyFacade companyFacade;

    @Override
    public Property findOne(ObjectId id) throws BizException{
        try {
            Company company = companyFacade.getCompanyByCompanyId(id);
            return fillProperty(company);
        }catch (cn.bit.common.facade.exception.BizException e){
            log.error(e.getMessage());
            switch (e.getSubCode()) {
                case CodeConstants.CODE_COMPANY_NOT_EXIST:
                    throw COMPANY_NOT_EXIST;
                default:
                    throw UNKNOWN_ERROR;
            }
        }
    }

    @Override
    public Property findByCommunityId(ObjectId communityId) {
        List<CompanyToCommunity> companies = companyFacade
                .listCompaniesByCommunityIdAndCompanyType(communityId, CompanyTypeEnum.PROPERTY.value());
        if(companies.isEmpty()){
            throw COMMUNITY_NOT_BIND_PROPERTY;
        }
        ObjectId companyId = companies.get(0).getCompanyId();
        return findOne(companyId);
    }

    @Override
    public List<PropertyDTO> findByCommunityIds(Collection<ObjectId> communityIds) {
        List<CommunityCompanyDTO> dtoList = companyFacade
                .listCompaniesByCommunityIdsAndCompanyType(communityIds, CompanyTypeEnum.PROPERTY.value());
        if(dtoList.isEmpty()){
            return Collections.emptyList();
        }
        List<PropertyDTO> propertyDTOList = new ArrayList<>();
        dtoList.forEach(communityCompanyDTO -> {
            PropertyDTO propertyDTO = new PropertyDTO();
            BeanUtils.copyProperties(communityCompanyDTO, propertyDTO);
            propertyDTOList.add(propertyDTO);
        });
        return propertyDTOList;
    }

    private Property fillProperty(Company company) {
        Property property = new Property();
        BeanUtils.copyProperties(company, property);
        property.setLogoImg(company.getLogo());
        property.setContact(company.getContactsName());
        property.setTelphone(company.getContactsPhone());
        property.setCreatorId(company.getCreator());
        return property;
    }
}
