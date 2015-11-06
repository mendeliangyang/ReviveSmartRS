/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Map;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Administrator
 */
public class PoiExcelHelper {

    /**
     *
     * @param exportMapDatas 导出的数据
     * @param columnsExplain 导出数据列明说明
     * @param sheetName
     * @param outFile 输入的文件位置
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static boolean productExcelFile(List<Map<String, String>> exportMapDatas, Map<String, String> columnsExplain, String sheetName, File outFile) throws FileNotFoundException, IOException {
        HSSFWorkbook workbook = new HSSFWorkbook();
        HSSFSheet sheet = workbook.createSheet(sheetName);
        //set columnTitle
        int rownum = 1, cellnum = 0;
        Set<String> keysSet = columnsExplain.keySet();
        Row columnRowTitle = sheet.createRow(0);
        for (String key : keysSet) {
            Cell columnCellTitle = columnRowTitle.createCell(cellnum++);
            columnCellTitle.setCellValue(columnsExplain.get(key));
        }
        for (Map<String, String> exportMapData : exportMapDatas) {
            Row sheetRow = sheet.createRow(rownum++);
            cellnum = 0;
            for (String key : keysSet) {
                Cell cell = sheetRow.createCell(cellnum++);
                cell.setCellValue(exportMapData.get(key));
            }
        }
        FileOutputStream out = new FileOutputStream(outFile);
        workbook.write(out);
        out.close();
        return true;

    }
}
