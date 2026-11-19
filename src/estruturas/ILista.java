package estruturas;

/**
 * Contrato mínimo que a LISTA PRÓPRIA do grupo deve cumprir para servir ao grafo
 * (adjacências, conjunto de vértices, caminhos reconstruídos e coleções do domínio).
 *
 * <p>Requisito do trabalho: "sempre que precisar de uma lista, utilize suas próprias
 * bibliotecas". A lista encadeada do Trabalho 1 deve, portanto, passar a implementar
 * esta interface — tipicamente acrescentando um {@code iterator()} que caminha pelos nós
 * ({@code prim → prox → …}). Estender {@link Iterable} não fere o requisito: {@code Iterable}
 * é apenas um contrato da linguagem (permite o for-each), não uma coleção pronta.
 *
 * @param <T> tipo dos elementos
 */
public interface ILista<T> extends Iterable<T> {

    /**
     * Acrescenta um elemento ao FIM da lista. Deve custar O(1): mantenha um ponteiro
     * para o último nó, para que montar o grafo inteiro custe O(V + E).
     *
     * @param elemento elemento a inserir (não nulo)
     * @return {@code true} se inserido
     */
    boolean adicionar(T elemento);

    /**
     * Acrescenta um elemento ao INÍCIO da lista, em O(1). Usado na reconstrução do
     * caminho pelo vetor de predecessores (que é percorrido do destino para a origem).
     *
     * @param elemento elemento a inserir (não nulo)
     */
    void adicionarNoInicio(T elemento);

    /**
     * Remove e devolve o primeiro elemento, em O(1). Permite reutilizar a lista como
     * fila (FIFO) na busca em largura.
     *
     * @return o primeiro elemento, ou {@code null} se a lista estiver vazia
     */
    T removerPrimeiro();

    /** @return quantidade de elementos, em O(1) (mantenha um contador). */
    int tamanho();

    /** @return {@code true} se não houver elementos. */
    boolean estaVazia();
}
