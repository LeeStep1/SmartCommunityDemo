package cn.bit.facade.poi.utils;

import cn.afterturn.easypoi.excel.entity.params.MergeEntity;
import cn.bit.framework.utils.string.StringUtil;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;

import java.util.*;

public class PoiMergeCellUtil {

    private PoiMergeCellUtil() {
    }

    /**
     * 纵向合并相同内容的单元格
     *  @param sheet
     * @param mergeMap key--列,value--依赖的列,没有传空
     */
    public static void mergeCells(Sheet sheet, Map<Integer, int[]> mergeMap) {
        if (mergeMap.size() == 0) {
            return;
        }
        Map<Integer, MergeEntity> mergeDataMap = new HashMap<>();

        Row row;
        // 需要合并的列
        Set<Integer> mergeColumnIndexs = mergeMap.keySet();
        // 合并后的文本
        String text;
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            row = sheet.getRow(i);
            for (Integer index : mergeColumnIndexs) {
                if (row.getCell(index) == null) {
                    mergeDataMap.get(index).setEndRow(i);
                    continue;
                }
                text = row.getCell(index).getStringCellValue();
                if (StringUtil.isNotBlank(text)) {
                    hanlderMergeCells(index, i, text, mergeDataMap, sheet, row.getCell(index), mergeMap.get(index));
                    continue;
                }
                mergeCellOrContinue(index, mergeDataMap, sheet);
            }
        }
        if (mergeDataMap.size() <= 0) {
            return;
        }
        for (Integer index : mergeDataMap.keySet()) {
            if (mergeDataMap.get(index).getStartRow() == mergeDataMap.get(index).getEndRow()) {
                continue;
            }
            sheet.addMergedRegion(new CellRangeAddress(mergeDataMap.get(index).getStartRow(), mergeDataMap.get(index).getEndRow(), index, index));
        }
    }

    /**
     * 处理合并单元格
     *
     * @param index
     * @param rowNum
     * @param text
     * @param mergeDataMap
     * @param sheet
     * @param cell
     * @param delys
     */
    private static void hanlderMergeCells(Integer index, int rowNum, String text,
                                          Map<Integer, MergeEntity> mergeDataMap, Sheet sheet,
                                          Cell cell, int[] delys) {
        if (!mergeDataMap.containsKey(index)) {
            mergeDataMap.put(index, createMergeEntity(text, rowNum, cell, delys));
            return;
        }
        // 检查依赖列对应的 value 是否一致
        if (checkIsEqualByCellContents(mergeDataMap.get(index), text, cell, delys, rowNum)) {
            mergeDataMap.get(index).setEndRow(rowNum);
            return;
        }
        // value 不一致的情况下，如果开始行跟结束行相同，则不需要合并这一行，并且移除
        if (mergeDataMap.get(index).getStartRow() == mergeDataMap.get(index).getEndRow()) {
            mergeDataMap.remove(index);
            // 把新的单元格放入map
            mergeDataMap.put(index, createMergeEntity(text, rowNum, cell, delys));
            return;
        }
        sheet.addMergedRegion(
                new CellRangeAddress(mergeDataMap.get(index).getStartRow(), mergeDataMap.get(index).getEndRow(), index, index));
        mergeDataMap.put(index, createMergeEntity(text, rowNum, cell, delys));
    }

    /**
     * 字符为空的情况下判断
     *
     * @param index
     * @param mergeDataMap
     * @param sheet
     */
    private static void mergeCellOrContinue(Integer index, Map<Integer, MergeEntity> mergeDataMap,
                                            Sheet sheet) {
        if (mergeDataMap.containsKey(index)
                && mergeDataMap.get(index).getEndRow() != mergeDataMap.get(index).getStartRow()) {
            sheet.addMergedRegion(new CellRangeAddress(mergeDataMap.get(index).getStartRow(),
                    mergeDataMap.get(index).getEndRow(), index, index));
            mergeDataMap.remove(index);
        }
    }

    /**
     * 创建合并后的单元格
     *
     * @param text   单元格内容
     * @param rowNum 行号
     * @param cell   单元格
     * @param delys  依赖的列
     * @return
     */
    private static MergeEntity createMergeEntity(String text, int rowNum, Cell cell, int[] delys) {
        MergeEntity mergeEntity = new MergeEntity(text, rowNum, rowNum);
        List<String> list = new ArrayList<>(delys.length);
        mergeEntity.setRelyList(list);
        for (int i = 0; i < delys.length; i++) {
            list.add(getCellNotNullText(cell, delys[i], rowNum));
        }
        return mergeEntity;
    }

    /**
     * 检查待合并单元格的内容与依赖列的内容是否一致
     *
     * @param mergeEntity
     * @param text
     * @param cell
     * @param delys
     * @param rowNum
     * @return
     */
    private static boolean checkIsEqualByCellContents(MergeEntity mergeEntity, String text,
                                                      Cell cell, int[] delys, int rowNum) {
        // 没有依赖关系
        if (delys == null || delys.length == 0) {
            return mergeEntity.getText().equals(text);
        }
        // 不存在依赖关系
        if (!mergeEntity.getText().equals(text)) {
            return false;
        }

        for (int i = 0; i < delys.length; i++) {
            if (!getCellNotNullText(cell, delys[i], rowNum).equals(mergeEntity.getRelyList().get(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取一个单元格的值,确保这个单元格必须有值,不然向上查询
     *
     * @param cell
     * @param index
     * @param rowNum
     * @return
     */
    private static String getCellNotNullText(Cell cell, int index, int rowNum) {
        String temp = cell.getRow().getCell(index).getStringCellValue();
        while (StringUtil.isBlank(temp)) {
            temp = cell.getRow().getSheet().getRow(--rowNum).getCell(index).getStringCellValue();
        }
        return temp;
    }

}
