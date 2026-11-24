package grafo;

import estruturas.Fila;
import estruturas.HeapMinimo;
import estruturas.ILista;
import estruturas.ListaEncadeada;

import java.util.Arrays;
import java.util.Objects;

public class Grafo<T> implements IGrafo<T> {

    private final boolean dirigido; // determina se o grafo tem um sentido ou nao
    private final ILista<Vertice<T>> vertices; // Todos vertices do grafo
    private int quantArestas; // Quantidade total de arestas

    // Cria o grafo
    public Grafo(boolean dirigido) {

        this.dirigido = dirigido;
        this.vertices = new ListaEncadeada<>();
        this.quantArestas = 0;
    }

    // Informa se o grafo tem sentido
    @Override
    public boolean ehDirigido() {
        return dirigido;
    }

    //____________________________VERTICES

    // Adiciona uma vertice
    @Override
    public Vertice<T> adicionarVertice(T valor) {

        if (valor == null) {
            throw new IllegalArgumentException(
                    "O valor do vértice não pode ser nulo."
            );
        }

        // Evita duplicação de vértices
        Vertice<T> existente = obterVertice(valor);

        if (existente != null) {
            return existente;
        }

        // O índice corresponde à quantidade atual de vértices
        int indice = vertices.tamanho();

        // Cada vértice recebe sua própria lista de adjacências vazia
        ILista<IAresta<T>> adjacentes = new ListaEncadeada<>();

        Vertice<T> novoVertice = new Vertice<>(
                valor,
                indice,
                adjacentes
        );

        vertices.adicionar(novoVertice);

        return novoVertice;
    }

    //____________________________ARESTAS

    // Adiciona uma aresta
    @Override
    public void adicionarAresta(
            T origem,
            T destino,
            float peso
    ) {

        adicionarAresta(origem, destino, peso, "");
    }

    // Adiciona uma aresta com rotulo
    @Override
    public void adicionarAresta(
            T origem,
            T destino,
            float peso,
            String rotulo
    ) {

        if (peso < 0
                || Float.isNaN(peso)
                || Float.isInfinite(peso)) {

            throw new IllegalArgumentException(
                    "O peso deve ser finito e não negativo."
            );
        }

        // Localiza os dois vértices
        Vertice<T> verticeOrigem = obterVertice(origem);

        Vertice<T> verticeDestino = obterVertice(destino);

        if (verticeOrigem == null || verticeDestino == null) {

            throw new IllegalArgumentException(
                    "Os vértices da aresta devem existir no grafo."
            );
        }

        // Cria a aresta principal
        Aresta<T> aresta = new Aresta<>(
                verticeOrigem,
                verticeDestino,
                peso,
                rotulo
        );

        verticeOrigem.adicionarAdjacente(aresta);

        // Em grafo não dirigido, cria a aresta inversa
        if (!dirigido && verticeOrigem != verticeDestino) {

            Aresta<T> inversa = new Aresta<>(
                    verticeDestino,
                    verticeOrigem,
                    peso,
                    rotulo
            );

            verticeDestino.adicionarAdjacente(inversa);
        }

        // Conta a ligação lógica uma única vez
        quantArestas++;
    }


    //____________________________INFORMACAOES GRAFO

    //Obtem um vertice apartir de um valor especifico
    @Override
    public Vertice<T> obterVertice(T valor) {

        for (Vertice<T> vertice : vertices) {

            if (Objects.equals(vertice.getValor(), valor)) {
                return vertice;
            }
        }

        return null;
    }

    //Obtem um vertice
    @Override
    public ILista<Vertice<T>> getVertices() {

        ILista<Vertice<T>> copia = new ListaEncadeada<>();

        for (Vertice<T> vertice : vertices) {
            copia.adicionar(vertice);
        }

        return copia;
    }

    @Override
    public int quantidadeVertices() {
        return vertices.tamanho();
    }

    //Obtem arestas
    @Override
    public int quantidadeArestas() {
        return quantArestas;
    }

    // Algoritmo de busca (BFS - Busca em largura)
    @Override
    public String caminharEmLargura(T origem) {

        Vertice<T> inicial = exigirVertice(origem);

        boolean[] visitado = new boolean[quantidadeVertices()]; // Lista de booleanos para verificar vertices visitados

        Fila<Vertice<T>> fila = new Fila<>();

        StringBuilder resultado = new StringBuilder();

        // Insere a origem e marca como visitada
        fila.enfileirar(inicial);

        visitado[inicial.getIndice()] = true;

        while (!fila.estaVazia()) {

            Vertice<T> atual = fila.desenfileirar();

            // Adiciona o vértice ao resultado
            if (resultado.length() > 0) {
                resultado.append(System.lineSeparator());
            }

            resultado.append(atual.getValor());

            // Percorre as arestas de saída
            for (IAresta<T> aresta : atual.getAdjacentes()) {

                Vertice<T> vizinho = aresta.getDestino();

                int indice = vizinho.getIndice();

                // Se ainda não foi visitado, adiciona à fila
                if (!visitado[indice]) {

                    visitado[indice] = true;

                    fila.enfileirar(vizinho);
                }
            }
        }

        return resultado.toString();
    }

    //____________________________ENCONTRA CAMINHO
    @Override
    public boolean existeCaminho(T origem, T destino) {

        Vertice<T> inicial = exigirVertice(origem);

        Vertice<T> objetivo = exigirVertice(destino);

        // Um vértice sempre alcança a si próprio
        if (inicial == objetivo) {
            return true;
        }

        boolean[] visitado = new boolean[quantidadeVertices()];

        Fila<Vertice<T>> fila = new Fila<>();

        fila.enfileirar(inicial);

        visitado[inicial.getIndice()] = true;

        while (!fila.estaVazia()) {

            Vertice<T> atual = fila.desenfileirar();

            for (IAresta<T> aresta : atual.getAdjacentes()) {

                Vertice<T> vizinho = aresta.getDestino();

                int indice = vizinho.getIndice();

                if (!visitado[indice]) {

                    // Encontrou o destino
                    if (vizinho == objetivo) {
                        return true;
                    }

                    visitado[indice] = true;

                    fila.enfileirar(vizinho);
                }
            }
        }

        return false;
    }

    // ==========================================
    // DIJKSTRA - MENOR CAMINHO
    // ==========================================

    /**
     * Calcula o caminho de menor custo entre dois vértices
     * utilizando o algoritmo de Dijkstra.
     *
     * Utiliza:
     * - Heap mínimo próprio;
     * - Vetor de distâncias;
     * - Vetor de arestas predecessoras;
     * - Vetor de vértices finalizados;
     * - Inserção preguiçosa (lazy deletion).
     *
     * Antes da execução, verifica a alcançabilidade
     * utilizando busca em largura.
     *
     * Complexidade:
     * O(V + E) para a verificação de alcançabilidade,
     * seguido de O((V + E) log V) para Dijkstra,
     * considerando um grafo simples.
     *
     * @param origem valor do vértice inicial
     * @param destino valor do vértice final
     * @return caminho mínimo encontrado
     */
    @Override
    public ICaminho<T> menorCaminho(T origem, T destino) {

        Vertice<T> inicial = exigirVertice(origem);

        Vertice<T> objetivo = exigirVertice(destino);

        // Verifica se existe algum caminho antes do Dijkstra
        if (!existeCaminho(origem, destino)) {
            return caminhoInexistente();
        }

        int quantidade = quantidadeVertices();

        // Vetor de distâncias
        float[] distancias = new float[quantidade];

        Arrays.fill(distancias, Float.POSITIVE_INFINITY);

        // Vetor de arestas predecessoras
        @SuppressWarnings("unchecked")
        IAresta<T>[] predecessoras =
                (IAresta<T>[]) new IAresta<?>[quantidade];

        // Indica se o menor custo do vértice já foi finalizado
        boolean[] finalizado = new boolean[quantidade];

        // Fila de prioridade mínima
        HeapMinimo<Vertice<T>> fila = new HeapMinimo<>();

        // Inicializa a origem
        distancias[inicial.getIndice()] = 0f;

        fila.inserir(inicial, 0f);

        // Processamento do Dijkstra
        while (!fila.estaVazia()) {

            // Consulta a prioridade antes da remoção
            float prioridade = fila.menorPrioridade();

            Vertice<T> atual = fila.removerMinimo();

            int indiceAtual = atual.getIndice();

            /*
             * Lazy deletion:
             *
             * Descarta entradas antigas do heap,
             * cuja prioridade não corresponde mais
             * à menor distância conhecida.
             */
            if (Float.compare(
                    prioridade,
                    distancias[indiceAtual]
            ) != 0 || finalizado[indiceAtual]) {

                continue;
            }

            // A menor distância deste vértice foi confirmada
            finalizado[indiceAtual] = true;

            // Parada antecipada ao alcançar o destino
            if (atual == objetivo) {
                break;
            }

            // Percorre todas as arestas de saída
            for (IAresta<T> aresta : atual.getAdjacentes()) {

                Vertice<T> vizinho = aresta.getDestino();

                int indiceVizinho = vizinho.getIndice();

                if (finalizado[indiceVizinho]) {
                    continue;
                }

                // Calcula o novo custo acumulado
                float novaDistancia =
                        distancias[indiceAtual] + aresta.getPeso();

                // Verifica se encontrou um caminho melhor
                if (novaDistancia < distancias[indiceVizinho]) {

                    distancias[indiceVizinho] = novaDistancia;

                    // Guarda a aresta utilizada para chegar ao vizinho
                    predecessoras[indiceVizinho] = aresta;

                    // Insere novamente o vértice com a prioridade atualizada
                    fila.inserir(vizinho, novaDistancia);
                }
            }
        }

        // Reconstrói o caminho utilizando as predecessoras
        return reconstruirCaminho(
                inicial,
                objetivo,
                distancias[objetivo.getIndice()],
                predecessoras
        );
    }

    //____________________________RECONSTROI O CAMINHO FEITO
    private ICaminho<T> reconstruirCaminho(
            Vertice<T> origem,
            Vertice<T> destino,
            float custoTotal,
            IAresta<T>[] predecessoras
    ) {

        ILista<Vertice<T>> caminhoVertices =
                new ListaEncadeada<>();

        ILista<IAresta<T>> caminhoArestas =
                new ListaEncadeada<>();

        Vertice<T> atual = destino;

        // O destino é o primeiro elemento inserido
        caminhoVertices.adicionarNoInicio(atual);

        // Percorre as predecessoras até chegar à origem
        while (atual != origem) {

            IAresta<T> aresta =
                    predecessoras[atual.getIndice()];

            if (aresta == null) {
                return caminhoInexistente();
            }

            // Adiciona a aresta no início
            caminhoArestas.adicionarNoInicio(aresta);

            atual = aresta.getOrigem();

            // Adiciona o vértice no início
            caminhoVertices.adicionarNoInicio(atual);
        }

        return new Caminho<>(
                true,
                custoTotal,
                caminhoVertices,
                caminhoArestas
        );
    }

    //____________________________METODOS AUXILIARES
    private Vertice<T> exigirVertice(T valor) {

        Vertice<T> vertice = obterVertice(valor);

        if (vertice == null) {
            throw new IllegalArgumentException(
                    "O vértice informado não existe no grafo."
            );
        }

        return vertice;
    }

    // registra um caminho inexistente
    private ICaminho<T> caminhoInexistente() {

        return new Caminho<>(
                false,
                Float.POSITIVE_INFINITY,
                new ListaEncadeada<>(),
                new ListaEncadeada<>()
        );
    }
}