package br.edu.cpa.cpainteligente.entity.enums;

public enum SinaesAxis {
    ENSINO("Ensino"),
    PESQUISA("Pesquisa"),
    EXTENSAO("Extensão"),
    GESTAO("Gestão"),
    INFRAESTRUTURA("Infraestrutura");

    private final String label;

    SinaesAxis(String label) { this.label = label; }
    public String getLabel()  { return label; }
}