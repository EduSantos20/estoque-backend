package com.loja.estoque.exception;

/** Excecao para regras de negocio (ex: estoque insuficiente, tamanho invalido). */
public class NegocioException extends RuntimeException {
    public NegocioException(String mensagem) {
        super(mensagem);
    }
}
