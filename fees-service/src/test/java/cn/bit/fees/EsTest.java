package cn.bit.fees;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.fees.PropertyBill;
import cn.bit.fees.dao.PropertyBillRepository;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class EsTest {

    @Autowired
    private PropertyBillRepository repository;

    @Autowired
    private EsTemplate esTemplate;

    @Test
    public void test () throws InterruptedException {
        int size = 1000;
        int page = 1;
        int count = size;
        while (count == size) {
            PropertyBill toGet = new PropertyBill();
            toGet.setDataStatus(DataStatusType.VALID.KEY);
            Page<PropertyBill> _page = repository.findPage(toGet, page++, size, null);
            count = _page.getRecords().size();
            if (count == 0) {
                return;
            }

            for (PropertyBill propertyBill : _page.getRecords()) {
                cn.bit.facade.data.fees.PropertyBill genPropertyBill = new cn.bit.facade.data.fees.PropertyBill();
                genPropertyBill.setCommunityId(propertyBill.getCommunityId());
                genPropertyBill.setRoomId(propertyBill.getRoomId());
                genPropertyBill.setStatus(propertyBill.getBillStatus());
                genPropertyBill.setTotalAmount(propertyBill.getTotalAmount());
                genPropertyBill.setCreateAt(propertyBill.getCreateAt());
                genPropertyBill.setExpireAt(propertyBill.getOverdueDate());
                esTemplate.upsertAsync("cm_bill", "property_bill", propertyBill.getId().toString(), genPropertyBill);
            }
        }

        Thread.sleep(5000L);
    }

}
