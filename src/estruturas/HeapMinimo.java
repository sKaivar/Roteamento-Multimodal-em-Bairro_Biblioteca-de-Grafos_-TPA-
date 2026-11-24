package estruturas;

import java.util.Arrays;
import java.util.Objects;

/**
 * Fila de prioridade mínima implementada utilizando heap binário.
 */
public class HeapMinimo<T> implements IFilaPrioridade<T> {

    private static final int CAPACIDADE_INICIAL = 16;

    private Entrada<T>[] heap;
    private int quant;

    /**
     * Representa um elemento associado à sua prioridade.
     */
    private static class Entrada<E> {

        private final E elemento;
        private final float prioridade;

        Entrada(E elemento, float prioridade) {
            this.elemento = elemento;
            this.prioridade = prioridade;
        }
    }

    //Cria um heap vazio
    @SuppressWarnings("unchecked")
    public HeapMinimo() {

        heap = (Entrada<T>[]) new Entrada[CAPACIDADE_INICIAL];

        quant = 0;
    }

    /**
     * Insere um elemento com a prioridade informada.
     *
     * O(log n), desconsiderando o custo ocasional de expansão
     * do vetor, que é amortizado O(1).
     *
     * @param elemento elemento a inserir
     * @param prioridade prioridade do elemento
     */
    @Override
    public void inserir(T elemento, float prioridade) {

        Objects.requireNonNull(elemento);

        if (Float.isNaN(prioridade)) {
            throw new IllegalArgumentException(
                    "A prioridade não pode ser NaN."
            );
        }

        garantirCapacidade();

        Entrada<T> novaEntrada =
                new Entrada<>(elemento, prioridade);

        heap[quant] = novaEntrada;

        int indice = quant;

        quant++;

        subir(indice);
    }

    //Remove e retorna elemento de menor prioridade
    @Override
    public T removerMinimo() {

        if (estaVazia()) {
            return null;
        }

        T minimo = heap[0].elemento;

        int ultimoIndice = quant - 1;

        // Caso exista apenas um elemento
        if (ultimoIndice == 0) {

            heap[0] = null;

            quant = 0;

            return minimo;
        }

        // O último elemento assume a posição da raiz
        heap[0] = heap[ultimoIndice];

        heap[ultimoIndice] = null;

        quant--;

        // Restaura a propriedade do heap
        descer(0);

        return minimo;
    }

    //Consulta a qual elemento de menor prioridade
    @Override
    public float menorPrioridade() {

        if (estaVazia()) {
            throw new IllegalStateException(
                    "O heap está vazio."
            );
        }

        return heap[0].prioridade;
    }

    // Move elemento para cima se a sua prioridade for menor que seu pai
    private void subir(int indice) {

        while (indice > 0) {

            int pai = (indice - 1) / 2;

            if (Float.compare(
                    heap[indice].prioridade,
                    heap[pai].prioridade
            ) >= 0) {
                break;
            }

            trocar(indice, pai);

            indice = pai;
        }
    }

    // Move elemento para baixo se filho tiver prioridade menor
    private void descer(int indice) {

        while (true) {

            int esquerda = 2 * indice + 1;
            int direita = 2 * indice + 2;

            int menor = indice;

            // Verifica o filho esquerdo
            if (esquerda < quant &&
                    Float.compare(
                            heap[esquerda].prioridade,
                            heap[menor].prioridade
                    ) < 0) {

                menor = esquerda;
            }

            // Verifica o filho direito
            if (direita < quant &&
                    Float.compare(
                            heap[direita].prioridade,
                            heap[menor].prioridade
                    ) < 0) {

                menor = direita;
            }

            // Se o próprio elemento já for o menor, termina
            if (menor == indice) {
                break;
            }

            trocar(indice, menor);

            indice = menor;
        }
    }

    // Troca dois elementos de posicao
    private void trocar(int i, int j) {

        Entrada<T> aux = heap[i];

        heap[i] = heap[j];

        heap[j] = aux;
    }

    // Aumenta vetor se tiver ocupado seu limite
    private void garantirCapacidade() {

        if (quant == heap.length) {

            heap = Arrays.copyOf(
                    heap,
                    heap.length * 2
            );
        }
    }

    // Verifica se o heap esta vazio
    @Override
    public boolean estaVazia() {
        return quant == 0;
    }

    // Quantidade total elementos
    @Override
    public int tamanho() {
        return quant;
    }
}