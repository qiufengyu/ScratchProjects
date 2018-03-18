package com.example.hadoop.preprocess;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class PreProcess {
    private static final String rawFilePath = "raw";
    private static final String inputFilePath = "input";

    public PreProcess() {}

    public void excelReader() throws IOException, InvalidFormatException {
        File rawFolder = new File(rawFilePath);
        File[] listOfFiles = rawFolder.listFiles();
        DataFormatter dataFormatter = new DataFormatter();
        for(File f: listOfFiles) {
            if(f.isFile()) {
                String fileName = f.getName();
                String absoluteFile = f.getAbsolutePath();
                Workbook workbook = WorkbookFactory.create(new File(absoluteFile));
                System.out.println("读取文件" + fileName);
                // 只需要用到第一个 sheet
                // 后面的 sheet 只是前面总的 sheet 的分组
                Sheet sheet = workbook.getSheetAt(0);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(inputFilePath + "/" + fileName.replace("xls", "csv")), "utf-8"));
                // 由于每个文件的数据字段都完全相同，所以分开处理
                if (fileName.contains("2014")) {
                    int[] colIndex = {0, 5, 8, 9};
                    for(Row row: sheet) {
                        if(row.getRowNum() > 0 && row.getRowNum() < sheet.getLastRowNum()) {
                            ArrayList<String> rows = new ArrayList<String>();
                            for (int j: colIndex) {
                                rows.add(dataFormatter.formatCellValue(row.getCell(j)));
                            }
                            bw.write(String.join(",", rows));
                            bw.write("\n");
                            bw.flush();
                        }
                    }
                }
                else if (fileName.contains("2015")) {
                    int[] colIndex = {0, 4, 5, 6};
                    for(Row row: sheet) {
                        if(row.getRowNum() > 0 && row.getRowNum() < sheet.getLastRowNum()) {
                            ArrayList<String> rows = new ArrayList<String>();
                            for (int j: colIndex) {
                                rows.add(dataFormatter.formatCellValue(row.getCell(j)));
                            }
                            bw.write(String.join(",", rows));
                            bw.write("\n");
                            bw.flush();
                        }
                    }
                }
                else if (fileName.contains("2016") || fileName.contains("2017")) {
                    int[] colIndex = {0, 6, 9, 10};
                    for(Row row: sheet) {
                        if(row.getRowNum() > 0 && row.getRowNum() < sheet.getLastRowNum()) {
                            ArrayList<String> rows = new ArrayList<String>();
                            for (int j: colIndex) {
                                rows.add(dataFormatter.formatCellValue(row.getCell(j)));
                            }
                            bw.write(String.join(",", rows));
                            bw.write("\n");
                            bw.flush();
                        }
                    }
                }
            }
        }


    }


}
