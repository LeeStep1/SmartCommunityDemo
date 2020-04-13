package cn.bit.task;

import cn.bit.facade.model.task.Record;
import cn.bit.facade.model.task.Schedule;
import cn.bit.facade.service.task.ScheduleFacade;
import cn.bit.facade.vo.task.ScheduleRequest;
import cn.bit.framework.utils.DateUtils;
import cn.bit.task.dao.RecordRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;

/**
 * Created by terry on 2018/1/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-context.xml")
public class MongoTest {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private ScheduleFacade scheduleFacade;

    @Test
    public void test() {
        /*DBObject dbObject = new BasicDBObject();
        dbObject.put("id", "5a5df3a6950e2e46aa2c8794");  //查询条件

        BasicDBObject fieldsObject=new BasicDBObject();
//指定返回的字段
        fieldsObject.put("id", true);
        Query query = new BasicQuery(dbObject,fieldsObject);
        User user = mongoTemplate.getCameraById(query, User.class);
        System.err.println(JSON.toJSONString(user));*/

        Record record = new Record();
//        record.setId("5a69ded5950e84c2078cc777");//default sequence
        record.setUserId(new ObjectId());
        record.setUserName("decai.liu2");
        record.setCommunityId(new ObjectId());
        record.setRemark("test add record2");
        record.setCreateAt(new Date());
        record.setCreatorId(new ObjectId());
        record.setTaskType(2);//巡更
        record.setDataStatus(1);
        record.setBuckName("bucket-name");
        record.setKey("key");
        record.setUrl("/mnt/san/Record/Image/image2.jpg");
        record.setCoordinate("30,50");
        record.setDeviceId("bluetooth001");
//        record.setUpdateAt(null);
//        record.setUpdateAt(new Date());

        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + record.toString());

        mongoTemplate.insert(record);
        System.err.println(">>>>>>>>> record insert complete !!!!!");
        Query query = Query.query(Criteria.where("userId").is("12345").and("taskType").is(1));
        System.err.println("query:" + query);
        System.err.println("queryStr:" + query.toString());
        boolean rs = mongoTemplate.exists(query,Record.class);
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + rs);
        Record record2 = mongoTemplate.findOne(query,Record.class);
        System.err.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>record2:" + record2.toString());
    }

    @Test
    public void test3() throws Exception{
        ScheduleRequest request = new ScheduleRequest();
        request.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        request.setPostCode("5cc806e4d5de9f51fa9550dc");
//        request.setStartDate(DateUtils.parseDate("2019-06-01", "yyyy-MM-dd"));
//        request.setEndDate(DateUtils.parseDate("2019-06-30", "yyyy-MM-dd"));
        request.setDutyTime(DateUtils.parseDate("2019-06-14 16:21:05", "yyyy-MM-dd HH:mm:ss"));
//        Page<Schedule> page = scheduleFacade.getSchedules(request, 1, 10);
//        System.out.println("pageList:" + page.getRecords());
        List<Schedule> list = scheduleFacade.getSchedules(request);
        System.out.println("list:" + list);
    }

    @Test
    public void test4() throws Exception{
        ScheduleRequest request = new ScheduleRequest();
        request.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        request.setPostCode("5cc806e4d5de9f51fa9550dc");
        request.setStartDate(DateUtils.parseDate("2019-06-01", "yyyy-MM-dd"));
        request.setEndDate(DateUtils.parseDate("2019-06-10", "yyyy-MM-dd"));
        scheduleFacade.deleteSchedules(request);
        request.setEndDate(DateUtils.parseDate("2019-06-11", "yyyy-MM-dd"));
        List<Schedule> list = scheduleFacade.getSchedules(request);
        System.out.println("list:" + list);
    }

}
