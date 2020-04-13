package cn.bit.user;

import cn.bit.facade.data.user.Household;
import cn.bit.facade.data.user.TenantApplication;
import cn.bit.facade.enums.CertificateType;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.user.Card;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.utils.IdentityCardUtils;
import cn.bit.framework.utils.string.StringUtil;
import cn.bit.user.dao.CardRepository;
import cn.bit.user.dao.HouseholdRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class EsTest {
    @Autowired
    private EsTemplate esTemplate;

    @Autowired
    private HouseholdRepository householdRepository;

    @Autowired
    private CardRepository cardRepository;

    /**
     * 住户档案数据同步到es
     * @throws Exception
     */
    @Test
    public void test() throws Exception {
        int size = 1000;
        int page = 1;
        int count = size;
        while (count == size) {
            cn.bit.facade.model.user.Household toGet = new cn.bit.facade.model.user.Household();
            toGet.setDataStatus(DataStatusType.VALID.KEY);
            Page<cn.bit.facade.model.user.Household> _page = householdRepository.findPage(toGet, page++, size, null);
            count = _page.getRecords().size();
            if (count == 0) {
                return;
            }

            for (cn.bit.facade.model.user.Household h : _page.getRecords()) {
                Household household = new Household();
                if(StringUtil.isNotBlank(h.getIdentityCard())){
                    IdentityCardUtils.IdentityCardMeta meta = IdentityCardUtils.getIdentityCardMeta(h.getIdentityCard());
                    if(meta != null){
                        household.setBirthday(meta.getBirthday());
                    }
                }
                household.setCommunityId(h.getCommunityId());
                household.setRelationship(h.getRelationship());
                household.setSex(h.getSex());
                household.setCreateAt(h.getCreateAt());
                esTemplate.upsertAsync("cm_user", "household", h.getId().toString(), household);
            }
        }

        Thread.sleep(5000L);
    }

    @Test
    public void test1() throws Exception {
        int size = 1000;
        int page = 1;
        int count = size;
        while (count == size) {
            Card toGet = new Card();
            toGet.setKeyType(CertificateType.QR_CODE.KEY);
            toGet.setDataStatus(DataStatusType.VALID.KEY);
            Page<Card> _page = cardRepository.findPage(toGet, page++, size, null);
            count = _page.getRecords().size();
            if (count == 0) {
                return;
            }

            for (Card card : _page.getRecords()) {
                TenantApplication tenantApplication = new TenantApplication();
                tenantApplication.setCommunityId(card.getCommunityId());
                tenantApplication.setCreateAt(card.getCreateAt());
                esTemplate.upsertAsync("cm_tenant_record", "application", card.getId().toString(), tenantApplication);
            }
        }

        Thread.sleep(5000L);
    }

}
