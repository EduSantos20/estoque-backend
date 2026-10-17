package com.loja.estoque.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Uma batida de ponto. O horario vem SEMPRE do servidor quando o proprio
 * funcionario bate (origem BATIDA). O administrador pode ajustar, lancar
 * (origem MANUAL) ou remover batidas -- tudo com justificativa e registro de
 * quem mexeu. Batidas removidas nao sao apagadas: ficam marcadas (removida).
 */
@Entity
@Table(name = "ponto_batida", indexes = {
        @Index(name = "idx_ponto_usuario_data", columnList = "usuario_id, data_hora")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ponto {

    public enum Origem { BATIDA, MANUAL }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "data_hora", nullable = false)
    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private Origem origem = Origem.BATIDA;

    /** Horario original, guardado na primeira vez que o admin ajusta a batida. */
    @Column(name = "data_hora_original")
    private LocalDateTime dataHoraOriginal;

    @Column(name = "ajustado_por", length = 60)
    private String ajustadoPor;

    @Column(name = "ajustado_em")
    private LocalDateTime ajustadoEm;

    @Column(length = 255)
    private String justificativa;

    @Column(nullable = false)
    @Builder.Default
    private boolean removida = false;
}
