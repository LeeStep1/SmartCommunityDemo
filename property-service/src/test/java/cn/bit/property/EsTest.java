package cn.bit.property;

import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.property.Alarm;
import cn.bit.facade.model.property.Complain;
import cn.bit.facade.model.property.Fault;
import cn.bit.facade.service.property.ComplainFacade;
import cn.bit.facade.service.property.FaultFacade;
import cn.bit.facade.vo.property.ComplainRequest;
import cn.bit.facade.vo.property.FaultPageQuery;
import cn.bit.facade.vo.statistics.FaultResponse;
import cn.bit.facade.vo.statistics.StatisticsRequest;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import cn.bit.framework.utils.DateUtils;
import cn.bit.property.dao.AlarmRepository;
import cn.bit.property.dao.FaultRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class EsTest {

    @Autowired
    private AlarmRepository alarmRepository;

    @Autowired
    private FaultRepository faultRepository;

    @Autowired
    private EsTemplate esTemplate;

    @Autowired
    private FaultFacade faultFacade;

    @Autowired
    private ComplainFacade complainFacade;

    @Test
    public void test() throws Exception {
        int size = 1000;
        int page = 1;
        int count = size;
        while (count == size) {
            Alarm toGet = new Alarm();
            toGet.setDataStatus(DataStatusType.VALID.KEY);
            Page<Alarm> _page = alarmRepository.findPage(toGet, page++, size, null);
            count = _page.getRecords().size();
            if (count == 0) {
                return;
            }

            for (Alarm alarm : _page.getRecords()) {
                cn.bit.facade.data.property.Alarm genAlarm = new cn.bit.facade.data.property.Alarm();
                genAlarm.setCommunityId(alarm.getCommunityId());
                genAlarm.setReceiverId(alarm.getReceiverId());
                genAlarm.setCreateAt(alarm.getCreateAt());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(alarm.getCreateAt());
                genAlarm.setHour(calendar.get(Calendar.HOUR_OF_DAY));
                esTemplate.upsertAsync("cm_accident", "alarm", alarm.getId().toString(), genAlarm);
            }
        }

        Thread.sleep(5000L);
    }

    @Test
    public void test2() throws Exception {
        int size = 1000;
        int page = 1;
        int count = size;
        while (count == size) {
            Fault toGet = new Fault();
            toGet.setDataStatus(DataStatusType.VALID.KEY);
            Page<Fault> _page = faultRepository.findPage(toGet, page++, size, null);
            count = _page.getRecords().size();
            if (count == 0) {
                return;
            }

            for (Fault fault : _page.getRecords()) {
                cn.bit.facade.data.property.Fault genFault = new cn.bit.facade.data.property.Fault();
                genFault.setCommunityId(fault.getCommunityId());
                genFault.setRepairId(fault.getRepairId());
                genFault.setType(fault.getFaultType());
                genFault.setStatus(fault.getFaultStatus());
                genFault.setScore(fault.getEvaluationGrade());
                genFault.setCreateAt(fault.getCreateAt());
                esTemplate.upsertAsync("cm_accident", "fault", fault.getId().toString(), genFault);
            }
        }

        Thread.sleep(5000L);
    }

    /**
     * 故障数量
     */
    @Test
    public void test3() {
        Date beginTime = DateUtils.getMonthStart(new Date());
        long num = faultRepository.countByCommunityIdAndFinishTimeGreaterThanEqualAndFinishTimeLessThan(
                new ObjectId("5a82adf3b06c97e0cd6c0f3d"), beginTime, new Date());
        System.out.println("num = " + num);
    }

    /**
     * 故障统计-大屏
     */
    @Test
    public void test4() {
        StatisticsRequest request = new StatisticsRequest();
        request.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        FaultResponse response = faultFacade.getFaultStatisticsForBigScreen(request);
        System.out.println(response);
    }

    /**
     * 投诉报事列表-大屏
     */
    @Test
    public void test5() {
        List<Complain> complains = complainFacade.listComplainsForScreen(new ObjectId("5a82adf3b06c97e0cd6c0f3d"), 2);
        System.out.println(complains);
    }

    /**
     * 故障分页
     */
    @Test
    public void test6() {
        FaultPageQuery query = new FaultPageQuery();
        query.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        query.setUserId(new ObjectId("5ae135a8e4b07c885aeea1de"));
        query.setHidden(Boolean.TRUE);
        Page<Fault> faultPage = faultFacade.listFaults(query);
        System.out.println(faultPage.getRecords());
    }

    /**
     * 投诉分页
     */
    @Test
    public void test7() {
        ComplainRequest query = new ComplainRequest();
        query.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        query.setUserName("丽");
        Page<Complain> complainPage = complainFacade.getComplainPage(query, 1, 10);
        System.out.println(complainPage.getRecords());
    }

}
