package cn.bit.api.support.interceptor;

import cn.bit.api.support.SessionUtil;
import cn.bit.common.facade.company.enums.CompanyTypeEnum;
import cn.bit.common.facade.company.model.CompanyToCommunity;
import cn.bit.common.facade.company.service.CompanyFacade;
import cn.bit.framework.utils.string.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

import static cn.bit.facade.exception.community.CommunityBizException.COMMUNITY_NOT_BIND_PROPERTY;
import static cn.bit.framework.exceptions.BizException.HEADER_INVALID;

/**
 * 社区及企业头部 拦截器
 *
 * @author decai.liu
 * @date 2019-06-21
 */
public class HeaderInterceptor implements HandlerInterceptor, InitializingBean {

    @Resource
    private CompanyFacade companyFacade;

    /**
     * http head cid字段名
     */
    private final String BIT_CID = "BIT-CID";

    /**
     * http head company_id字段名
     */
    private final String COMPANY_ID = "COMPANY-ID";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object o) throws Exception {
        if (!(o instanceof HandlerMethod)) {
            return true;
        }

        String cidStr = request.getHeader(BIT_CID);
        cidStr = StringUtils.isBlank(cidStr) ? request.getParameter(BIT_CID) : cidStr;
        String companyIdStr = request.getHeader(COMPANY_ID);
        companyIdStr = StringUtils.isBlank(companyIdStr) ? request.getParameter(COMPANY_ID) : companyIdStr;
        if (StringUtil.isBlank(cidStr)) {
            throw HEADER_INVALID;
        }
        ObjectId cid = null;
        try {
            cid = new ObjectId(cidStr);
        } catch (IllegalArgumentException e) {
            throw HEADER_INVALID;
        }
        ObjectId companyId = null;
        if (StringUtil.isBlank(companyIdStr)) {
            List<CompanyToCommunity> companyToCommunities =
                    companyFacade.listCompaniesByCommunityIdAndCompanyType(cid, CompanyTypeEnum.PROPERTY.value());
            // 社区没有绑定物业公司
            if (companyToCommunities == null || companyToCommunities.isEmpty()) {
                throw COMMUNITY_NOT_BIND_PROPERTY;
            }
            companyId = companyToCommunities.get(0).getCompanyId();
        }
        try {
            if (companyId == null) {
                companyId = new ObjectId(companyIdStr);
            }
        } catch (IllegalArgumentException e) {
            throw HEADER_INVALID;
        }

        // 统一将Header设置到线程会话
        SessionUtil.setCommunityId(cid);
        SessionUtil.setCompanyId(companyId);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest httpServletRequest,
                           HttpServletResponse httpServletResponse,
                           Object o, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {

        // 清理资源：清除线程会话的Header
        SessionUtil.setCommunityId(null);
        SessionUtil.setCompanyId(null);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
    }
}
