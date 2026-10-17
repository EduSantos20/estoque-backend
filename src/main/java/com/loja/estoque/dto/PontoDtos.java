package com.loja.estoque.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** DTOs do ponto digital (records imutaveis, agrupados num unico arquivo). */
public final class PontoDtos {

    private PontoDtos() {}

    /** Uma batida. "tipo" (ENTRADA/SAIDA) e calculado pela ordem das batidas no dia. */
    public record BatidaDTO(Long id, LocalDateTime dataHora, String tipo, String origem,
                            boolean ajustada, LocalDateTime dataHoraOriginal,
                            String ajustadoPor, String justificativa) {}

    /** Um dia do espelho de ponto, ja com atraso/faltante/extra calculados. */
    public record DiaDTO(LocalDate data, String diaSemana, boolean escalado, String situacao,
                         boolean emExpediente, boolean incompleto, boolean ajustado,
                         List<BatidaDTO> batidas,
                         int minutosTrabalhados, int minutosAtraso,
                         int minutosFaltantes, int minutosExtras) {}

    public record TotaisDTO(int diasTrabalhados, int diasFalta, int diasAtraso, int diasIncompletos,
                            int minutosTrabalhados, int minutosAtraso,
                            int minutosFaltantes, int minutosExtras) {}

    public record FolhaDTO(BigDecimal salarioBase, int diasEscalados, int minutosDia,
                           BigDecimal valorHora, BigDecimal desconto, BigDecimal valorFinal) {}

    public record EspelhoDTO(Long usuarioId, String nome, String escala, String mes,
                             List<DiaDTO> dias, TotaisDTO totais, FolhaDTO folha) {}

    public record LinhaDiaDTO(Long usuarioId, String nome, String escala, DiaDTO dia) {}

    public record ContadoresDiaDTO(int escalados, int bateram, int naoBateram, int atrasados, int emFolga) {}

    public record ResumoDiaDTO(LocalDate data, List<LinhaDiaDTO> linhas, ContadoresDiaDTO contadores) {}

    public record ResumoUsuarioMesDTO(Long usuarioId, String nome, String escala,
                                      TotaisDTO totais, FolhaDTO folha) {}

    public record ResumoMesDTO(String mes, List<ResumoUsuarioMesDTO> usuarios,
                               BigDecimal totalBase, BigDecimal totalDescontos, BigDecimal totalFinal,
                               int diasIncompletosPendentes) {}

    public record StatusDTO(boolean controlaPonto, String proximoTipo, List<BatidaDTO> batidasHoje,
                            LocalDateTime agora, String escala) {}

    // ---- requests ----
    public record BatidaRequest(LocalDateTime dataHora, String justificativa) {}

    public record RemoverBatidaRequest(String justificativa) {}
}
