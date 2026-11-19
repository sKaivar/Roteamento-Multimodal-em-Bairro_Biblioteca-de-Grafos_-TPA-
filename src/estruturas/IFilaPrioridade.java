package estruturas;

/**
 * Fila de prioridade de MÍNIMO, implementada como HEAP BINÁRIO próprio (vetor de pares
 * elemento/prioridade, com {@code subir} e {@code descer}). É o que dá ao Dijkstra a
 * complexidade O((V + E) log V).
 *
 * <p>Estratégia recomendada no Dijkstra: <b>inserção preguiçosa</b> (lazy deletion). Em vez
 * de uma operação "diminuir prioridade", reinsere-se o vértice sempre que sua distância
 * melhora; ao retirar um vértice já finalizado, ele é simplesmente ignorado. O heap chega a
 * ter até E entradas, e log E ≤ 2 log V, de modo que a ordem de complexidade se mantém.
 *
 * <p>O vetor interno pode crescer por duplicação (custo amortizado O(1) por inserção) —
 * um vetor primitivo não é "classe padrão de lista" do Java.
 *
 * @param <T> tipo dos elementos (no Dijkstra, {@code Vertice<T>})
 */
public interface IFilaPrioridade<T> {

    /**
     * Insere um elemento com a prioridade dada. O(log n).
     *
     * @param elemento   elemento a inserir
     * @param prioridade chave de ordenação (no Dijkstra: tempo acumulado em minutos)
     */
    void inserir(T elemento, float prioridade);

    /**
     * Remove e devolve o elemento de MENOR prioridade. O(log n).
     *
     * @return o elemento mínimo, ou {@code null} se vazia
     */
    T removerMinimo();

    /**
     * Consulta, sem remover, a menor prioridade presente. O(1). Útil no Dijkstra
     * preguiçoso para comparar com {@code dist[v]} e descartar entradas obsoletas.
     *
     * @return a menor prioridade
     * @throws IllegalStateException se a fila estiver vazia
     */
    float menorPrioridade();

    /** @return {@code true} se vazia. */
    boolean estaVazia();

    /** @return quantidade de entradas. */
    int tamanho();
}
