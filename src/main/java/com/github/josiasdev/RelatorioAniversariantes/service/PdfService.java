package com.github.josiasdev.RelatorioAniversariantes.service;

import com.github.josiasdev.RelatorioAniversariantes.dto.AniversarianteDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.CasamentoDTO;
import com.github.josiasdev.RelatorioAniversariantes.dto.DadosRelatorioDTO;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    public void gerarRelatorioPdf(String nomeArquivo, DadosRelatorioDTO dados, LocalDate inicio, LocalDate fim) throws IOException {
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, new FileOutputStream(nomeArquivo));
            document.open();

            adicionarCabecalho(document);

            String periodo = formatarData(inicio) + " a " + formatarData(fim);

            if (dados.getMembros() != null && !dados.getMembros().isEmpty()) {
                adicionarTabelaAniversariantes(document, "Membros - Aniversariantes de " + periodo, dados.getMembros());
            }

            if (dados.getCongregados() != null && !dados.getCongregados().isEmpty()) {
                adicionarTabelaAniversariantes(document, "Congregados - Aniversariantes de " + periodo, dados.getCongregados());
            }

            if (dados.getCasamentos() != null && !dados.getCasamentos().isEmpty()) {
                adicionarTabelaCasamentos(document, "Aniversariantes de Casamento - " + periodo, dados.getCasamentos());
            }

        } catch (DocumentException e) {
            throw new IOException("Erro ao gerar PDF: " + e.getMessage(), e);
        } finally {
            document.close();
        }
    }

    private void adicionarCabecalho(Document document) throws DocumentException {
        Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Color.BLACK);
        Font fontSubtitulo = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);

        Paragraph titulo = new Paragraph("IGREJA EVANGÉLICA ASSEMBLEIA DE DEUS DE QUIXADÁ\nMINISTÉRIO TEMPLO CENTRAL", fontTitulo);
        titulo.setAlignment(Element.ALIGN_CENTER);

        Paragraph endereco = new Paragraph("Rua Epitácio Pessoa, 1074, Centro, Quixadá, Ceará, CEP: 63900-133\n\n", fontSubtitulo);
        endereco.setAlignment(Element.ALIGN_CENTER);

        document.add(titulo);
        document.add(endereco);
    }

    private void adicionarTabelaAniversariantes(Document document, String titulo, List<AniversarianteDTO> lista) throws DocumentException {
        Font fontSessao = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        Paragraph pTitulo = new Paragraph(titulo, fontSessao);
        pTitulo.setSpacingAfter(10f);
        document.add(pTitulo);

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 5f, 1.5f, 4f}); // Proporção das colunas

        adicionarCabecalhoTabela(table, "DIA", "NOME", "IDADE", "CONGREGAÇÃO");

        Font fontDados = FontFactory.getFont(FontFactory.HELVETICA, 10);
        for (AniversarianteDTO aniv : lista) {
            table.addCell(criarCelula(aniv.getDia(), fontDados, Element.ALIGN_CENTER));
            table.addCell(criarCelula(aniv.getNome(), fontDados, Element.ALIGN_LEFT));
            table.addCell(criarCelula(aniv.getIdade(), fontDados, Element.ALIGN_CENTER));
            table.addCell(criarCelula(aniv.getCongregacao(), fontDados, Element.ALIGN_LEFT));
        }

        document.add(table);
        document.add(new Paragraph("\n")); // Espaço extra após a tabela
    }

    private void adicionarTabelaCasamentos(Document document, String titulo, List<CasamentoDTO> lista) throws DocumentException {
        Font fontSessao = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLACK);
        Paragraph pTitulo = new Paragraph(titulo, fontSessao);
        pTitulo.setSpacingAfter(10f);
        document.add(pTitulo);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1f, 6f, 3f});

        adicionarCabecalhoTabela(table, "DIA", "CASAL", "DATA CASAMENTO");

        Font fontDados = FontFactory.getFont(FontFactory.HELVETICA, 10);
        for (CasamentoDTO cas : lista) {
            table.addCell(criarCelula(cas.getDia(), fontDados, Element.ALIGN_CENTER));
            table.addCell(criarCelula(cas.getCasal(), fontDados, Element.ALIGN_LEFT));
            table.addCell(criarCelula(cas.getDataCasamento(), fontDados, Element.ALIGN_CENTER));
        }

        document.add(table);
        document.add(new Paragraph("\n"));
    }

    private void adicionarCabecalhoTabela(PdfPTable table, String... colunas) {
        Font fontCabecalho = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String coluna : colunas) {
            PdfPCell cell = new PdfPCell(new Phrase(coluna, fontCabecalho));
            cell.setBackgroundColor(new Color(0, 102, 204)); // Azul padrão
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private PdfPCell criarCelula(String texto, Font font, int alinhamento) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, font));
        cell.setHorizontalAlignment(alinhamento);
        cell.setPadding(4);
        return cell;
    }

    private String formatarData(LocalDate data) {
        return data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}