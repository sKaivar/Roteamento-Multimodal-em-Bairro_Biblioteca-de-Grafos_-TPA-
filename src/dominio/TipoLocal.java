package dominio;

/** Natureza de um local (coluna "tipo" da seção [VERTICES] do arquivo). */
public enum TipoLocal {
    /** Cruzamento ou extremidade de via. Camada A (pedestre/ciclista). */
    INTERSECAO,
    /** Ponto de ônibus físico; é também vértice do grafo pedestre. Camada A. */
    PARADA,
    /** Local de interesse (escola, farmácia…), ligado à malha por um trecho de acesso. Camada A. */
    INTERESSE,
    /**
     * Ponto de ônibus DE UMA LINHA (camada B). Não vem do arquivo: é criado pelo serviço,
     * um por par (parada, linha), com id "P0004@0160-1". Separar por linha é o que faz a
     * baldeação custar uma nova espera de 8 min (desembarque 0 + embarque 8).
     */
    PARADA_LINHA
}
