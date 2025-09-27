package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


@Service
public class ExcelService {
    public void gerarPlanilha(String nomeArquivo, List<AniversarianteDTO> dados) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Aniversariantes");
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);

            criarCabecalho(sheet, headerStyle);
            preencherDados(sheet, dados, centerStyle);

            for (int i = 0; i < 4; i++){
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream fileOut = new FileOutputStream(nomeArquivo)) {
                workbook.write(fileOut);
            }
        }
    }
    private void criarCabecalho(Sheet sheet, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        String[] columns = {"Dia", "Nome", "Idade", "Congregação"};
        for(int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    private void preencherDados(Sheet sheet, List<AniversarianteDTO> dados, CellStyle centerStyle){
        int rowNum = 1;
        for (AniversarianteDTO aniv: dados){
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(aniv.getDia());
            row.createCell(1).setCellValue(aniv.getNome());
            row.createCell(2).setCellValue(aniv.getIdade());
            row.createCell(3).setCellValue(aniv.getCongregacao());

            row.getCell(0).setCellStyle(centerStyle);
            row.getCell(2).setCellStyle(centerStyle);
        }


    }
    private CellStyle createHeaderStyle(Workbook workbook){
        CellStyle style = workbook.createCellStyle();
        style.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setBold(true);
        style.setFont(font);
        return style;
    }
    private CellStyle createCenterStyle(Workbook workbook){
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
}