package grafo;

import estruturas.ILista;
import estruturas.ListaEncadeada;

/**
 * Representa o resultado de uma consulta de caminho mínimo.
 *
 * Armazena os vértices e as arestas na ordem de percurso,
 * da origem até o destino.
 *
 * @param <T> tipo do objeto armazenado nos vértices
 */
public final class Caminho<T> implements ICaminho<T> {

    private final boolean existe;
    private final float custoTotal;

    private final ILista<Vertice<T>> vertices;
    private final ILista<IAresta<T>> arestas;

    Caminho(
            boolean existe,
            float custoTotal,
            ILista<Vertice<T>> vertices,
            ILista<IAresta<T>> arestas
    ) {

        if (vertices == null || arestas == null) {
            throw new IllegalArgumentException(
                    "As listas do caminho não podem ser nulas."
            );
        }

        this.existe = existe;

        if (existe) {

            if (custoTotal < 0
                    || Float.isNaN(custoTotal)
                    || Float.isInfinite(custoTotal)) {

                throw new IllegalArgumentException(
                        "O custo total do caminho é inválido."
                );
            }

            if (vertices.estaVazia()
                    || arestas.tamanho() != vertices.tamanho() - 1) {

                throw new IllegalArgumentException(
                        "A quantidade de vértices e arestas é inconsistente."
                );
            }

            this.custoTotal = custoTotal;

            this.vertices = copiarLista(vertices);
            this.arestas = copiarLista(arestas);

        } else {

            this.custoTotal = Float.POSITIVE_INFINITY;

            this.vertices = new ListaEncadeada<>();
            this.arestas = new ListaEncadeada<>();
        }
    }

    // Cria copia da lista
    private static <E> ILista<E> copiarLista(ILista<E> original) {

        ILista<E> copia = new ListaEncadeada<>();

        for (E elemento : original) {
            copia.adicionar(elemento);
        }

        return copia;
    }

    // Informa se existe caminho entre origem e destino
    @Override
    public boolean existe() {
        return existe;
    }

    // Retorna custo total de um caminho
    @Override
    public float getCustoTotal() {
        return custoTotal;
    }

    // Retorna vertices percorridos, utiliza a copia para nao baguncar a estutura interna
    @Override
    public ILista<Vertice<T>> getVertices() {
        return copiarLista(vertices);
    }

    // Retorna arestas percorridos, utiliza a copia para nao baguncar a estutura interna
    @Override
    public ILista<IAresta<T>> getArestas() {
        return copiarLista(arestas);
    }
}