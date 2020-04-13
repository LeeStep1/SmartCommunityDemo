package cn.bit.communityIoT;

import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.service.communityIoT.FreeViewTokenFacade;
import cn.bit.facade.service.communityIoT.MiliTokenFacade;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class TokenTest {

    @Autowired
    private MiliTokenFacade miliTokenFacade;

    @Autowired
    private FreeViewTokenFacade freeViewTokenFacade;

    @Autowired
    private DoorFacade doorFacade;

    @Test
    public void test() {
        ExecutorService executorService = Executors.newCachedThreadPool();
        List<Future<String>> futures = new ArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            Future<String> future = executorService.submit(() -> miliTokenFacade.updateToken("B55C92B6A7290A72"));
            futures.add(future);
        }
        System.out.println(futures.iterator().next());
        try {
            for (int i = 0; i < 1000; i++) {
                System.err.println(String.format("第%d次获取token: %s", i, futures.get(i).get()));
            }
        } catch (Exception e) {
        }

        executorService.shutdown();
    }

    @Test
    public void testDoorQuery() {
        Set<ObjectId> bIds = new HashSet<>();
        bIds.add(new ObjectId("5aa0f477f7183af6340b2f79"));
        bIds.add(new ObjectId("5ab64bae916ac93d8fd0d3ea"));
        List<Door> list = doorFacade.getBuildingAndCommunityDoorByBrandNo(bIds,
                                                                          new ObjectId("5a8cfa62518089ae7afccc0c"),
                                                                          2);
        System.out.println(list);
    }
}
