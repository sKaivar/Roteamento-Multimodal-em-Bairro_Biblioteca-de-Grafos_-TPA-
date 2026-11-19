package dominio;

/**
 * Trecho entre duas paradas CONSECUTIVAS de uma linha (seção [TRECHOS_ONIBUS]); dirigido,
 * no sentido de percurso da linha.
 */
public interface ITrechoOnibus {

    ILinhaOnibus getLinha();

    /** @return parada física de partida (tipo PARADA). */
    ILocal getOrigem();

    /** @return parada física de chegada (tipo PARADA). */
    ILocal getDestino();

    /** @return distância percorrida pelo ônibus, em metros. */
    double getDistanciaMetros();
}
