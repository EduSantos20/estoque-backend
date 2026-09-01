package com.loja.estoque.model;

/** As 3 grades de estoque da loja, controladas de forma independente. */
public enum Categoria {
    CAMPO("Campo"),
    FUTSAL("Futsal"),
    SOCIETY("Society");

    private final String label;

    Categoria(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
