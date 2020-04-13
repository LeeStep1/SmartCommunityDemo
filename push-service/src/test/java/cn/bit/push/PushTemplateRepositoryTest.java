package cn.bit.push;

import cn.bit.facade.model.push.PushTemplate;
import cn.bit.push.dao.PushTemplateRepository;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-context.xml")
public class PushTemplateRepositoryTest {

    @Autowired
    private PushTemplateRepository dao;


    @Test
    public void testAdd() {
        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setName("测试");
        pushTemplate.setContent("{\"platform\": \"all\",\"audience\" : {\"registration_id\" : [ \"171976fa8a8b68962cf\" ]},\"notification\" : {\"alert\" : \"Hi, JPush!\",\"android\" : {}, \"ios\" : {\"extras\" : { \"newsid\" : 321}}}}");
        pushTemplate.setCreateAt(new Date());
        pushTemplate.setUpdateAt(pushTemplate.getCreateAt());
        pushTemplate.setDataStatus(1);
        pushTemplate = dao.insert(pushTemplate);
    }

    @Test
    public void testUpdate() {
        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setId(new ObjectId("5a79b2648d6a6cd6d97135cb"));
        pushTemplate.setName("测试");
        pushTemplate.setContent("{\"a\": \"%b%\", \"c\": true}");
        pushTemplate.setCreateAt(new Date());
        pushTemplate.setUpdateAt(pushTemplate.getCreateAt());
        pushTemplate.setDataStatus(1);
        System.err.println(dao.updateOne(pushTemplate));
    }

    @Test
    public void testFindById() {
        PushTemplate pushTemplate = dao.findById(new ObjectId("5a79b2648d6a6cd6d97135cb"));
        System.err.println(pushTemplate);
    }

    @Test
    public void testDeleteById() {
        PushTemplate pushTemplate = new PushTemplate();
        pushTemplate.setId(new ObjectId("5a79b2648d6a6cd6d97135cb"));
        dao.delete(pushTemplate);
    }

}
