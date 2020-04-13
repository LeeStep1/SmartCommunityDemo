package cn.bit.api.support;

import cn.bit.common.facade.enums.OsEnum;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.framework.context.SpringContextHolder;
import cn.bit.framework.utils.ThreadContext;
import org.bson.types.ObjectId;

/**
 * Created by terry on 2018/1/15.
 */
public class SessionUtil {

    private static final String USER_KEY = "USER_KEY";

    private static final String APP_KEY = "APP_KEY";

    private static final String CID_KEY = "CID_KEY";

    private static final String COMPANY_ID_KEY = "COMPANY_ID_KEY";

    public static void bindTokenSubject(String token, ObjectId uid) {
        ThreadContext.put(USER_KEY, new TokenSubject(token, uid));
    }

    public static TokenSubject getTokenSubject() {
        return (TokenSubject) ThreadContext.get(USER_KEY);
    }

    public static void bindAppSubject(ObjectId appId, Integer client, Integer partner, OsEnum osEnum, ObjectId accAppId,
                                      ObjectId pushAppId) {
        ThreadContext.put(APP_KEY, new AppSubject(appId, client, partner, osEnum, accAppId, pushAppId));
    }

    public static AppSubject getAppSubject() {
        return (AppSubject) ThreadContext.get(APP_KEY);
    }

    public static void setCommunityId(ObjectId communityId) {
        ThreadContext.put(CID_KEY, communityId);
    }

    public static ObjectId getCommunityId() {
        return (ObjectId) ThreadContext.get(CID_KEY);
    }

    public static void setCompanyId(ObjectId companyId) {
        ThreadContext.put(COMPANY_ID_KEY, companyId);
    }

    public static ObjectId getCompanyId() {
        return (ObjectId) ThreadContext.get(COMPANY_ID_KEY);
    }

    public static UserVO getCurrentUser() {
        TokenSubject tokenSubject = getTokenSubject();
        if (tokenSubject == null) {
            return null;
        }

        AppSubject appSubject = getAppSubject();
        if (appSubject == null) {
            return null;
        }

        UserFacade userFacade = SpringContextHolder.getBean(UserFacade.class);
        return userFacade.getUserById(appSubject.getClient(), appSubject.getPartner(), tokenSubject.getUid());
    }

}
