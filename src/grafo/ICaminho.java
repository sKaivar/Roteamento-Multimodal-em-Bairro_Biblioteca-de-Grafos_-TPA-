package grafo;

import estruturas.ILista;

/**
 * Resultado de uma consulta de caminho mínimo, reconstruído a partir do vetor de
 * predecessores do Dijkstra (do destino até a origem, inserindo no início da lista: O(V)).
 *
 * <p>Implementação esperada: classe {@code Caminho<T>} imutável, criada pelo próprio
 * {@code Grafo}. A camada de serviço lê as arestas, na ordem, para montar o guia textual
 * e somar os tempos parciais por meio de transporte.
 *
 * @param <T> tipo armazenado nos vértices
 */
public interface ICaminho<T> {

    /** @return {@code false} se o destino for inalcançável a partir da origem. */
    boolean existe();

    /** @return soma dos pesos (minutos); {@code Float.POSITIVE_INFINITY} se não existir. */
    float getCustoTotal();

    /** @return vértices na ordem origem → destino (vazia se não existir). */
    ILista<Vertice<T>> getVertices();

    /** @return arestas na ordem de percurso (vazia se não existir ou se origem = destino). */
    ILista<IAresta<T>> getArestas();
}
