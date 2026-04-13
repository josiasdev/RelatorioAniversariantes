package com.github.josiasdev.RelatorioAniversariantes.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DadosRelatorioDTO {
    private List<AniversarianteDTO> membros;
    private List<AniversarianteDTO> congregados;
    private List<CasamentoDTO> casamentos;
}