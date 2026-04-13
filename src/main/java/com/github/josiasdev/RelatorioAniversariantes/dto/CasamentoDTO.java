package com.github.josiasdev.RelatorioAniversariantes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CasamentoDTO {
    private String dia;
    private String casal;
    private String dataCasamento;
}