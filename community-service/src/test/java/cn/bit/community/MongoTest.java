package cn.bit.community;

import cn.bit.community.dao.BuildingRepository;
import cn.bit.community.dao.CommunityRepository;
import cn.bit.community.dao.CommunityTradeAccountRepository;
import cn.bit.facade.enums.DataStatusType;
import cn.bit.facade.enums.PlatformType;
import cn.bit.facade.model.community.Building;
import cn.bit.facade.model.community.Community;
import cn.bit.facade.model.community.CommunityTradeAccount;
import cn.bit.facade.model.community.DataLayout;
import cn.bit.facade.service.community.BuildingFacade;
import cn.bit.facade.service.community.CommunityFacade;
import cn.bit.facade.service.community.DataLayoutFacade;
import cn.bit.facade.vo.community.DataLayoutQuery;
import cn.bit.facade.vo.community.Points;
import org.bson.types.ObjectId;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:spring/spring-context.xml")
public class MongoTest {

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private CommunityTradeAccountRepository communityTradeAccountRepository;

    @Autowired
    private DataLayoutFacade dataLayoutFacade;

    @Autowired
    private CommunityFacade communityFacade;

    @Autowired
    private BuildingFacade buildingFacade;

    @Test
    public void test() {
        List<Building> buildingIds = buildingRepository.findByCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        System.err.println(buildingIds.size());
    }

    @Test
    public void test1() {
        CommunityTradeAccount communityTradeAccount = new CommunityTradeAccount();
        communityTradeAccount.setClient(1000);
        communityTradeAccount.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        communityTradeAccount.setTradeAccountId(new ObjectId("5ab37cdc8d6a7ab6b93a617b"));
        communityTradeAccount.setPlatform(PlatformType.WECHAT.value());
        communityTradeAccount.setCreateAt(new Date());
        communityTradeAccount.setUpdateAt(communityTradeAccount.getCreateAt());
        communityTradeAccount.setDataStatus(DataStatusType.VALID.KEY);
        communityTradeAccountRepository.insert(communityTradeAccount);
    }

    /**
     * JPA 方法名更新示例
     */
    @Test
    public void test2() {
        Community community = new Community();
        community.setYun_community_id(81L);
        community.setHouseholdCnt(1);
        community.setCheckInRoomCnt(1);
        community.setUpdateAt(new Date());
        community.setMenus(Collections.<ObjectId>emptySet());
        System.err.println(communityRepository.upsertWithIncHouseholdCntAndCheckInRoomCntById(community,
                new ObjectId("5a82adf3b06c97e0cd6c0f3d")));
    }

    /**
     * 保存大屏布局
     */
    @Test
    public void test3(){
        DataLayout dataLayout = new DataLayout();
        dataLayout.setId(new ObjectId("5c2e0266852e3b1bd0b725f0"));
        dataLayout.setKey("household");
        dataLayout.setName("住户数据");
        dataLayout.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        dataLayout.setRefreshInterval(300);
        dataLayout.setDisplayable(Boolean.TRUE);
        dataLayout.setScreenRatioType(1);
        dataLayout.setPoints(new Points(0, 0, 1, 2));
        dataLayout.setUpdateAt(new Date());
        dataLayoutFacade.saveDataLayout(dataLayout);

        dataLayout = new DataLayout();
        dataLayout.setKey("community");
        dataLayout.setName("社区实景");
        dataLayout.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        dataLayout.setRefreshInterval(300);
        dataLayout.setDisplayable(Boolean.TRUE);
        dataLayout.setScreenRatioType(1);
        dataLayout.setPoints(new Points(1, 0, 2, 2));
        dataLayout.setUpdateAt(new Date());
        dataLayoutFacade.saveDataLayout(dataLayout);

        dataLayout = new DataLayout();
        dataLayout.setKey("fee");
        dataLayout.setName("收费情况");
        dataLayout.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        dataLayout.setRefreshInterval(300);
        dataLayout.setDisplayable(Boolean.TRUE);
        dataLayout.setScreenRatioType(1);
        dataLayout.setPoints(new Points(3, 0, 1, 1));
        dataLayout.setUpdateAt(new Date());
        dataLayoutFacade.saveDataLayout(dataLayout);

        dataLayout = new DataLayout();
        dataLayout.setKey("work");
        dataLayout.setName("工单统计");
        dataLayout.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        dataLayout.setRefreshInterval(300);
        dataLayout.setDisplayable(Boolean.TRUE);
        dataLayout.setScreenRatioType(1);
        dataLayout.setPoints(new Points(4, 0, 1, 2));
        dataLayout.setUpdateAt(new Date());
        dataLayoutFacade.saveDataLayout(dataLayout);

        dataLayout = new DataLayout();
        dataLayout.setKey("videoMonitor");
        dataLayout.setName("视频监控");
        dataLayout.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        dataLayout.setRefreshInterval(300);
        dataLayout.setDisplayable(Boolean.TRUE);
        dataLayout.setScreenRatioType(1);
        dataLayout.setPoints(new Points(3, 1, 1, 1));
        dataLayout.setAttachValue(30);
        dataLayout.setUpdateAt(new Date());
        dataLayoutFacade.saveDataLayout(dataLayout);

        dataLayout = new DataLayout();
        dataLayout.setKey("guest");
        dataLayout.setName("访客数据");
        dataLayout.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        dataLayout.setRefreshInterval(300);
        dataLayout.setDisplayable(Boolean.TRUE);
        dataLayout.setScreenRatioType(1);
        dataLayout.setPoints(new Points(0, 2, 1, 1));
        dataLayout.setUpdateAt(new Date());
        dataLayoutFacade.saveDataLayout(dataLayout);

        dataLayout = new DataLayout();
        dataLayout.setKey("car");
        dataLayout.setName("车辆数据");
        dataLayout.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        dataLayout.setRefreshInterval(300);
        dataLayout.setDisplayable(Boolean.TRUE);
        dataLayout.setScreenRatioType(1);
        dataLayout.setPoints(new Points(1, 2, 1, 1));
        dataLayout.setUpdateAt(new Date());
        dataLayoutFacade.saveDataLayout(dataLayout);

        dataLayout = new DataLayout();
        dataLayout.setKey("door");
        dataLayout.setName("门禁数据");
        dataLayout.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        dataLayout.setRefreshInterval(300);
        dataLayout.setDisplayable(Boolean.TRUE);
        dataLayout.setScreenRatioType(1);
        dataLayout.setPoints(new Points(2, 2, 1, 1));
        dataLayout.setUpdateAt(new Date());
        dataLayoutFacade.saveDataLayout(dataLayout);

        dataLayout = new DataLayout();
        dataLayout.setKey("elevator");
        dataLayout.setName("电梯数据");
        dataLayout.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        dataLayout.setRefreshInterval(300);
        dataLayout.setDisplayable(Boolean.TRUE);
        dataLayout.setScreenRatioType(1);
        dataLayout.setPoints(new Points(3, 2, 1, 1));
        dataLayout.setUpdateAt(new Date());
        dataLayoutFacade.saveDataLayout(dataLayout);

        dataLayout = new DataLayout();
        dataLayout.setKey("suggestions");
        dataLayout.setName("投诉建议");
        dataLayout.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        dataLayout.setRefreshInterval(300);
        dataLayout.setDisplayable(Boolean.TRUE);
        dataLayout.setScreenRatioType(1);
        dataLayout.setPoints(new Points(4, 2, 1, 1));
        dataLayout.setAttachValue(15);
        dataLayout.setUpdateAt(new Date());
        dataLayoutFacade.saveDataLayout(dataLayout);

    }

    /**
     * 查询布局列表
     */
    @Test
    public void test4(){
        DataLayoutQuery query = new DataLayoutQuery();
        query.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        query.setDisplayable(Boolean.FALSE);
        List<DataLayout> layoutList = dataLayoutFacade.listDataLayouts(query);
        System.out.println(layoutList);
    }

    /**
     * 更新布局
     */
    @Test
    public void test5(){
        DataLayout toUpdate = new DataLayout();
        toUpdate.setId(new ObjectId("5c2e0266852e3b1bd0b725f9"));
        toUpdate.setDisplayable(Boolean.FALSE);
        toUpdate.setPoints(new Points(4,2,1,1));
        toUpdate = dataLayoutFacade.modifyDataLayout(toUpdate);
        System.out.println(toUpdate);
    }

    /**
     * 更新社区实景图片
     */
    @Test
    public void test6(){
        Community toUpdate = new Community();
        toUpdate.setPhotos(Arrays.asList("1.jpg", "2.jpg"));
        toUpdate.setId(new ObjectId("5c2e0266852e3b1bd0b725f9"));
        toUpdate = communityFacade.updateCommunity(toUpdate);
        System.out.println(toUpdate);
    }

    /**
     * 查询社区下已开放的楼栋ID集合
     */
    @Test
    public void test7(){
        List<ObjectId> buildingIds = buildingFacade.getBuildingIdsByCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        System.out.println(buildingIds);
    }
}
