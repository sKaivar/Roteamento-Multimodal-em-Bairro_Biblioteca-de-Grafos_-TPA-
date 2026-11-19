package dominio;

/** As três modalidades comparadas pelo sistema. */
public enum Modalidade {
    A_PE("I — a pé"),
    A_PE_ONIBUS("II — a pé + ônibus"),
    BICICLETA("III — bicicleta");

    private final String descricao;

    Modalidade(String descricao) { this.descricao = descricao; }

    @Override
    public String toString() { return descricao; }
}
