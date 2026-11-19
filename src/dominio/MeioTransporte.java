package dominio;

/**
 * Meios em que se decompõe o tempo total de uma rota (saída 3 da especificação).
 *
 * <p>No grafo composto, o meio de cada aresta é deduzido das camadas dos extremos — sem
 * nenhum campo extra na biblioteca:
 * A→A = CAMINHADA; A→B (embarque) = ESPERA; B→B = ONIBUS; B→A (desembarque) = 0 min.
 */
public enum MeioTransporte {
    CAMINHADA, ESPERA, ONIBUS, BICICLETA
}
