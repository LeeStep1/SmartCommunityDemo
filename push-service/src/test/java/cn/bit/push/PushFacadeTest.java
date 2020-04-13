package cn.bit.push;

import cn.bit.facade.service.push.PushFacade;
import cn.bit.facade.vo.push.PushResult;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-context.xml")
public class PushFacadeTest {

    @Autowired
    private PushFacade pushFacade;

    @Test
    public void testSendPush() {
//        PushResult pushResult = pushFacade.sendPush(new ObjectId("5a79b2648d6a6cd6d97135cb"), new HashMap<>());
//        System.err.println(pushResult.getMessageId());
    }

}
