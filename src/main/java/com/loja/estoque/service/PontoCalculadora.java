package com.loja.estoque.service;

import com.loja.estoque.dto.PontoDtos.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Regras de calculo do ponto, em Java puro (sem Spring) para ficar simples de testar.
 *
 * Resumo das regras:
 *  - batidas do dia alternam ENTRADA, SAIDA, ENTRADA, SAIDA...
 *  - horas trabalhadas = soma dos pares entrada->saida
 *  - se o dia tem so 2 batidas e a escala tem intervalo, o intervalo e descontado (pre-assinalado)
 *  - atraso = 1a entrada depois do horario da escala + tolerancia (conta o atraso inteiro)
 *  - faltante / extra so contam em dias ja encerrados e acima da tolerancia
 *  - dia escalado sem nenhuma batida e encerrado = FALTA (faltante = jornada inteira)
 *  - dia encerrado com batida sem par (esqueceu a saida) = INCOMPLETO: nao gera desconto
 *    ate o administrador ajustar
 */
public final class PontoCalculadora {

    private PontoCalculadora() {}

    private static final String[] DIAS_ABREV = {"SEG", "TER", "QUA", "QUI", "SEX", "SAB", "DOM"};

    /** Escala de trabalho de um funcionario. inicio = primeiro dia em que o ponto passa a valer. */
    public record Escala(LocalDate inicio, LocalTime entrada, LocalTime saida,
                         int tolerancia, int intervalo, Set<DayOfWeek> dias, BigDecimal salario) {

        public int minutosEsperados() {
            int bruto = (int) Duration.between(entrada, saida).toMinutes();
            return Math.max(0, bruto - intervalo);
        }
    }

    public static String abreviar(LocalDate data) {
        return DIAS_ABREV[data.getDayOfWeek().getValue() - 1];
    }

    public static String descricaoEscala(Escala e) {
        return hhmm(e.entrada()) + " - " + hhmm(e.saida());
    }

    private static String hhmm(LocalTime t) {
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    /** Define ENTRADA/SAIDA de cada batida pela ordem no dia. */
    public static List<BatidaDTO> numerar(List<BatidaDTO> batidas) {
        List<BatidaDTO> out = new ArrayList<>(batidas.size());
        for (int i = 0; i < batidas.size(); i++) {
            BatidaDTO b = batidas.get(i);
            out.add(new BatidaDTO(b.id(), b.dataHora(), i % 2 == 0 ? "ENTRADA" : "SAIDA",
                    b.origem(), b.ajustada(), b.dataHoraOriginal(), b.ajustadoPor(), b.justificativa()));
        }
        return out;
    }

    /** batidas: ja ordenadas por horario (crescente). */
    public static DiaDTO calcularDia(Escala e, LocalDate data, List<BatidaDTO> batidas, LocalDateTime agora) {
        String abrev = abreviar(data);

        // dias anteriores ao inicio do controle de ponto nao contam
        if (e.inicio() != null && data.isBefore(e.inicio())) {
            return new DiaDTO(data, abrev, false, "SEM_CONTROLE", false, false, false,
                    List.of(), 0, 0, 0, 0);
        }

        List<BatidaDTO> lista = numerar(batidas);
        int n = lista.size();
        LocalDate hoje = agora.toLocalDate();
        boolean fechado = data.isBefore(hoje);
        boolean escalado = e.dias().contains(data.getDayOfWeek());
        boolean impar = n % 2 == 1;
        boolean incompleto = fechado && impar;
        boolean emExpediente = data.equals(hoje) && impar;

        int trabalhados = 0;
        for (int i = 0; i + 1 < n; i += 2) {
            trabalhados += (int) Duration.between(lista.get(i).dataHora(), lista.get(i + 1).dataHora()).toMinutes();
        }
        if (n == 2 && e.intervalo() > 0) {
            trabalhados = Math.max(0, trabalhados - e.intervalo());
        }

        int esperado = escalado ? e.minutosEsperados() : 0;

        int atraso = 0;
        if (escalado && n > 0) {
            long diff = Duration.between(data.atTime(e.entrada()), lista.get(0).dataHora()).toMinutes();
            if (diff > e.tolerancia()) {
                atraso = (int) diff;
            }
        }

        String situacao;
        if (!escalado) {
            situacao = n > 0 ? "FOLGA_TRABALHADA" : "FOLGA";
        } else if (n == 0) {
            if (fechado) {
                situacao = "FALTA";
            } else if (data.isAfter(hoje)) {
                situacao = "AGUARDANDO";
            } else {
                boolean passouTolerancia = agora.isAfter(data.atTime(e.entrada()).plusMinutes(e.tolerancia()));
                situacao = passouTolerancia ? "NAO_BATEU" : "AGUARDANDO";
            }
        } else {
            situacao = atraso > 0 ? "ATRASO" : "PONTUAL";
        }

        int faltantes = 0;
        int extras = 0;
        if (fechado) {
            if (escalado) {
                if (n == 0) {
                    faltantes = esperado;
                } else if (!incompleto) {
                    int falta = esperado - trabalhados;
                    int sobra = trabalhados - esperado;
                    if (falta > e.tolerancia()) faltantes = falta;
                    if (sobra > e.tolerancia()) extras = sobra;
                }
            } else if (n > 0 && !incompleto) {
                extras = trabalhados;
            }
        }

        boolean ajustado = lista.stream().anyMatch(BatidaDTO::ajustada);

        return new DiaDTO(data, abrev, escalado, situacao, emExpediente, incompleto, ajustado,
                lista, trabalhados, atraso, faltantes, extras);
    }

    /** Dias do mes (do inicio do controle ate hoje), em ordem crescente. */
    public static List<DiaDTO> espelhoDoMes(Escala e, YearMonth mes,
                                            Map<LocalDate, List<BatidaDTO>> porDia, LocalDateTime agora) {
        LocalDate hoje = agora.toLocalDate();
        LocalDate ini = mes.atDay(1);
        if (e.inicio() != null && e.inicio().isAfter(ini)) ini = e.inicio();
        LocalDate fim = mes.atEndOfMonth();
        if (fim.isAfter(hoje)) fim = hoje;

        List<DiaDTO> dias = new ArrayList<>();
        for (LocalDate d = ini; !d.isAfter(fim); d = d.plusDays(1)) {
            dias.add(calcularDia(e, d, porDia.getOrDefault(d, List.of()), agora));
        }
        return dias;
    }

    public static TotaisDTO totalizar(List<DiaDTO> dias) {
        int trabalhados = 0, falta = 0, atraso = 0, incompletos = 0;
        int minTrab = 0, minAtraso = 0, minFaltantes = 0, minExtras = 0;
        for (DiaDTO d : dias) {
            if (!d.batidas().isEmpty()) trabalhados++;
            if ("FALTA".equals(d.situacao())) falta++;
            if (d.minutosAtraso() > 0) atraso++;
            if (d.incompleto()) incompletos++;
            minTrab += d.minutosTrabalhados();
            minAtraso += d.minutosAtraso();
            minFaltantes += d.minutosFaltantes();
            minExtras += d.minutosExtras();
        }
        return new TotaisDTO(trabalhados, falta, atraso, incompletos, minTrab, minAtraso, minFaltantes, minExtras);
    }

    /**
     * Estimativa de pagamento do mes: desconto = minutos faltantes x valor do minuto,
     * onde valor do minuto = salario / (dias escalados no mes x minutos da jornada).
     * Horas extras NAO sao somadas (apenas informadas).
     */
    public static FolhaDTO calcularFolha(Escala e, YearMonth mes, TotaisDTO totais) {
        BigDecimal salario = e.salario() == null ? BigDecimal.ZERO : e.salario();

        int diasEscalados = 0;
        for (int d = 1; d <= mes.lengthOfMonth(); d++) {
            if (e.dias().contains(mes.atDay(d).getDayOfWeek())) diasEscalados++;
        }
        int minutosDia = e.minutosEsperados();
        long base = (long) diasEscalados * minutosDia;

        BigDecimal valorMinuto = base > 0
                ? salario.divide(BigDecimal.valueOf(base), 8, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal desconto = valorMinuto.multiply(BigDecimal.valueOf(totais.minutosFaltantes()))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal salarioBase = salario.setScale(2, RoundingMode.HALF_UP);
        if (desconto.compareTo(salarioBase) > 0) desconto = salarioBase;

        BigDecimal valorHora = valorMinuto.multiply(BigDecimal.valueOf(60)).setScale(2, RoundingMode.HALF_UP);
        return new FolhaDTO(salarioBase, diasEscalados, minutosDia, valorHora, desconto, salarioBase.subtract(desconto));
    }

    public static ContadoresDiaDTO contar(List<DiaDTO> dias) {
        int escalados = 0, bateram = 0, naoBateram = 0, atrasados = 0, emFolga = 0;
        for (DiaDTO d : dias) {
            if (d.escalado()) escalados++; else emFolga++;
            if (!d.batidas().isEmpty()) bateram++;
            if ("FALTA".equals(d.situacao()) || "NAO_BATEU".equals(d.situacao())) naoBateram++;
            if (d.minutosAtraso() > 0) atrasados++;
        }
        return new ContadoresDiaDTO(escalados, bateram, naoBateram, atrasados, emFolga);
    }
}
