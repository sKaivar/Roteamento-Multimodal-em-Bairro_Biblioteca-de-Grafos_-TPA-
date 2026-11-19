package dominio;

/**
 * Trecho viário tal como lido da seção [TRECHOS] — dado BRUTO, ainda sem tempo.
 * O serviço converte cada trecho em até três arestas: uma (não dirigida) no grafo pedestre
 * e uma ou duas (dirigidas) no grafo ciclista.
 *
 * <p>Implementação esperada: classe {@code Trecho implements ITrecho}, imutável.
 */
public interface ITrecho {

    ILocal getOrigem();

    ILocal getDestino();

    /** @return comprimento em metros (atributo; o peso será minutos). */
    double getDistanciaMetros();

    /** @return nome da via, para o guia ("Rua X", "faixa de travessia", "ciclovia"). */
    String getNomeVia();

    /** @return {@code true} se caminhável (coluna pe = S). */
    boolean permitePedestre();

    /** @return sentido permitido à bicicleta (coluna bici). */
    SentidoBicicleta getSentidoBicicleta();

    /** @return {@code true} se ciclovia (tempo de bicicleta × 1/2). */
    boolean temCiclovia();
}
