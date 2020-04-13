package cn.bit.vehicle;

import cn.bit.facade.model.vehicle.InOut;
import cn.bit.facade.service.vehicle.InoutFacade;
import cn.bit.facade.vo.vehicle.InoutRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.utils.DateUtils;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * Created by decai.liu on 2019/1/7.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-context.xml")
public class UnitTest {
    @Autowired
    private InoutFacade inoutFacade;

    /**
     * 查询门禁记录列表
     * @throws Exception
     */
    @Test
    public void test1() throws Exception{
        InoutRequest request = new InoutRequest();
        request.setGateNO("3");
        request.setInOutDate(DateUtils.parseDate("2017-12-13", "yyyy-MM-dd"));
        List<InOut> list = inoutFacade.getInoutRecordByCarGate(request, new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        System.out.println("count:" + list.size() + ", list:" + list);
    }

    /**
     * 分页查询门禁记录
     * @throws Exception
     */
    @Test
    public void test2() throws Exception{
        InoutRequest request = new InoutRequest();
        request.setGateNO("1");
        request.setInOutDate(DateUtils.parseDate("2017-12-13", "yyyy-MM-dd"));
        Page<InOut> page = inoutFacade.getInoutRecordByCarGatePage(
                request, new ObjectId("5a82adf3b06c97e0cd6c0f3d"), 1, 10);
        System.out.println("total:" + page.getTotal() + ", PageList:" + page.getRecords());
    }

}
