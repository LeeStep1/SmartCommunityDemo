package cn.bit.communityIoT.mq;

import cn.bit.facade.enums.CertificateType;
import cn.bit.facade.model.trade.Trade;
import cn.bit.facade.vo.communityIoT.elevator.BuildingListVO;
import cn.bit.facade.vo.mq.DeviceAuthVO;
import cn.bit.facade.vo.mq.MiliDoorAuthVO;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static cn.bit.facade.constant.mq.QueueConstant.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class RabbitTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    public void testMiliAdd() {
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        deviceAuthVO.setName("测试人员");
        deviceAuthVO.setPhone("13454111157");
        BuildingListVO buildingListVO = new BuildingListVO();
        buildingListVO.setBuildingId(new ObjectId("5aa1eb42e4b071c9f9b6f1ad"));
        buildingListVO.setRooms(Collections.singleton(new ObjectId("5aa1f3a79ce9d549052cfd75")));
        Set<BuildingListVO> vos = Collections.singleton(buildingListVO);
        deviceAuthVO.setBuildingList(vos);
        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setKeyNo("BB7ADBEE765B");
        deviceAuthVO.setKeyId(new ObjectId().toHexString());
        deviceAuthVO.setProcessTime(300);
        deviceAuthVO.setUsesTime(0);
        deviceAuthVO.setCorrelationId(new ObjectId("5af3ffe85ee61dc00fcbd465"));
        deviceAuthVO.setHandleCount(0);
        deviceAuthVO.setSex(1);
        amqpTemplate.convertAndSend(QUEUE_COMMUNITY_IOT_DOOR_AUTH_ADD, deviceAuthVO);
        System.out.println(deviceAuthVO);
    }

    @Test
    public void testKangtuDoorAdd() {
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setKeyId("5af3b64ee4b0d052e005e19e");
        deviceAuthVO.setKeyNo("AB7C1D9630CC");
        deviceAuthVO.setProcessTime(1576800000);
        deviceAuthVO.setName("陈小池");
        deviceAuthVO.setHandleCount(1);
        deviceAuthVO.setUsesTime(0);
        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setCommunityId(new ObjectId("5a8cfa62518089ae7afccc0c"));
        BuildingListVO buildingListVO = new BuildingListVO();
        buildingListVO.setBuildingId(new ObjectId("5ab64bae916ac93d8fd0d3ea"));
        buildingListVO.setRooms(Collections.singleton(new ObjectId("5ab66eb4916ac93d8fd0d3f1")));
        Set<BuildingListVO> vos = Collections.singleton(buildingListVO);
        deviceAuthVO.setBuildingList(vos);
        deviceAuthVO.setPhone("18688906319");
        System.err.println(deviceAuthVO);
        amqpTemplate.convertAndSend(QUEUE_COMMUNITY_IOT_DOOR_AUTH_ADD, deviceAuthVO);
    }

    @Test
    public void testKangtuDoorDelete() {
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setKeyId("5af3e81847bc626165f37319");
        deviceAuthVO.setKeyNo("BB7ADBEE765B");
        deviceAuthVO.setProcessTime(300);
        deviceAuthVO.setName("测试人员");
        deviceAuthVO.setHandleCount(1);
        deviceAuthVO.setUsesTime(0);
        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setCommunityId(new ObjectId("5a8cfa62518089ae7afccc0c"));
        BuildingListVO buildingListVO = new BuildingListVO();
        buildingListVO.setBuildingId(new ObjectId("5aa103880cf2ca5d5440967e"));
        buildingListVO.setRooms(Collections.singleton(new ObjectId("5aa5e74777014dddc0b82f79")));
        Set<BuildingListVO> vos = Collections.singleton(buildingListVO);
        deviceAuthVO.setBuildingList(vos);
        deviceAuthVO.setPhone("13454111157");
        System.err.println(deviceAuthVO);
        amqpTemplate.convertAndSend(QUEUE_COMMUNITY_IOT_DOOR_AUTH_DELETE, deviceAuthVO);
    }

    @Test
    public void testKangtuElevatorAdd() {
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setKeyId("5af3e81847bc626165f37319");
        deviceAuthVO.setKeyNo("BB7ADBEE765B");
        deviceAuthVO.setProcessTime(300);
        deviceAuthVO.setName("测试人员");
        deviceAuthVO.setHandleCount(1);
        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        BuildingListVO buildingListVO = new BuildingListVO();
        buildingListVO.setBuildingId(new ObjectId("5aa1eb42e4b071c9f9b6f1ad"));
        buildingListVO.setRooms(Collections.singleton(new ObjectId("5aa1f3a79ce9d549052cfd75")));
        deviceAuthVO.setBuildingList(Collections.singleton(buildingListVO));
        deviceAuthVO.setPhone("13454111157");
        System.err.println(deviceAuthVO);
        amqpTemplate.convertAndSend(QUEUE_COMMUNITY_IOT_ELEVATOR_AUTH_ADD, deviceAuthVO);
    }

    @Test
    public void testKangtuElevatorDelete() {
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setKeyId("5af3e81847bc626165f37319");
        deviceAuthVO.setKeyNo("BB7ADBEE765B");
        deviceAuthVO.setName("测试人员");
        deviceAuthVO.setHandleCount(1);
        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        BuildingListVO buildingListVO = new BuildingListVO();
        buildingListVO.setBuildingId(new ObjectId("5aa1eb42e4b071c9f9b6f1ad"));
        buildingListVO.setRooms(Collections.singleton(new ObjectId("5aa1f3a79ce9d549052cfd75")));
        deviceAuthVO.setBuildingList(Collections.singleton(buildingListVO));
        deviceAuthVO.setPhone("13454111157");
        System.err.println(deviceAuthVO);
        amqpTemplate.convertAndSend(QUEUE_COMMUNITY_IOTELEVATOR_AUTH_DELETE, deviceAuthVO);
    }

    @Test
    public void testKangtuElevatorCover() {
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setKeyId("5af3e81847bc626165f37319");
        deviceAuthVO.setKeyNo("BB7ADBEE765B");
        deviceAuthVO.setName("测试人员");
        deviceAuthVO.setHandleCount(1);
        deviceAuthVO.setProcessTime(1576800000);
        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        BuildingListVO buildingListVO = new BuildingListVO();
        buildingListVO.setBuildingId(new ObjectId("5aa1eb42e4b071c9f9b6f1ad"));
        buildingListVO.setRooms(Collections.singleton(new ObjectId("5aa1f3a79ce9d549052cfd75")));
        deviceAuthVO.setBuildingList(Collections.singleton(buildingListVO));
        deviceAuthVO.setPhone("13454111157");
        System.err.println(deviceAuthVO);
        amqpTemplate.convertAndSend(QUEUE_COMMUNITY_IOT_ELEVATOR_AUTH_COVER, deviceAuthVO);
    }

    @Test
    public void testDistrictOpen() {
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setName("测试人员");
        deviceAuthVO.setHandleCount(1);
        deviceAuthVO.setSex(1);
        deviceAuthVO.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        deviceAuthVO.setCorrelationId(new ObjectId("5a803bef9ce9b7f61ae863ad"));
        BuildingListVO buildingListVO = new BuildingListVO();
        buildingListVO.setBuildingId(new ObjectId("5aa1eb42e4b071c9f9b6f1ad"));
        deviceAuthVO.setBuildingList(Collections.singleton(buildingListVO));
        deviceAuthVO.setPhone("13454111157");
        System.err.println(deviceAuthVO);
        amqpTemplate.convertAndSend(QUEUE_COMMUNITY_IOT_DOOR_AUTH_ADD, deviceAuthVO);
    }

    @Test
    public void testDistrictCover() {
        DeviceAuthVO deviceAuthVO = new DeviceAuthVO();
        deviceAuthVO.setName("测试人员");
        deviceAuthVO.setHandleCount(1);
        deviceAuthVO.setSex(1);
        deviceAuthVO.setKeyId("5af3e81847bc626165f37319");
        deviceAuthVO.setKeyNo("BB7ADBEE765B");
        deviceAuthVO.setKeyType(CertificateType.PHONE_MAC.KEY);
        deviceAuthVO.setCommunityId(new ObjectId("5a8cfa62518089ae7afccc0c"));
        deviceAuthVO.setCorrelationId(new ObjectId("5af43f765ee61dc00fcdacd0"));
        Set<BuildingListVO> set = new HashSet<>();
        BuildingListVO buildingListVO2 = new BuildingListVO();
        set.add(buildingListVO2);
        deviceAuthVO.setBuildingList(set);
        deviceAuthVO.setPhone("15521032221");
        System.err.println(deviceAuthVO);
        amqpTemplate.convertAndSend(QUEUE_COMMUNITY_IOT_DOOR_AUTH_COVER, deviceAuthVO);
    }
}
