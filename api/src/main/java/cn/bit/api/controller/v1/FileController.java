package cn.bit.api.controller.v1;


import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import cn.bit.api.support.SessionUtil;
import cn.bit.api.support.annotation.Authorization;
import cn.bit.common.facade.community.model.Building;
import cn.bit.common.facade.community.model.Community;
import cn.bit.common.facade.community.model.Room;
import cn.bit.common.facade.community.service.CommunityFacade;
import cn.bit.facade.model.user.Household;
import cn.bit.facade.poi.entity.HouseholdEntity;
import cn.bit.facade.poi.styler.ExcelExportStylerImpl;
import cn.bit.facade.service.user.HouseholdFacade;
import cn.bit.facade.vo.user.userToRoom.EmergencyContactDTO;
import cn.bit.framework.utils.BeanUtils;
import cn.bit.framework.utils.DateUtils;
import cn.bit.framework.utils.IdentityCardUtils;
import cn.bit.framework.utils.string.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * 文件导入导出接口
 */
@RestController
@RequestMapping(value = "/v1/files", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
@Slf4j
public class FileController {

    @Autowired
    private HouseholdFacade householdFacade;

    @Resource
    private CommunityFacade commonCommunityFacade;

    private final String TODAY = DateUtils.getReqDate();

    private final String[] EXCEL_SUFFIX = {".xls", ".xlsx"};

    /**
     * 住房档案模板导出(只导出没有档案的房间)
     *
     * @return
     */
    @GetMapping(name = "住房档案模板导出(只导出没有档案的房间)", path = "/households/template/export")
    @Authorization
    public void exportHouseholdTemplate(HttpServletResponse response) {
        Community community = commonCommunityFacade.getCommunityByCommunityId(SessionUtil.getCommunityId());
        List<Building> buildings = commonCommunityFacade.listBuildingsByCommunityId(community.getId());
        // 获取已存在档案的房间集合
        Set<ObjectId> roomIds = householdFacade.listRoomIdsByCommunityId(SessionUtil.getCommunityId());
        List<HouseholdEntity> householdEntities = new ArrayList<>();
        for (Building building : buildings) {
            List<Room> rooms = commonCommunityFacade.listRoomsByBuildingId(building.getId());
//            rooms.sort(Comparator.comparing(Room::getFloorId).thenComparing(Room::getName));
            rooms.forEach(room -> {
                if (!roomIds.contains(room.getId())) {
                    HouseholdEntity entity = new HouseholdEntity();
                    entity.setBuildingId(building.getId().toString());
                    entity.setBuildingName(building.getName());
                    entity.setRoomId(room.getId().toString());
                    entity.setRoomName(room.getName());
                    entity.setRoomLocation(building.getName() + room.getName());
                    householdEntities.add(entity);
                }
            });
        }
        String sheetName = "住房档案";
        String title = community.getName() + sheetName;
        ExportParams params = new ExportParams();
        params.setSheetName(sheetName);
        params.setStyle(ExcelExportStylerImpl.class);
        Workbook workbook = ExcelExportUtil.exportExcel(params, HouseholdEntity.class, householdEntities);
        ExcelExportStylerImpl styler = new ExcelExportStylerImpl(workbook);
        CellStyle colorCellStyle = styler.getDefaultStyle(Font.COLOR_RED);
        CellStyle unlockCellStyle = styler.getDefaultStyle(Font.COLOR_NORMAL);
        unlockCellStyle.setLocked(false);
        Sheet sheet = workbook.getSheetAt(0);
        // 设置密码保护
        sheet.protectSheet("");
        // 设置表头样式（红色）
        Row header = sheet.getRow(0);
        header.getCell(0).setCellStyle(colorCellStyle);
        header.getCell(1).setCellStyle(colorCellStyle);
        header.getCell(2).setCellStyle(colorCellStyle);
        header.getCell(3).setCellStyle(colorCellStyle);

        for (int r = 1; r < sheet.getLastRowNum() + 1; r++) {
            Row row = sheet.getRow(r);
            for (int c = 0; c < row.getLastCellNum(); c++) {
                Cell column = row.getCell(c);
                switch (c) {
                    case 0:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        break;
                    default:
                        column.setCellStyle(unlockCellStyle);
                        break;
                }
            }
        }
        try {
            response.setContentType("application/x-download;charset=iso8859-1");
            response.setHeader("Content-Disposition",
                    "attachment;filename=" + URLEncoder.encode(title + TODAY + EXCEL_SUFFIX[0], "utf8"));
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            log.error("exportHouseholdTemplate IOException:", e);
        }
    }

    /**
     * 导入住房档案，返回校验未通过的数据
     *
     * @return
     */
    @PostMapping(name = "导入住房档案(返回校验未通过数据)", path = "/households/import")
    @Authorization
    public void importHouseholds(HttpServletResponse response,
                                 @RequestPart(value = "file", required = false) MultipartFile file,
                                 @RequestParam(value = "sendMsg", defaultValue = "false") Boolean sendMsg) throws IOException {
        if (file == null) {
            response.sendError(HttpServletResponse.SC_NO_CONTENT, "文件不能为空");
            return;
        }
        response.setCharacterEncoding("iso8859-1");
        if (!file.getOriginalFilename().toLowerCase().endsWith(EXCEL_SUFFIX[0])
                && !file.getOriginalFilename().toLowerCase().endsWith(EXCEL_SUFFIX[1])) {
            response.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "文件格式不正确");
            return;
        }
        ImportParams importParams = new ImportParams();
        // 标题行默认为0
//        params.setTitleRows(1);
        // 表头行默认为1
//        params.setHeadRows(1);

        /*// 数据处理
        IExcelDataHandler<HouseholdEntity> handler = new HouseholdEntityHandler();
        // 对应的列名 （@Excel中指定的name）
        String[] fields = new String[] {"房间ID"};
        handler.setNeedHandlerFields(fields);
        params.setDataHandler(handler);*/
        /*// 数据验证
        IExcelVerifyHandler<HouseholdEntity> verifyHandler = new HouseholdEntityVerifyHandler();
        params.setVerifyHandler(verifyHandler);*/

        // 默认使用 hibernate 的验证框架
        importParams.setNeedVerify(true);
        ExcelImportResult<HouseholdEntity> result = null;
        try {
            result = ExcelImportUtil.importExcelMore(file.getInputStream(), HouseholdEntity.class, importParams);
        } catch (Exception e) {
            log.error("importHouseholds Exception:", e);
            response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE, "文件解析失败");
            return;
        }

        List<HouseholdEntity> successList = result.getList();
        List<HouseholdEntity> failList = result.getFailList();
        log.info("校验未通过数量：{}，校验通过数量：{}", failList.size(), successList.size());
        // 写入数据库
        if (!successList.isEmpty()) {
            saveHouseholds(successList, failList, sendMsg);
        }
        if (failList.isEmpty()) {
            response.sendError(HttpServletResponse.SC_ACCEPTED, "数据导入成功");
            return;
        }
        log.info("{} 条数据校验失败，将返回错误文件", failList.size());
        ExportParams exportParams = new ExportParams();
        exportParams.setStyle(ExcelExportStylerImpl.class);
        Workbook workbook = ExcelExportUtil.exportExcel(exportParams, HouseholdEntity.class, failList);
        ExcelExportStylerImpl styler = new ExcelExportStylerImpl(workbook);
        CellStyle colorCellStyle = styler.getDefaultStyle(Font.COLOR_RED);
        CellStyle unlockCellStyle = styler.getDefaultStyle(Font.COLOR_NORMAL);
        unlockCellStyle.setLocked(false);
        Sheet sheet = workbook.getSheetAt(0);
        // 设置密码保护
        sheet.protectSheet("");
        // 设置表头样式（红色）
        Row header = sheet.getRow(0);
        header.getCell(0).setCellStyle(colorCellStyle);
        header.getCell(1).setCellStyle(colorCellStyle);
        header.getCell(2).setCellStyle(colorCellStyle);
        header.getCell(3).setCellStyle(colorCellStyle);

        for (int r = 1; r < sheet.getLastRowNum() + 1; r++) {
            Row row = sheet.getRow(r);
            for (int c = 0; c < row.getLastCellNum(); c++) {
                Cell column = row.getCell(c);
                switch (c) {
                    case 0:
                    case 6:
                    case 7:
                    case 8:
                    case 9:
                        break;
                    default:
                        column.setCellStyle(unlockCellStyle);
                        break;
                }
            }
        }
        try {
            response.setStatus(HttpServletResponse.SC_CREATED);
            response.setContentType("application/x-download;charset=iso8859-1");
            workbook.write(response.getOutputStream());
        } catch (IOException e) {
            log.error("importHouseholds IOException:", e);
        }
        return;
    }

    /**
     * 校验身份证合法性并写入数据库
     *
     * @param successList
     * @param failList
     * @param sendMsg
     */
    private void saveHouseholds(List<HouseholdEntity> successList, List<HouseholdEntity> failList, Boolean sendMsg) {
        // 获取已存在档案的房间集合
        Set<ObjectId> roomIds = householdFacade.listRoomIdsByCommunityId(SessionUtil.getCommunityId());
        List<Household> households = new ArrayList<>();

        for (HouseholdEntity entity : successList) {
            // 已经存在档案，跳过不做处理
            if (roomIds.contains(new ObjectId(entity.getRoomId()))) {
                log.info("{}（{}）已经存在档案，跳过处理", entity.getRoomLocation(), entity.getRoomId());
                continue;
            }
            // 校验身份证合法性
            if (!IdentityCardUtils.isValidIdentityCard(entity.getIdentityCard())) {
                log.info("身份证（{}）不合法，跳过处理", entity.getIdentityCard());
                failList.add(entity);
                continue;
            }
            Household household = new Household();
            BeanUtils.copyProperties(entity, household);
            if (StringUtil.isNotBlank(entity.getContactsPhone()) || StringUtil.isNotBlank(entity.getContactsName())) {
                EmergencyContactDTO contactDTO = new EmergencyContactDTO();
                contactDTO.setName(entity.getContactsName());
                contactDTO.setPhone(entity.getContactsPhone());
                household.setContacts(Arrays.asList(contactDTO));
            }
            household.setCommunityId(SessionUtil.getCommunityId());
            household.setBuildingId(new ObjectId(entity.getBuildingId()));
            household.setRoomId(new ObjectId(entity.getRoomId()));
            household.setRoomLocation(entity.getBuildingName() + entity.getRoomName());
            households.add(household);
        }

        if (households.isEmpty()) {
            log.info("没有需要导入的住房档案, end !!!");
            return;
        }
        // 写入数据库
        householdFacade.saveHouseholdsForImporting(households, sendMsg);
    }
}