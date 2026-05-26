package com.example.demo.infrastructure.adapters.output;

import com.example.demo.domain.model.DadosTermo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;

@Component
public class ExcelAdapter {
    private final String PATH = "/home/marcos/Documents/controle_documentos.xlsx";

    public void registrarNoExcel(DadosTermo dados) throws IOException {
        Workbook workbook;
        File file = new File(PATH);

        if (file.exists()) {
            workbook = new XSSFWorkbook(new FileInputStream(file));
        } else {
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Documentos");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("ID");
            header.createCell(1).setCellValue("Colaborador");
            header.createCell(2).setCellValue("Data");
        }

        Sheet sheet = workbook.getSheetAt(0);
        int lastRow = sheet.getLastRowNum();
        Row row = sheet.createRow(lastRow + 1);
        
        row.createCell(0).setCellValue(dados.getId());
        row.createCell(1).setCellValue(dados.getNomeColaborador());
        row.createCell(2).setCellValue(LocalDate.now().toString());

        try (FileOutputStream out = new FileOutputStream(PATH)) {
            workbook.write(out);
        }
        workbook.close();
    }
}