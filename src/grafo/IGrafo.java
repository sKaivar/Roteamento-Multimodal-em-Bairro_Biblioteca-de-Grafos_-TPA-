package grafo;

import estruturas.ILista;

/**
 * Contrato da biblioteca genérica de grafos (Etapa 2 do enunciado), em lista de adjacências.
 *
 * <p>Implementação esperada: classe {@code Grafo<T> implements IGrafo<T>}, com construtor
 * {@code Grafo(boolean dirigido)}, mantendo:
 * <ul>
 *   <li>uma lista própria de {@link Vertice} (o i-ésimo inserido recebe índice i);</li>
 *   <li>um contador de arestas.</li>
 * </ul>
 *
 * <p><b>Grafos do aplicativo.</b> Pedestre: não dirigido. Ciclista: DIRIGIDO — o carregador
 * insere os dois sentidos quando a rua é de mão dupla ou quando é ciclovia (ciclovias são
 * bidirecionais), e um só sentido nas ruas de mão única. Ônibus: dirigido. Composto
 * (modalidade II): dirigido.
 *
 * <p><b>Busca de vértice por valor.</b> Com lista simples, {@link #obterVertice} custa O(V),
 * e montar o grafo custa O(V·E). Melhoria opcional (vale ponto em "eficiência" na
 * arguição): indexar os vértices também na árvore AVL do Trabalho 2, com um
 * {@code Comparator<T>} recebido no construtor, reduzindo a busca a O(log V).
 *
 * @param <T> tipo armazenado nos vértices
 */
public interface IGrafo<T> {

    /** @return {@code true} se as arestas tiverem sentido. */
    boolean ehDirigido();

    /**
     * (i) do enunciado — se não existir vértice com valor igual a {@code valor}, cria um,
     * com índice = quantidade atual de vértices.
     *
     * @param valor objeto a armazenar
     * @return o vértice (novo ou já existente) que guarda o valor
     */
    Vertice<T> adicionarVertice(T valor);

    /**
     * (ii) do enunciado — localiza os vértices de {@code origem} e {@code destino} e cria a
     * aresta com o peso dado (rótulo vazio). Em grafo não dirigido, cria também a inversa.
     *
     * @throws IllegalArgumentException se algum vértice não existir ou se {@code peso < 0}
     *         (Dijkstra exige pesos não negativos)
     */
    void adicionarAresta(T origem, T destino, float peso);

    /**
     * Igual a {@link #adicionarAresta(Object, Object, float)}, registrando um rótulo para o
     * guia textual (nome da rua, código da linha).
     */
    void adicionarAresta(T origem, T destino, float peso, String rotulo);

    /** @return o vértice que guarda {@code valor}, ou {@code null}. */
    Vertice<T> obterVertice(T valor);

    /** @return todos os vértices, na ordem de inserção (índice 0 .. V-1). */
    ILista<Vertice<T>> getVertices();

    /** @return V. */
    int quantidadeVertices();

    /** @return E (em grafo não dirigido, conte cada ligação uma única vez). */
    int quantidadeArestas();

    /**
     * (iii) do enunciado — caminhamento em largura a partir de {@code origem}, usando
     * {@link estruturas.IFila} e vetor {@code visitado[]} indexado por {@link Vertice#getIndice()}.
     * O(V + E).
     *
     * @return os vértices na ordem de visita, um por linha (para imprimir e testar o grafo)
     */
    String caminharEmLargura(T origem);

    /**
     * Verifica, por busca em largura, se {@code destino} é alcançável a partir de
     * {@code origem}. Usado antes do Dijkstra: rota inexistente ⇒ modalidade inviável. O(V + E).
     */
    boolean existeCaminho(T origem, T destino);

    /**
     * Dijkstra com fila de prioridade em heap próprio ({@link estruturas.IFilaPrioridade}),
     * vetores {@code dist[]} e {@code pred[]} (aresta predecessora) indexados por vértice,
     * parada antecipada ao retirar o destino do heap. O((V + E) log V).
     *
     * @return o caminho mínimo; {@link ICaminho#existe()} falso se inalcançável
     * @throws IllegalArgumentException se origem ou destino não existirem
     */
    ICaminho<T> menorCaminho(T origem, T destino);
}
