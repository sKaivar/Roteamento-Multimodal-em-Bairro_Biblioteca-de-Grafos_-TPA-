package servico;

/**
 * Regras de tempo (seção 2 da especificação). Único lugar do sistema que transforma
 * metros em minutos — se o professor perguntar "onde está a premissa X?", a resposta é aqui.
 *
 * <p>Implementação esperada: {@code ModeloCusto}, com velocidades configuráveis no
 * construtor (padrões: pé 5 km/h, bicicleta 15 km/h, ônibus 20 km/h, espera 8 min).
 * Conversão: minutos = metros / (km/h × 1000 / 60).
 */
public interface IModeloCusto {

    /** @return minutos para caminhar {@code metros}. */
    float tempoCaminhada(double metros);

    /** @return minutos de bicicleta; multiplicado por 1/2 quando {@code ciclovia}. */
    float tempoBicicleta(double metros, boolean ciclovia);

    /** @return minutos de ônibus (permanência em paradas desprezada). */
    float tempoOnibus(double metros);

    /** @return espera fixa a cada embarque (8 min), peso das arestas de embarque. */
    float esperaEmbarque();
}
