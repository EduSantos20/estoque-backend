package com.loja.estoque.service;

import com.loja.estoque.dto.AtualizarUsuarioRequest;
import com.loja.estoque.dto.CriarUsuarioRequest;
import com.loja.estoque.exception.NegocioException;
import com.loja.estoque.model.Usuario;
import com.loja.estoque.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.TreeSet;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario criar(CriarUsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new NegocioException("Ja existe um usuario com este username");
        }
        Usuario usuario = Usuario.builder()
                .username(request.getUsername())
                .nomeCompleto(request.getNomeCompleto())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(request.getRole())
                .ativo(true)
                .build();

        aplicarConfigPonto(usuario, request.getControlaPonto(), request.getHoraEntrada(), request.getHoraSaida(),
                request.getToleranciaMinutos(), request.getIntervaloMinutos(), request.getDiasTrabalho(),
                request.getSalarioMensal(), request.getPontoDesde());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario atualizar(Long id, AtualizarUsuarioRequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario nao encontrado"));

        if (request.getNomeCompleto() != null) {
            usuario.setNomeCompleto(request.getNomeCompleto());
        }
        if (request.getSenha() != null && !request.getSenha().isBlank()) {
            if (request.getSenha().length() < 6) {
                throw new NegocioException("A senha deve ter pelo menos 6 caracteres");
            }
            usuario.setSenha(passwordEncoder.encode(request.getSenha()));
        }

        aplicarConfigPonto(usuario, request.getControlaPonto(), request.getHoraEntrada(), request.getHoraSaida(),
                request.getToleranciaMinutos(), request.getIntervaloMinutos(), request.getDiasTrabalho(),
                request.getSalarioMensal(), request.getPontoDesde());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NegocioException("Usuario nao encontrado"));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }

    /** Aplica e valida os campos de ponto/escala/salario. Campos nulos mantem o valor atual. */
    private void aplicarConfigPonto(Usuario u, Boolean controla, LocalTime entrada, LocalTime saida,
                                    Integer tolerancia, Integer intervalo, String dias,
                                    BigDecimal salario, LocalDate desde) {
        if (controla != null) u.setControlaPonto(controla);
        if (entrada != null) u.setHoraEntrada(entrada);
        if (saida != null) u.setHoraSaida(saida);

        if (tolerancia != null) {
            if (tolerancia < 0 || tolerancia > 60) {
                throw new NegocioException("A tolerância deve ficar entre 0 e 60 minutos");
            }
            u.setToleranciaMinutos(tolerancia);
        }
        if (intervalo != null) {
            if (intervalo < 0 || intervalo > 240) {
                throw new NegocioException("O intervalo deve ficar entre 0 e 240 minutos");
            }
            u.setIntervaloMinutos(intervalo);
        }
        if (dias != null && !dias.isBlank()) {
            String limpo = dias.replace(" ", "");
            if (!limpo.matches("^[1-7](,[1-7])*$")) {
                throw new NegocioException("Dias de trabalho inválidos");
            }
            TreeSet<Integer> ordenados = new TreeSet<>();
            for (String d : limpo.split(",")) ordenados.add(Integer.parseInt(d));
            u.setDiasTrabalho(String.join(",", ordenados.stream().map(String::valueOf).toList()));
        }
        if (salario != null) {
            if (salario.signum() < 0) {
                throw new NegocioException("O salário não pode ser negativo");
            }
            u.setSalarioMensal(salario);
        }
        if (desde != null) u.setPontoDesde(desde);

        if (u.isControlaPonto()) {
            if (u.getHoraEntrada() == null || u.getHoraSaida() == null) {
                throw new NegocioException("Informe o horário de entrada e de saída para controlar o ponto");
            }
            if (!u.getHoraSaida().isAfter(u.getHoraEntrada())) {
                throw new NegocioException("A saída deve ser depois da entrada (turnos que passam da meia-noite ainda não são suportados)");
            }
            int jornada = (int) Duration.between(u.getHoraEntrada(), u.getHoraSaida()).toMinutes();
            int descanso = u.getIntervaloMinutos() != null ? u.getIntervaloMinutos() : 0;
            if (jornada - descanso <= 0) {
                throw new NegocioException("O intervalo não pode ser maior que a jornada");
            }
            if (u.getDiasTrabalho() == null || u.getDiasTrabalho().isBlank()) {
                u.setDiasTrabalho("1,2,3,4,5,6");
            }
            if (u.getPontoDesde() == null) {
                u.setPontoDesde(LocalDate.now());
            }
        }
    }
}
