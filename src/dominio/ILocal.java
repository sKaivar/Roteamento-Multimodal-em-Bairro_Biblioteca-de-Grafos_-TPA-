package dominio;

/**
 * Local do bairro — o tipo {@code T} guardado nos vértices dos quatro grafos.
 *
 * <p>Implementação esperada: classe {@code Local implements ILocal}, imutável, com
 * {@code equals}/{@code hashCode} baseados SOMENTE no {@code id} (o grafo procura vértices
 * por valor, e dois objetos com o mesmo id representam o mesmo lugar).
 */
public interface ILocal {

    /** @return identificador único (ex.: "I0123", "P0004", "L0012", "P0004@0160-1"). */
    String getId();

    /** @return nome legível (ex.: "Rua Carlos Martins × Rua Italina Pereira Motta"). */
    String getNome();

    double getLatitude();

    double getLongitude();

    TipoLocal getTipo();

    /**
     * Distância em linha reta (haversine), em metros. Serve à heurística do A* opcional e
     * a ligar ao grafo um local acrescentado pelo usuário (interseção mais próxima).
     */
    double distanciaMetros(ILocal outro);
}
