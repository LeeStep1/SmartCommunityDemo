package cn.bit.user;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.bit.facade.model.user.Household;
import cn.bit.facade.model.user.UserToRoom;
import cn.bit.facade.poi.entity.HouseholdEntity;
import cn.bit.facade.poi.styler.ExcelExportStylerImpl;
import cn.bit.facade.service.user.HouseholdFacade;
import cn.bit.facade.service.user.UserFacade;
import cn.bit.facade.service.user.UserToPropertyFacade;
import cn.bit.facade.service.user.UserToRoomFacade;
import cn.bit.facade.vo.user.UserVO;
import cn.bit.facade.vo.user.userToProperty.EmployeeVO;
import cn.bit.facade.vo.user.userToProperty.UserToProperty;
import cn.bit.facade.vo.user.userToRoom.EmergencyContactDTO;
import cn.bit.facade.vo.user.userToRoom.HouseholdPageQuery;
import cn.bit.facade.vo.user.userToRoom.HouseholdVO;
import cn.bit.facade.vo.user.userToRoom.MemberDTO;
import cn.bit.framework.data.common.Page;
import com.alibaba.fastjson.JSON;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;
import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by terry on 2018/1/14.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/spring-context.xml")
public class FacadeTest {
    @Autowired
    private UserFacade userFacade;

    @Autowired
    private UserToPropertyFacade userToPropertyFacade;

    @Autowired
    private UserToRoomFacade userToRoomFacade;

    @Autowired
    private HouseholdFacade householdFacade;

//    @Test
//    public void test() {
//        User user = new User();
//        user.setPhone("15918729264");
//        user.setPassword(DigestUtils.md5Hex("123"));
//        user.setName("ali");
//        user.setEmail("ali@163.com");
//        user.getRoles().add("HOUSEHOLDER");
//        userFacade.addUser(1000, "", user, null);
//    }

    /*@Test
    public void test1() throws InterruptedException {
        UserVO urp = userFacade.signIn("18665682210","123456");
        System.err.println(JSON.toJSONString(urp));//06042960dae04695a16b69d48e1f4dbf
        Thread.sleep(2000);
    }*/

    @Test
    public void test2() {
        UserVO urp = userFacade.getUserByToken("06042960dae04695a16b69d48e1f4dbf");
        System.err.println(JSON.toJSONString(urp));
    }

    @Test
    public void test3() {
        EmployeeVO employeeVO = new EmployeeVO();
        employeeVO.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        Page<UserToProperty> page = userToPropertyFacade.findPageByCommunityIdAndUserToProperty(employeeVO, 0, 2, 10);
        Assert.assertFalse(page.getRecords().isEmpty());
    }

    @Test
    public void testUserToRoom(){
        List<UserToRoom> list = userToRoomFacade.queryListByBuildingId(new ObjectId("5a82ae1db06c97e0cd6c0f3f"));
        System.out.println(list);
    }

    private static class TestVO {
        private Long id;
        private String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @Test
    public void testAddHousehold(){
        HouseholdVO householdVO = new HouseholdVO();
        householdVO.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        householdVO.setBuildingId(new ObjectId("5aa1eb42e4b071c9f9b6f1ad"));
        householdVO.setRoomId(new ObjectId("5aa1f3a49ce9d549052cfcd0"));
        householdVO.setRoomLocation("3单元101");
        householdVO.setUserName("刘德财");
        householdVO.setPhone("15889898240");
        householdVO.setRelationship(1);
        householdVO.setSex(1);
        householdVO.setWorkUnit("上海");
        householdVO.setHouseholdAddress("广州");
        householdVO.setIdentityCard("441622199002167192");
        List<EmergencyContactDTO> contactDTOS = new ArrayList<>();
        EmergencyContactDTO eDto = new EmergencyContactDTO();
        eDto.setName("刘星");
        eDto.setPhone("15889898444");
        EmergencyContactDTO eDto2 = new EmergencyContactDTO();
        eDto2.setName("黄尚");
        eDto2.setPhone("15989898555");
        contactDTOS.add(eDto);
        contactDTOS.add(eDto2);
        householdVO.setContacts(contactDTOS);
        List<MemberDTO> userList = new ArrayList<>();
        MemberDTO dto2 = new MemberDTO();
        dto2.setUserName("刘夕媛");
        dto2.setRelationship(2);
        dto2.setRelationshipDesc("父女");
        dto2.setSex(2);
        userList.add(dto2);
        householdVO.setMembers(userList);
        householdFacade.saveHouseholds(householdVO);
        System.out.println("录入完成...");
    }

    @Test
    public void testListHousehold(){
        HouseholdPageQuery query = new HouseholdPageQuery();
        query.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        query.setUserName("刘夕媛");
        Page<Household> householdPage = householdFacade.listHouseholds(query);
        System.out.println("aaa1111 = " + householdPage.getRecords());
        query.setPhone("15889898240");
        householdPage = householdFacade.listHouseholds(query);
        System.out.println("bbbb222 = " + householdPage.getRecords());

        Household toGet = householdFacade.getHouseholdDetail(new ObjectId("5bfe4d08852e3ac946a30912"));
        System.out.println(toGet);
    }

    @Test
    public void testHouseholdDetailByRoom(){
        HouseholdVO householdVO = householdFacade.findDetailByRoom(new ObjectId("5aa1f3a49ce9d549052cfcd0"));
        System.out.println(householdVO);
    }

    /**
     * 导出数据，单sheet
     * @throws IOException
     */
    @Test
    public void testExport() throws IOException {

        HouseholdPageQuery query = new HouseholdPageQuery();
        query.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        query.setSize(30);
        Page<Household> households = householdFacade.listHouseholds(query);
        List<HouseholdEntity> entities = new ArrayList<>();
        for (Household household : households.getRecords()) {
            HouseholdEntity entity = new HouseholdEntity();
            BeanUtils.copyProperties(household, entity);
            entity.setRoomId(household.getRoomId().toString());
            entities.add(entity);
        }

        ExportParams params = new ExportParams();
        params.setSheetName("住房档案登记表");
        params.setStyle(ExcelExportStylerImpl.class);
        Workbook workbook = ExcelExportUtil.exportExcel(params, HouseholdEntity.class, entities);
        ExcelExportStylerImpl styler = new ExcelExportStylerImpl(workbook);
        CellStyle cellStyle = styler.getDefaultStyle(Font.COLOR_RED);
        cellStyle.setLocked(true);
        workbook.getSheetAt(0).getRow(0).getCell(0).setCellStyle(cellStyle);
        workbook.getSheetAt(0).getRow(0).getCell(1).setCellStyle(cellStyle);
        workbook.getSheetAt(0).getRow(0).getCell(2).setCellStyle(cellStyle);
        workbook.getSheetAt(0).getRow(0).getCell(3).setCellStyle(cellStyle);
        File excel = new File("D:/excel/");
        if (!excel.exists()) {
            excel.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream("D:/excel/households.xls");
        workbook.write(fos);
        fos.close();
    }

    /**
     * 导出多个sheet
     * @throws IOException
     */
    @Test
    public void testExportMoreSheets() throws IOException {

        HouseholdPageQuery query = new HouseholdPageQuery();
        query.setCommunityId(new ObjectId("5a82adf3b06c97e0cd6c0f3d"));
        query.setSize(30);
        Page<Household> households = householdFacade.listHouseholds(query);

        List<Map<String, Object>> list = new ArrayList<>();
        List<HouseholdEntity> entities = new ArrayList<>();
        int num = 1;
        for (Household household : households.getRecords()) {
            HouseholdEntity entity = new HouseholdEntity();
            entity.setRoomId(household.getRoomId().toString());
            entity.setRoomLocation(household.getRoomLocation());
            entity.setUserName(household.getUserName());
            entity.setPhone(household.getPhone());
            entity.setIdentityCard(household.getIdentityCard());
            entities.add(entity);
            if (entities.size() == 10) {
                ExportParams params = new ExportParams("住房档案登记表", "住房档案登记表" + num);
                Map<String, Object> map = new HashMap<>();
                map.put("title", params);
                map.put("entity", HouseholdEntity.class);
                map.put("data", entities);
                list.add(map);
                entities = new ArrayList<>();
                num ++;
            }
        }

        Workbook workbook = ExcelExportUtil.exportExcel(list, ExcelType.HSSF);
        File excel = new File("D:/excel/");
        if (!excel.exists()) {
            excel.mkdirs();
        }
        FileOutputStream fos = new FileOutputStream("D:/excel/householdMoreSheets.xls");
        workbook.write(fos);
        fos.close();
    }

    @Test
    public void testImport() {
        ImportParams params = new ImportParams();
        params.setTitleRows(1);
        params.setHeadRows(1);
        File file = new File("D:/excel/和谐警苑住房档案登记表.xls");
        List<HouseholdEntity> list = ExcelImportUtil.importExcel(file, HouseholdEntity.class, params);
        System.out.println(list);
    }

}
