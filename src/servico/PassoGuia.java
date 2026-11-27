package servico;

import dominio.IPassoGuia;
import dominio.MeioTransporte;

/**
 * Representa uma instrução individual do guia textual de uma rota calculada.
 */
public class PassoGuia implements IPassoGuia {

    private final MeioTransporte meio;
    private final String descricao;
    private final float minutos;

    /**
     * Cria um passo do guia de deslocamento.
     *
     * @param meio meio de transporte utilizado neste passo
     * @param descricao descrição textual legível para o usuário
     * @param minutos duração estimada do passo em minutos (não negativa)
     * @throws IllegalArgumentException se o meio for nulo ou o tempo for negativo
     */
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

    /**
     * @return o meio de transporte empregado neste passo
     */
    @Override
    public MeioTransporte getMeio() {
        return meio;
    }

    /**
     * @return o texto da instrução do passo
     */
    @Override
    public String getDescricao() {
        return descricao;
    }

    /**
     * @return a duração do passo em minutos
     */
    @Override
    public float getMinutos() {
        return minutos;
    }

    /**
     * @return representação textual formatada do passo com sua duração
     */
    @Override
    public String toString() {
        return String.format("- %s (%.1f min)", descricao, minutos);
    }
}
