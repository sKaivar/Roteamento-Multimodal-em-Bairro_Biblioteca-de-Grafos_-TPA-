package dominio;

/** Coluna "bici" da seção [TRECHOS]. */
public enum SentidoBicicleta {
    /** Pedalável nos dois sentidos (rua de mão dupla ou ciclovia). */
    AMBOS,
    /** Pedalável só de origem para destino (rua de mão única). */
    IDA,
    /** Não pedalável (escadaria, calçada, travessia). */
    NENHUM
}
