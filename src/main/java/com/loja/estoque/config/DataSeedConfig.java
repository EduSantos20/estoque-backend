package com.loja.estoque.config;

import com.loja.estoque.model.Categoria;
import com.loja.estoque.model.EstoqueTamanho;
import com.loja.estoque.model.Role;
import com.loja.estoque.model.Usuario;
import com.loja.estoque.repository.EstoqueTamanhoRepository;
import com.loja.estoque.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Roda uma unica vez na inicializacao:
 * - cria as linhas fixas de tamanho 34 a 43 para CADA uma das 3 categorias
 *   (Campo, Futsal, Society) -- 30 linhas no total -- se ainda nao existirem
 * - cria um usuario administrador padrao (se ainda nao existir nenhum usuario)
 */
@Component
@RequiredArgsConstructor
public class DataSeedConfig implements CommandLineRunner {

    private final EstoqueTamanhoRepository estoqueTamanhoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.estoque.tamanho-min}")
    private int tamanhoMin;

    @Value("${app.estoque.tamanho-max}")
    private int tamanhoMax;

    @Value("${app.estoque.meta-padrao}")
    private int metaPadrao;

    @Override
    public void run(String... args) {
        seedTamanhos();
        //seedAdmin();
    }

    private void seedTamanhos() {
        for (Categoria categoria : Categoria.values()) {
            for (int tamanho = tamanhoMin; tamanho <= tamanhoMax; tamanho++) {
                if (!estoqueTamanhoRepository.existsByCategoriaAndTamanho(categoria, tamanho)) {
                    EstoqueTamanho linha = EstoqueTamanho.builder()
                            .categoria(categoria)
                            .tamanho(tamanho)
                            .estoque(0)
                            .vendasSemana(0)
                            .aCaminho(0)
                            .meta(metaPadrao)
                            .build();
                    estoqueTamanhoRepository.save(linha);
                }
            }
        }
    }

//    private void seedAdmin() {
//        if (!usuarioRepository.existsByUsername("admin")) {
//            Usuario admin = Usuario.builder()
//                    .username("admin")
//                    .nomeCompleto("Administrador")
//                    .senha(passwordEncoder.encode("admin123"))
//                    .role(Role.ADMIN)
//                    .ativo(true)
//                    .build();
//            usuarioRepository.save(admin);
//            System.out.println("=====================================================");
//            System.out.println("Usuario admin criado. username=admin senha=admin123");
//            System.out.println("TROQUE ESSA SENHA assim que possivel!");
//            System.out.println("=====================================================");
//        }
//    }
}

