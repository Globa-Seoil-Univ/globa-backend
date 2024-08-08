package org.y2k2.globa.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.y2k2.globa.dto.DictionaryDto;
import org.y2k2.globa.dto.ResponseDictionaryDto;
import org.y2k2.globa.exception.DictionaryException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Component
@Slf4j
public class Excel {
    @Value("${dictionary.path}")
    private String dictionaryPath;

    private List<File> getExcelList() {
        List<File> excelFiles = new ArrayList<>();

        File directory = new File(dictionaryPath);
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".xls"));
            if (files != null) {
                excelFiles.addAll(Arrays.asList(files));
            }
        } else {
            throw new IllegalArgumentException("Directory not found: " + dictionaryPath);
        }

        return excelFiles;
    }

    public List<DictionaryDto> getDictionaryDto() {
        List<File> excelFiles = getExcelList();

        try {
            List<DictionaryDto> dictionaryDtos = new ArrayList<>();

            for (File file : excelFiles) {
                System.out.println(file);
                Workbook workbook = new HSSFWorkbook(new BufferedInputStream(new FileInputStream(file)));
                Sheet sheet = workbook.getSheetAt(0);

                for (int i = 1; i < sheet.getPhysicalNumberOfRows(); i++) {
                    Row row = sheet.getRow(i);

                    if (row != null) {
                        Cell wordCell = row.getCell(0);
                        Cell otherWordCell = row.getCell(4);
                        Cell pronunciationCell = row.getCell(6);
                        Cell categoryCell = row.getCell(9);
                        Cell descriptionCell = row.getCell(11);

                        if (wordCell == null) {
                            continue;
                        }

                        String word = wordCell.getStringCellValue().replaceAll("\\^", " ").replaceAll("-", "");
                        String otherWord = otherWordCell == null || otherWordCell.getStringCellValue().trim().isEmpty()
                                ? null
                                : otherWordCell.getStringCellValue()
                                    .trim()
                                    .replaceAll("[가-힣]","")
                                    .replaceAll("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]+", "")
                                    .replaceAll("[^\\p{IsHan}\\w\\s]", "");
                        String pronunciation = pronunciationCell == null || pronunciationCell.getStringCellValue().trim().isEmpty()
                                ? null
                                : pronunciationCell.getStringCellValue().trim();
                        String category = categoryCell == null || categoryCell.getStringCellValue().trim().isEmpty()
                                ? null
                                : categoryCell.getStringCellValue().trim();
                        String description = descriptionCell == null || descriptionCell.getStringCellValue().trim().isEmpty()
                                ? null
                                : descriptionCell.getStringCellValue().trim();

                        dictionaryDtos.add(new DictionaryDto(word, otherWord, description, category, pronunciation));
                    }
                }
            }

            return dictionaryDtos;
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new DictionaryException("Dictionary Error");
        }
    }
}
