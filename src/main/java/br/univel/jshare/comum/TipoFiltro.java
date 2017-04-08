package br.univel.jshare.comum;

public enum TipoFiltro {
    NOME("NOME"),
    TAMANHO_MIN("TAMANHO MINIMO"),
    TAMANHO_MAX("TAMANHO MAXIMO"),
    EXTENSAO("EXTENSAO"),;

    private String label;
    TipoFiltro(String label) {
        this.label = label;
    }

    public String toString() {
        return label;
    }
}