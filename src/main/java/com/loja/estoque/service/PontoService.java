package com.loja.estoque.service;

import com.loja.estoque.dto.PontoDtos.*;
import com.loja.estoque.exception.NegocioException;
import com.loja.estoque.model.Ponto;
import com.loja.estoque.model.Usuario;
import com.loja.estoque.repository.PontoRepository;
import com.loja.estoque.repository.UsuarioRepository;
import com.loja.estoque.service.PontoCalculadora.Escala;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class PontoService {

    private final PontoRepository pontoRepository;
    private final UsuarioRepository usuarioRepository;

    // =====================================================================
    //  Funcionario (usuario logado)
    // =====================================================================

    public StatusDTO status(String username) {
        Usuario u = usuarioPorUsername(username);
        LocalDateTime agora = LocalDateTime.now();
        boolean controla = u.isAtivo() && u.isControlaPonto();

        if (!controla) {
            return new StatusDTO(false, "ENTRADA", List.of(), agora, null);
        }

        List<BatidaDTO> hoje = PontoCalculadora.numerar(batidasDoDia(u.getId(), agora.toLocalDate()));
        String proximo = hoje.size() % 2 == 0 ? "ENTRADA" : "SAIDA";
        return new StatusDTO(true, proximo, hoje, agora, PontoCalculadora.descricaoEscala(escalaDe(u)));
    }

    /** O funcionario bate o proprio ponto. O horario e SEMPRE o do servidor. */
    @Transactional
    public StatusDTO bater(String username) {
        Usuario u = usuarioPorUsername(username);
        if (!u.isAtivo() || !u.isControlaPonto()) {
            throw new NegocioException("Seu usuário não está habilitado para bater ponto. Fale com o administrador.");
        }

        LocalDateTime agora = LocalDateTime.now().withNano(0);

        // protecao contra clique duplo / batida acidental repetida
        pontoRepository.findFirstByUsuarioIdAndRemovidaFalseOrderByDataHoraDesc(u.getId()).ifPresent(ultima -> {
            if (ultima.getDataHora().isAfter(agora.minusSeconds(60))) {
                throw new NegocioException("Você acabou de bater o ponto. Aguarde 1 minuto para registrar novamente.");
            }
        });

        pontoRepository.save(Ponto.builder()
                .usuario(u)
                .dataHora(agora)
                .origem(Ponto.Origem.BATIDA)
                .build());

        return status(username);
    }

    /** Espelho de ponto do proprio usuario (sem valores em dinheiro). */
    public EspelhoDTO meuEspelho(String username, String mes) {
        Usuario u = usuarioPorUsername(username);
        if (!u.isAtivo() || !u.isControlaPonto()) {
            throw new NegocioException("Seu usuário não está habilitado para bater ponto.");
        }
        return espelho(u, parseMes(mes), false);
    }

    // =====================================================================
    //  Administrador
    // =====================================================================

    /** Registro geral do dia: quem bateu, quem nao bateu, quem atrasou. */
    public ResumoDiaDTO resumoDia(String dataStr) {
        LocalDate data = parseData(dataStr);
        LocalDateTime agora = LocalDateTime.now();

        List<Usuario> usuarios = usuariosComPonto();
        List<Ponto> pontos = pontoRepository.findByRemovidaFalseAndDataHoraBetweenOrderByDataHoraAsc(
                data.atStartOfDay(), data.atTime(LocalTime.MAX));

        Map<Long, List<BatidaDTO>> porUsuario = new HashMap<>();
        for (Ponto p : pontos) {
            porUsuario.computeIfAbsent(p.getUsuario().getId(), k -> new ArrayList<>()).add(toBatida(p));
        }

        List<LinhaDiaDTO> linhas = new ArrayList<>();
        List<DiaDTO> dias = new ArrayList<>();
        for (Usuario u : usuarios) {
            Escala e = escalaDe(u);
            DiaDTO dia = PontoCalculadora.calcularDia(e, data, porUsuario.getOrDefault(u.getId(), List.of()), agora);
            dias.add(dia);
            linhas.add(new LinhaDiaDTO(u.getId(), nome(u), PontoCalculadora.descricaoEscala(e), dia));
        }
        return new ResumoDiaDTO(data, linhas, PontoCalculadora.contar(dias));
    }

    /** Fechamento do mes de todos os funcionarios: faltas, atrasos, desconto e valor a pagar. */
    public ResumoMesDTO resumoMes(String mesStr) {
        YearMonth mes = parseMes(mesStr);

        List<ResumoUsuarioMesDTO> lista = new ArrayList<>();
        BigDecimal totalBase = BigDecimal.ZERO;
        BigDecimal totalDescontos = BigDecimal.ZERO;
        BigDecimal totalFinal = BigDecimal.ZERO;
        int pendentes = 0;

        for (Usuario u : usuariosComPonto()) {
            EspelhoDTO esp = espelho(u, mes, true);
            lista.add(new ResumoUsuarioMesDTO(u.getId(), esp.nome(), esp.escala(), esp.totais(), esp.folha()));
            totalBase = totalBase.add(esp.folha().salarioBase());
            totalDescontos = totalDescontos.add(esp.folha().desconto());
            totalFinal = totalFinal.add(esp.folha().valorFinal());
            pendentes += esp.totais().diasIncompletos();
        }
        return new ResumoMesDTO(mes.toString(), lista, totalBase, totalDescontos, totalFinal, pendentes);
    }

    /** Espelho completo de um funcionario (com folha), usado na tela de ajustes. */
    public EspelhoDTO espelhoDoUsuario(Long usuarioId, String mes) {
        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado"));
        if (!u.isControlaPonto()) {
            throw new NegocioException("Este usuário não está configurado para controle de ponto");
        }
        return espelho(u, parseMes(mes), true);
    }

    /** Lanca uma batida que faltou (esqueceu de bater, etc). */
    @Transactional
    public BatidaDTO adicionarBatida(Long usuarioId, BatidaRequest req, String admin) {
        validarJustificativa(req.justificativa());
        LocalDateTime dataHora = validarDataHora(req.dataHora());

        Usuario u = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado"));

        Ponto p = pontoRepository.save(Ponto.builder()
                .usuario(u)
                .dataHora(dataHora)
                .origem(Ponto.Origem.MANUAL)
                .ajustadoPor(admin)
                .ajustadoEm(LocalDateTime.now())
                .justificativa(req.justificativa().trim())
                .build());
        return toBatida(p);
    }

    /** Corrige o horario de uma batida. O horario original fica guardado. */
    @Transactional
    public BatidaDTO editarBatida(Long batidaId, BatidaRequest req, String admin) {
        validarJustificativa(req.justificativa());
        LocalDateTime novo = validarDataHora(req.dataHora());

        Ponto p = pontoRepository.findById(batidaId)
                .orElseThrow(() -> new NegocioException("Batida não encontrada"));
        if (p.isRemovida()) {
            throw new NegocioException("Esta batida já foi removida");
        }

        if (p.getDataHoraOriginal() == null && p.getOrigem() == Ponto.Origem.BATIDA && !novo.equals(p.getDataHora())) {
            p.setDataHoraOriginal(p.getDataHora());
        }
        p.setDataHora(novo);
        p.setAjustadoPor(admin);
        p.setAjustadoEm(LocalDateTime.now());
        p.setJustificativa(req.justificativa().trim());
        return toBatida(pontoRepository.save(p));
    }

    /** Remove uma batida (ex: batida duplicada ou feita por engano). Fica marcada no banco, nao e apagada. */
    @Transactional
    public void removerBatida(Long batidaId, String justificativa, String admin) {
        validarJustificativa(justificativa);
        Ponto p = pontoRepository.findById(batidaId)
                .orElseThrow(() -> new NegocioException("Batida não encontrada"));
        if (p.isRemovida()) {
            throw new NegocioException("Esta batida já foi removida");
        }
        p.setRemovida(true);
        p.setAjustadoPor(admin);
        p.setAjustadoEm(LocalDateTime.now());
        p.setJustificativa(justificativa.trim());
        pontoRepository.save(p);
    }

    // =====================================================================
    //  Auxiliares
    // =====================================================================

    private EspelhoDTO espelho(Usuario u, YearMonth mes, boolean incluirFolha) {
        Escala e = escalaDe(u);
        LocalDateTime agora = LocalDateTime.now();

        List<Ponto> pontos = pontoRepository.findByUsuarioIdAndRemovidaFalseAndDataHoraBetweenOrderByDataHoraAsc(
                u.getId(), mes.atDay(1).atStartOfDay(), mes.atEndOfMonth().atTime(LocalTime.MAX));

        Map<LocalDate, List<BatidaDTO>> porDia = new HashMap<>();
        for (Ponto p : pontos) {
            porDia.computeIfAbsent(p.getDataHora().toLocalDate(), k -> new ArrayList<>()).add(toBatida(p));
        }

        List<DiaDTO> dias = PontoCalculadora.espelhoDoMes(e, mes, porDia, agora);
        TotaisDTO totais = PontoCalculadora.totalizar(dias);
        FolhaDTO folha = incluirFolha ? PontoCalculadora.calcularFolha(e, mes, totais) : null;

        return new EspelhoDTO(u.getId(), nome(u), PontoCalculadora.descricaoEscala(e), mes.toString(), dias, totais, folha);
    }

    private List<BatidaDTO> batidasDoDia(Long usuarioId, LocalDate data) {
        List<BatidaDTO> out = new ArrayList<>();
        for (Ponto p : pontoRepository.findByUsuarioIdAndRemovidaFalseAndDataHoraBetweenOrderByDataHoraAsc(
                usuarioId, data.atStartOfDay(), data.atTime(LocalTime.MAX))) {
            out.add(toBatida(p));
        }
        return out;
    }

    private BatidaDTO toBatida(Ponto p) {
        boolean ajustada = p.getOrigem() == Ponto.Origem.MANUAL || p.getDataHoraOriginal() != null;
        return new BatidaDTO(p.getId(), p.getDataHora(), null, p.getOrigem().name(), ajustada,
                p.getDataHoraOriginal(), p.getAjustadoPor(), p.getJustificativa());
    }

    private List<Usuario> usuariosComPonto() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.isAtivo() && u.isControlaPonto())
                .sorted((a, b) -> nome(a).compareToIgnoreCase(nome(b)))
                .toList();
    }

    private Usuario usuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NegocioException("Usuário não encontrado"));
    }

    private String nome(Usuario u) {
        return (u.getNomeCompleto() != null && !u.getNomeCompleto().isBlank()) ? u.getNomeCompleto() : u.getUsername();
    }

    private Escala escalaDe(Usuario u) {
        if (u.getHoraEntrada() == null || u.getHoraSaida() == null) {
            throw new NegocioException("Escala de trabalho não configurada para " + nome(u));
        }
        Set<DayOfWeek> dias = new TreeSet<>();
        String cfg = (u.getDiasTrabalho() == null || u.getDiasTrabalho().isBlank()) ? "1,2,3,4,5,6" : u.getDiasTrabalho();
        for (String parte : cfg.split(",")) {
            String p = parte.trim();
            if (!p.isEmpty()) dias.add(DayOfWeek.of(Integer.parseInt(p)));
        }
        int tolerancia = u.getToleranciaMinutos() != null ? u.getToleranciaMinutos() : 10;
        int intervalo = u.getIntervaloMinutos() != null ? u.getIntervaloMinutos() : 0;
        return new Escala(u.getPontoDesde(), u.getHoraEntrada(), u.getHoraSaida(), tolerancia, intervalo, dias, u.getSalarioMensal());
    }

    private YearMonth parseMes(String mes) {
        if (mes == null || mes.isBlank()) return YearMonth.now();
        try {
            return YearMonth.parse(mes.trim());
        } catch (DateTimeParseException ex) {
            throw new NegocioException("Mês inválido. Use o formato AAAA-MM");
        }
    }

    private LocalDate parseData(String data) {
        if (data == null || data.isBlank()) return LocalDate.now();
        try {
            return LocalDate.parse(data.trim());
        } catch (DateTimeParseException ex) {
            throw new NegocioException("Data inválida. Use o formato AAAA-MM-DD");
        }
    }

    private void validarJustificativa(String justificativa) {
        if (justificativa == null || justificativa.trim().length() < 3) {
            throw new NegocioException("Informe a justificativa do ajuste (mínimo 3 caracteres)");
        }
        if (justificativa.trim().length() > 255) {
            throw new NegocioException("A justificativa deve ter no máximo 255 caracteres");
        }
    }

    private LocalDateTime validarDataHora(LocalDateTime dataHora) {
        if (dataHora == null) {
            throw new NegocioException("Informe a data e a hora da batida");
        }
        if (dataHora.isAfter(LocalDateTime.now().plusMinutes(1))) {
            throw new NegocioException("Não é possível lançar uma batida no futuro");
        }
        return dataHora.withNano(0);
    }
}
