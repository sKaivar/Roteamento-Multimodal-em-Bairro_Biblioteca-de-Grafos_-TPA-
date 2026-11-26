package servico;

import dominio.IPassoGuia;
import dominio.MeioTransporte;

/**
 * Representa uma instrução do trajeto para o usuário.
 * Exemplo: "Caminhe 5 min pela Rua Carlos Martins".
 */
public class PassoGuia implements IPassoGuia {

    private final MeioTransporte meio;
    private final String descricao;
    private final float minutos;

    // Construtor do passo
    public PassoGuia(MeioTransporte meio, String descricao, float minutos) {
        if (meio == null) {
            throw new IllegalArgumentException("O meio de transporte não pode ser nulo.");
        }
        if (minutos < 0) {
            throw new IllegalArgumentException("O tempo em minutos não pode ser negativo.");
        }

        this.meio = meio;
        this.descricao = (descricao == null) ? "" : descricao;
        this.minutos = minutos;
    }

    // Retorna o meio utilizado (a pé, ônibus, bike, espera)
    @Override
    public MeioTransporte getMeio() {
        return meio;
    }

    // Retorna a instrução em texto
    @Override
    public String getDescricao() {
        return descricao;
    }

    // Retorna o tempo estimado em minutos
    @Override
    public float getMinutos() {
        return minutos;
    }

    // Exibe o passo formatado
    @Override
    public String toString() {
        return String.format("- %s (%.1f min)", descricao, minutos);
    }
}
