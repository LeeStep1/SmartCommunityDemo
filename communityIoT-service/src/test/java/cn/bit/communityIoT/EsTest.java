package cn.bit.communityIoT;

import cn.bit.communityIoT.dao.DoorRecordRepository;
import cn.bit.communityIoT.dao.DoorRepository;
import cn.bit.communityIoT.dao.ElevatorRecordRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.model.communityIoT.Door;
import cn.bit.facade.model.communityIoT.DoorRecord;
import cn.bit.facade.model.communityIoT.ElevatorRecord;
import cn.bit.facade.service.communityIoT.DoorFacade;
import cn.bit.facade.service.communityIoT.ElevatorFacade;
import cn.bit.facade.vo.statistics.ElevatorSummaryResponse;
import cn.bit.framework.data.common.Page;
import cn.bit.framework.data.elasticsearch.EsTemplate;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class EsTest {
    @Autowired
    private DoorRecordRepository doorRecordRepository;

    @Autowired
    private ElevatorRecordRepository elevatorRecordRepository;

    @Autowired
    private ElevatorFacade elevatorFacade;

    @Autowired
    private EsTemplate esTemplate;

    @Autowired
    private DoorRepository doorRepository;

    @Autowired
    private DoorFacade doorFacade;
    @Test
    public void test() throws InterruptedException {
        int size = 1000;
        int page = 1;
        int count = size;
        while (count == size) {
            DoorRecord toGet = new DoorRecord();
            toGet.setDataStatus(DataStatusType.VALID.KEY);
            Page<DoorRecord> _page = doorRecordRepository.findPage(toGet, page++, size, null);
            count = _page.getRecords().size();
            if (count == 0) {
                return;
            }


            for (DoorRecord doorRecord : _page.getRecords()) {
                cn.bit.facade.data.communityIoT.DoorRecord genDoorRecord = new cn.bit.facade.data.communityIoT.DoorRecord();
                genDoorRecord.setCommunityId(doorRecord.getCommunityId());
                genDoorRecord.setDoorId(doorRecord.getDoorId());
                genDoorRecord.setUseStyle(doorRecord.getUseStyle());
                genDoorRecord.setCreateAt(doorRecord.getCreateAt());
                esTemplate.upsertAsync("cm_device_record", "door", doorRecord.getId().toString(), genDoorRecord);
            }
        }

        Thread.sleep(5000L);
    }

    @Test
    public void test1() throws InterruptedException {
        int size = 1000;
        int page = 1;
        int count = size;
        while (count == size) {
            ElevatorRecord toGet = new ElevatorRecord();
            toGet.setDataStatus(DataStatusType.VALID.KEY);
            Page<ElevatorRecord> _page = elevatorRecordRepository.findPage(toGet, page++, size, null);
            count = _page.getRecords().size();
            if (count == 0) {
                return;
            }

            for (ElevatorRecord elevatorRecord : _page.getRecords()) {
                cn.bit.facade.data.communityIoT.ElevatorRecord genElevatorRecord = new cn.bit.facade.data.communityIoT.ElevatorRecord();
                genElevatorRecord.setCommunityId(elevatorRecord.getCommunityId());
                genElevatorRecord.setMac(elevatorRecord.getMacAddress());
                genElevatorRecord.setUseStyle(elevatorRecord.getUseStyle());
                genElevatorRecord.setCreateAt(elevatorRecord.getCreateAt());
                esTemplate.upsertAsync("cm_device_record", "elevator", elevatorRecord.getId().toString(), genElevatorRecord);
            }
        }

        Thread.sleep(5000L);
    }

    /**
     * 删除指定社区的es门禁使用记录
     * @throws InterruptedException
     */
    @Test
    public void test2() throws InterruptedException {
        int size = 1000;
        int page = 1;
        int count = size;
        while (count == size) {
            DoorRecord toGet = new DoorRecord();
            toGet.setDataStatus(DataStatusType.INVALID.KEY);
            toGet.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
            Page<DoorRecord> toDeleteList = doorRecordRepository.findPage(toGet, page++, size, null);
            count = toDeleteList.getRecords().size();
            if (count == 0) {
                return;
            }

            for (DoorRecord doorRecord : toDeleteList.getRecords()) {
                esTemplate.deleteAsync("cm_device_record", "door", doorRecord.getId().toString());
            }
        }

        Thread.sleep(5000L);
    }

    @Test
    public void test3() {
        ElevatorSummaryResponse response = elevatorFacade.summaryElevators(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        System.out.println(response);
    }
    @Test
    public void test4() {
        Door door =doorFacade.getDoorByDeviceIdAndBrandNoAndDeviceCode(2L,5,"114-11-1");
        System.out.println("-----");
        System.out.println(door);
    }
}
