package servico;

import dominio.IPassoGuia;
import dominio.MeioTransporte;

// Guarda uma instrucao do caminho (ex: caminhe 5 min na rua tal)
public class PassoGuia implements IPassoGuia {

    private final MeioTransporte meio;
    private final String descricao;
    private final float minutos;

    // Construtor
    public PassoGuia(MeioTransporte meio, String descricao, float minutos) {
        if (meio == null) {
            throw new IllegalArgumentException("Meio de transporte nao pode ser nulo.");
        }
        if (minutos < 0) {
            throw new IllegalArgumentException("Tempo nao pode ser negativo.");
        }

        this.meio = meio;
        this.descricao = (descricao == null) ? "" : descricao;
        this.minutos = minutos;
    }

    // Retorna o meio de transporte
    @Override
    public MeioTransporte getMeio() {
        return meio;
    }

    // Retorna o texto da instrucao
    @Override
    public String getDescricao() {
        return descricao;
    }

    // Retorna o tempo em minutos
    @Override
    public float getMinutos() {
        return minutos;
    }

    // Formata para exibicao
    @Override
    public String toString() {
        return String.format("- %s (%.1f min)", descricao, minutos);
    }
}
