package com.github.josiasdev.RelatorioAniversariantes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AniversarianteDTO {
    private String dia;
    private String nome;
    private String idade;
    private String congregacao;
}
