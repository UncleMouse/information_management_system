package com.example.information_management_system.util;

import cn.idev.excel.FastExcel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExportUtils {

    public static void exportToExcel(String filePath, List<String> headers, List<List<String>> data, String sheetName) {
        try {
            List<List<String>> head = headers.stream()
                    .map(Collections::singletonList)
                    .collect(Collectors.toList());

            FastExcel.write(filePath)
                    .head(head)
                    .sheet(sheetName)
                    .doWrite(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
