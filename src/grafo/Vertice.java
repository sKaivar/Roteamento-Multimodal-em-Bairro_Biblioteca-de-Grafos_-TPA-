package grafo;

import estruturas.ILista;

/**
 * Vértice (nó) de um grafo representado por LISTA DE ADJACÊNCIAS.
 *
 * <p>Cada vértice guarda:
 * <ul>
 *   <li>um objeto de tipo genérico {@code T} (no aplicativo, um {@code ILocal}: interseção,
 *       parada de ônibus ou local de interesse);</li>
 *   <li>um índice inteiro, atribuído pelo grafo na inserção (0, 1, 2, …), que permite aos
 *       algoritmos (largura, Dijkstra) usar vetores {@code dist[]}, {@code pred[]} e
 *       {@code visitado[]} com acesso O(1), em vez de procurar o vértice numa lista;</li>
 *   <li>a lista das arestas que PARTEM dele (adjacências de saída).</li>
 * </ul>
 *
 * <p>Por que o nome {@code Vertice} e não {@code No}: a biblioteca de listas do grupo já
 * possui uma classe {@code No<T>} (nó da lista encadeada); reutilizar o nome causaria
 * ambiguidade de importação e de leitura no relatório.
 *
 * <p>A lista de adjacências é recebida pronta no construtor (injeção de dependência): o
 * {@code Grafo} cria uma instância VAZIA da lista própria do grupo (exigência do trabalho:
 * nada de {@code java.util.LinkedList}/{@code ArrayList}) e a entrega ao vértice. Assim,
 * esta classe depende apenas da interface {@link ILista}, não de uma implementação.
 *
 * <p>Os estados dos algoritmos (distância, predecessor, visitado) NÃO ficam no vértice:
 * ficam em vetores locais do algoritmo, indexados por {@link #getIndice()}. Isso permite
 * executar Dijkstra várias vezes (uma por modalidade) sem "limpar" o grafo entre execuções.
 *
 * @param <T> tipo do objeto armazenado no vértice; deve implementar {@code equals} e
 *            {@code hashCode} de forma coerente (no domínio, pela identidade do local).
 */
public class Vertice<T> {

    /** Objeto armazenado (imutável após a criação: trocar o valor quebraria a busca por valor). */
    private final T valor;

    /** Posição do vértice no grafo (0 .. V-1), usada como índice nos vetores dos algoritmos. */
    private final int indice;

    /** Arestas que saem deste vértice. Em grafo não dirigido, cada ligação aparece nos dois vértices. */
    private final ILista<IAresta<T>> adjacentes;

    /**
     * Cria um vértice.
     *
     * @param valor           objeto a armazenar (não nulo)
     * @param indice          índice atribuído pelo grafo (não negativo)
     * @param listaAdjacentes instância VAZIA da lista própria do grupo, que passará a
     *                        guardar as arestas de saída
     * @throws IllegalArgumentException se algum argumento for inválido
     */
    public Vertice(T valor, int indice, ILista<IAresta<T>> listaAdjacentes) {
        if (valor == null) {
            throw new IllegalArgumentException("O valor do vértice não pode ser nulo.");
        }
        if (indice < 0) {
            throw new IllegalArgumentException("O índice do vértice deve ser não negativo.");
        }
        if (listaAdjacentes == null || !listaAdjacentes.estaVazia()) {
            throw new IllegalArgumentException("A lista de adjacências deve ser uma instância vazia.");
        }
        this.valor = valor;
        this.indice = indice;
        this.adjacentes = listaAdjacentes;
    }

    /** @return o objeto armazenado no vértice. */
    public T getValor() {
        return valor;
    }

    /** @return o índice do vértice no grafo (0 .. V-1). */
    public int getIndice() {
        return indice;
    }

    /**
     * Registra uma aresta de saída. Custo: O(1) se a lista própria inserir no início
     * (ou no fim com ponteiro para o último nó).
     *
     * <p>Chamado apenas pelo {@code Grafo}, que é quem valida a aresta (vértices
     * existentes, peso não negativo) — daí a visibilidade de pacote.
     *
     * @param aresta aresta cuja origem é este vértice
     * @throws IllegalArgumentException se a aresta for nula ou não partir deste vértice
     */
    void adicionarAdjacente(IAresta<T> aresta) {
        if (aresta == null || aresta.getOrigem() != this) {
            throw new IllegalArgumentException("A aresta deve existir e partir deste vértice.");
        }
        adjacentes.adicionar(aresta);
    }

    /**
     * Arestas de saída, para percorrer com for-each:
     * <pre>  for (IAresta&lt;T&gt; a : v.getAdjacentes()) { ... a.getDestino() ... }</pre>
     * A varredura custa O(grau(v)) — fundamento da complexidade O((V+E) log V) do Dijkstra.
     *
     * @return iterável somente-leitura sobre as arestas de saída
     */
    public Iterable<IAresta<T>> getAdjacentes() {
        return adjacentes;
    }

    /** @return grau de saída do vértice (quantidade de arestas que partem dele). */
    public int grauSaida() {
        return adjacentes.tamanho();
    }

    /**
     * Procura a aresta deste vértice até {@code destino}. Custo O(grau(v)) — a
     * "desvantagem assumida" da lista de adjacências, descrita na especificação.
     *
     * @param destino vértice de chegada
     * @return a primeira aresta encontrada, ou {@code null} se não houver
     */
    public IAresta<T> arestaPara(Vertice<T> destino) {
        for (IAresta<T> a : adjacentes) {
            if (a.getDestino() == destino) {
                return a;
            }
        }
        return null;
    }

    /** Dois vértices são iguais quando guardam valores iguais (a busca do grafo é por valor). */
    @Override
    public boolean equals(Object outro) {
        if (this == outro) return true;
        if (!(outro instanceof Vertice)) return false;
        return valor.equals(((Vertice<?>) outro).valor);
    }

    @Override
    public int hashCode() {
        return valor.hashCode();
    }

    /** Representação usada no caminhamento em largura e na depuração. */
    @Override
    public String toString() {
        return valor.toString();
    }
}
