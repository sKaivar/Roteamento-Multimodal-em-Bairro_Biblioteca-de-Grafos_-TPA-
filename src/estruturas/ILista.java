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

    boolean adicionar(T elemento);

    void adicionarNoInicio(T elemento);

    T removerPrimeiro();

    int tamanho();

    boolean estaVazia();
}
