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



}
