package estruturas;

/**
 * Fila FIFO usada na busca em largura. Implementação sugerida: um adaptador fino sobre a
 * lista própria ({@code enfileirar} = {@link ILista#adicionar}, {@code desenfileirar} =
 * {@link ILista#removerPrimeiro}), ambos O(1).
 *
 * @param <T> tipo dos elementos
 */
public interface IFila<T> {

    /** Insere no fim da fila, em O(1). */
    void enfileirar(T elemento);

    /** Remove e devolve o elemento do início, em O(1); {@code null} se vazia. */
    T desenfileirar();

    /** @return {@code true} se a fila estiver vazia. */
    boolean estaVazia();

    /** @return quantidade de elementos. */
    int tamanho();
}
