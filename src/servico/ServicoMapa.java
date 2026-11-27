package servico;

import dominio.ILinhaOnibus;
import dominio.ILocal;
import dominio.IMapa;
import dominio.LinhaOnibus;
import dominio.SentidoBicicleta;
import dominio.TipoLocal;
import dominio.Trecho;
import dominio.TrechoOnibus;
import estruturas.ILista;
import grafo.IGrafo;
import grafo.Vertice;

/**
 * Serviço responsável por acréscimos dinâmicos ao bairro após a leitura do arquivo,
 * atualizando o mapa de domínio e os grafos de pedestres, ciclistas e ônibus, além
 * de controlar a reconstrução sob demanda do grafo composto multimodal.
 */
public class ServicoMapa implements IServicoMapa {

    private IMapa mapa;
    private IConstrutorGrafos construtorGrafos;
    private IModeloCusto modeloCusto;

    private IGrafo<ILocal> grafoPedestre;
    private IGrafo<ILocal> grafoCiclista;
    private IGrafo<ILocal> grafoOnibus;
    private IGrafo<ILocal> grafoComposto;

    private boolean grafoCompostoDesatualizado = false;
    private int contadorLocaisNovos = 1000;

    /**
     * Cria o serviço de mapa sem dependências pré-configuradas.
     */
    public ServicoMapa() {
    }

    /**
     * Cria o serviço de mapa com todas as referências de domínio, construção e grafos.
     *
     * @param mapa repositório em memória dos dados do bairro
     * @param construtorGrafos construtor usado para gerar/reconstruir o grafo composto
     * @param modeloCusto conversor de distâncias em metros para tempos em minutos
     * @param grafoPedestre grafo não dirigido de deslocamento a pé
     * @param grafoCiclista grafo dirigido de deslocamento por bicicleta
     * @param grafoOnibus grafo dirigido da rede de ônibus
     * @param grafoComposto grafo composto em camadas (a pé + ônibus)
     */
    public ServicoMapa(
            IMapa mapa,
            IConstrutorGrafos construtorGrafos,
            IModeloCusto modeloCusto,
            IGrafo<ILocal> grafoPedestre,
            IGrafo<ILocal> grafoCiclista,
            IGrafo<ILocal> grafoOnibus,
            IGrafo<ILocal> grafoComposto
    ) {
        this.mapa = mapa;
        this.construtorGrafos = construtorGrafos;
        this.modeloCusto = modeloCusto;
        this.grafoPedestre = grafoPedestre;
        this.grafoCiclista = grafoCiclista;
        this.grafoOnibus = grafoOnibus;
        this.grafoComposto = grafoComposto;
    }

    /**
     * @return o mapa de domínio associado ao serviço
     */
    public IMapa getMapa() {
        return mapa;
    }

    /**
     * Define o mapa de domínio utilizado pelo serviço.
     *
     * @param mapa instância do mapa carregado
     */
    public void setMapa(IMapa mapa) {
        this.mapa = mapa;
    }

    /**
     * @return o construtor de grafos configurado
     */
    public IConstrutorGrafos getConstrutorGrafos() {
        return construtorGrafos;
    }

    /**
     * Define o construtor de grafos usado para reconstruir o grafo composto.
     *
     * @param construtorGrafos instância de {@link IConstrutorGrafos}
     */
    public void setConstrutorGrafos(IConstrutorGrafos construtorGrafos) {
        this.construtorGrafos = construtorGrafos;
    }

    /**
     * @return o modelo de custo configurado
     */
    public IModeloCusto getModeloCusto() {
        return modeloCusto;
    }

    /**
     * Define o modelo de custo responsável por converter metros em minutos.
     *
     * @param modeloCusto instância de {@link IModeloCusto}
     */
    public void setModeloCusto(IModeloCusto modeloCusto) {
        this.modeloCusto = modeloCusto;
    }

    /**
     * @return o grafo de pedestres
     */
    public IGrafo<ILocal> getGrafoPedestre() {
        return grafoPedestre;
    }

    /**
     * Define o grafo de pedestres.
     *
     * @param grafoPedestre grafo não dirigido de caminhada
     */
    public void setGrafoPedestre(IGrafo<ILocal> grafoPedestre) {
        this.grafoPedestre = grafoPedestre;
    }

    /**
     * @return o grafo de ciclistas
     */
    public IGrafo<ILocal> getGrafoCiclista() {
        return grafoCiclista;
    }

    /**
     * Define o grafo de ciclistas.
     *
     * @param grafoCiclista grafo dirigido de bicicleta
     */
    public void setGrafoCiclista(IGrafo<ILocal> grafoCiclista) {
        this.grafoCiclista = grafoCiclista;
    }

    /**
     * @return o grafo da rede de ônibus
     */
    public IGrafo<ILocal> getGrafoOnibus() {
        return grafoOnibus;
    }

    /**
     * Define o grafo da rede de ônibus.
     *
     * @param grafoOnibus grafo dirigido das linhas de ônibus
     */
    public void setGrafoOnibus(IGrafo<ILocal> grafoOnibus) {
        this.grafoOnibus = grafoOnibus;
    }

    /**
     * @return o grafo composto multimodal atual
     */
    public IGrafo<ILocal> getGrafoComposto() {
        return grafoComposto;
    }

    /**
     * Define o grafo composto multimodal.
     *
     * @param grafoComposto grafo composto em camadas
     */
    public void setGrafoComposto(IGrafo<ILocal> grafoComposto) {
        this.grafoComposto = grafoComposto;
    }

    /**
     * @return {@code true} se houve alterações que exigem reconstrução do grafo composto
     */
    public boolean isGrafoCompostoDesatualizado() {
        return grafoCompostoDesatualizado;
    }

    /**
     * Cria um novo local de interesse, registra-o no mapa e nos grafos básicos e,
     * opcionalmente, conecta-o a pé à interseção viária mais próxima em linha reta.
     *
     * @param nome nome legível do novo local
     * @param latitude coordenada de latitude
     * @param longitude coordenada de longitude
     * @param ligarAoMaisProximo se {@code true}, cria ligação a pé até a interseção mais próxima
     * @return o local criado
     * @throws IllegalArgumentException se o nome for nulo ou vazio
     * @throws IllegalStateException se {@code ligarAoMaisProximo} for solicitado sem grafo pedestre ou modelo de custo
     */
    @Override
    public ILocal adicionarLocal(
            String nome,
            double latitude,
            double longitude,
            boolean ligarAoMaisProximo
    ) {

        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do local nao pode ser vazio.");
        }

        if (ligarAoMaisProximo && (grafoPedestre == null || modeloCusto == null)) {
            throw new IllegalStateException(
                    "Grafo de pedestres e modelo de custo precisam estar configurados para conectar o local."
            );
        }

        // Gera identificador único sem colidir com locais já existentes
        String novoId;
        do {
            novoId = "L" + (++contadorLocaisNovos);
        } while (buscarLocalPorId(novoId) != null);

        ILocal novoLocal = new LocalSimples(novoId, nome.trim(), latitude, longitude, TipoLocal.INTERESSE);

        // Adiciona aos grafos básicos
        if (grafoPedestre != null) {
            grafoPedestre.adicionarVertice(novoLocal);
        }
        if (grafoCiclista != null) {
            grafoCiclista.adicionarVertice(novoLocal);
        }

        // Adiciona no mapa antes de criar trechos dependentes
        if (mapa != null) {
            mapa.adicionarLocal(novoLocal);
        }

        // Se pediu, acha a interseção mais próxima e liga a pé
        if (ligarAoMaisProximo) {
            ILocal maisProximo = encontrarIntersecaoMaisProxima(novoLocal);

            if (maisProximo != null) {
                double distancia = novoLocal.distanciaMetros(maisProximo);
                float tempoMinutos = modeloCusto.tempoCaminhada(distancia);

                // Garante que a interseção exista no grafo pedestre e cria a aresta de acesso
                grafoPedestre.adicionarVertice(maisProximo);
                grafoPedestre.adicionarAresta(novoLocal, maisProximo, tempoMinutos, "Acesso a pe");

                if (mapa != null) {
                    mapa.adicionarTrecho(new Trecho(
                            novoLocal,
                            maisProximo,
                            distancia,
                            "Acesso a pe",
                            true,
                            SentidoBicicleta.NENHUM,
                            false
                    ));
                }
            }
        }

        // Marca que o composto precisa ser refeito na próxima consulta
        this.grafoCompostoDesatualizado = true;

        return novoLocal;
    }

    /**
     * Acrescenta um trecho viário entre dois locais já cadastrados no mapa/grafos.
     *
     * @param idOrigem identificador do local de origem
     * @param idDestino identificador do local de destino
     * @param metros distância do trecho em metros (não negativa)
     * @param nomeVia nome da rua ou avenida para o guia textual
     * @param pedestre indica se o trecho permite caminhada
     * @param bici sentido permitido para bicicletas
     * @param ciclovia indica se há ciclovia no trecho
     * @throws IllegalArgumentException se os locais não existirem ou os dados forem inválidos
     * @throws IllegalStateException se o grafo necessário ou o modelo de custo não estiverem configurados
     */
    @Override
    public void adicionarTrecho(
            String idOrigem,
            String idDestino,
            double metros,
            String nomeVia,
            boolean pedestre,
            SentidoBicicleta bici,
            boolean ciclovia
    ) {

        if (idOrigem == null || idDestino == null) {
            throw new IllegalArgumentException("Origem e destino sao obrigatorios.");
        }
        if (metros < 0 || Double.isNaN(metros) || Double.isInfinite(metros)) {
            throw new IllegalArgumentException("Distancia invalida.");
        }

        boolean usaBici = (bici != null && bici != SentidoBicicleta.NENHUM);

        if (pedestre && (grafoPedestre == null || modeloCusto == null)) {
            throw new IllegalStateException("Grafo de pedestres e modelo de custo devem estar configurados.");
        }
        if (usaBici && (grafoCiclista == null || modeloCusto == null)) {
            throw new IllegalStateException("Grafo de ciclistas e modelo de custo devem estar configurados.");
        }

        ILocal origem = buscarLocalPorId(idOrigem.trim());
        ILocal destino = buscarLocalPorId(idDestino.trim());

        if (origem == null || destino == null) {
            throw new IllegalArgumentException("Local nao encontrado no mapa.");
        }

        String rotulo = (nomeVia != null) ? nomeVia.trim() : "";

        // Adiciona no grafo de pedestres se for permitido a pé
        if (pedestre) {
            float tempoPe = modeloCusto.tempoCaminhada(metros);
            grafoPedestre.adicionarVertice(origem);
            grafoPedestre.adicionarVertice(destino);
            grafoPedestre.adicionarAresta(origem, destino, tempoPe, rotulo);
        }

        // Adiciona no grafo de bicicleta se permitido
        if (usaBici) {
            float tempoBike = modeloCusto.tempoBicicleta(metros, ciclovia);
            grafoCiclista.adicionarVertice(origem);
            grafoCiclista.adicionarVertice(destino);
            grafoCiclista.adicionarAresta(origem, destino, tempoBike, rotulo);

            // Se for mão dupla ou ciclovia, adiciona o sentido inverso
            if (bici == SentidoBicicleta.AMBOS || ciclovia) {
                grafoCiclista.adicionarAresta(destino, origem, tempoBike, rotulo);
            }
        }

        if (mapa != null) {
            SentidoBicicleta sentido = (bici != null) ? bici : SentidoBicicleta.NENHUM;
            mapa.adicionarTrecho(new Trecho(origem, destino, metros, rotulo, pedestre, sentido, ciclovia));
        }

        this.grafoCompostoDesatualizado = true;
    }

    /**
     * Acrescenta um trecho dirigido de linha de ônibus entre duas paradas, atualizando
     * o grafo de ônibus e marcando o grafo composto para reconstrução.
     *
     * @param codigoLinha código da linha de ônibus (ex.: "0160-1")
     * @param idParadaOrigem identificador da parada física de origem
     * @param idParadaDestino identificador da parada física de destino
     * @param metros distância entre as paradas em metros
     * @throws IllegalArgumentException se parâmetros obrigatórios estiverem ausentes ou a distância for inválida
     * @throws IllegalStateException se o grafo de ônibus ou o modelo de custo não estiverem configurados
     */
    @Override
    public void adicionarTrechoOnibus(
            String codigoLinha,
            String idParadaOrigem,
            String idParadaDestino,
            double metros
    ) {

        if (codigoLinha == null || codigoLinha.trim().isEmpty()
                || idParadaOrigem == null || idParadaOrigem.trim().isEmpty()
                || idParadaDestino == null || idParadaDestino.trim().isEmpty()) {
            throw new IllegalArgumentException("Linha e paradas devem ser informadas.");
        }
        if (metros < 0 || Double.isNaN(metros) || Double.isInfinite(metros)) {
            throw new IllegalArgumentException("Distancia invalida.");
        }
        if (grafoOnibus == null || modeloCusto == null) {
            throw new IllegalStateException("Grafo de onibus e modelo de custo devem estar configurados.");
        }

        String linhaLimpa = codigoLinha.trim();
        String origemLimpa = idParadaOrigem.trim();
        String destinoLimpo = idParadaDestino.trim();

        String idVerticeOrigem = origemLimpa + "@" + linhaLimpa;
        String idVerticeDestino = destinoLimpo + "@" + linhaLimpa;

        ILocal paradaOrigem = buscarLocalPorId(origemLimpa);
        ILocal paradaDestino = buscarLocalPorId(destinoLimpo);

        // Reaproveita vértice existente no grafo de ônibus para evitar duplicidade
        ILocal localVerticeOrigem = buscarLocalNoGrafo(grafoOnibus, idVerticeOrigem);
        if (localVerticeOrigem == null) {
            String nomeOrigem = (paradaOrigem != null)
                    ? paradaOrigem.getNome() + " (" + linhaLimpa + ")"
                    : "Ponto " + origemLimpa + " (" + linhaLimpa + ")";
            double latOrigem = (paradaOrigem != null) ? paradaOrigem.getLatitude() : 0.0;
            double lonOrigem = (paradaOrigem != null) ? paradaOrigem.getLongitude() : 0.0;

            localVerticeOrigem = new LocalSimples(
                    idVerticeOrigem,
                    nomeOrigem,
                    latOrigem,
                    lonOrigem,
                    TipoLocal.PARADA_LINHA
            );
            grafoOnibus.adicionarVertice(localVerticeOrigem);
        }

        ILocal localVerticeDestino = buscarLocalNoGrafo(grafoOnibus, idVerticeDestino);
        if (localVerticeDestino == null) {
            String nomeDestino = (paradaDestino != null)
                    ? paradaDestino.getNome() + " (" + linhaLimpa + ")"
                    : "Ponto " + destinoLimpo + " (" + linhaLimpa + ")";
            double latDestino = (paradaDestino != null) ? paradaDestino.getLatitude() : 0.0;
            double lonDestino = (paradaDestino != null) ? paradaDestino.getLongitude() : 0.0;

            localVerticeDestino = new LocalSimples(
                    idVerticeDestino,
                    nomeDestino,
                    latDestino,
                    lonDestino,
                    TipoLocal.PARADA_LINHA
            );
            grafoOnibus.adicionarVertice(localVerticeDestino);
        }

        float tempoOnibus = modeloCusto.tempoOnibus(metros);
        grafoOnibus.adicionarAresta(localVerticeOrigem, localVerticeDestino, tempoOnibus, linhaLimpa);

        // Atualiza também a coleção de linhas no mapa caso as paradas existam no domínio
        if (mapa != null && paradaOrigem != null && paradaDestino != null) {
            ILinhaOnibus linha = buscarOuCriarLinha(linhaLimpa);
            linha.getTrechos().adicionar(new TrechoOnibus(linha, paradaOrigem, paradaDestino, metros));
        }

        this.grafoCompostoDesatualizado = true;
    }

    /**
     * Reconstrói o grafo composto caso tenha ocorrido alguma alteração recente e
     * o construtor de grafos esteja configurado.
     *
     * @return {@code true} se o grafo composto foi reconstruído nesta chamada
     */
    public boolean reconstruirCompostoSeNecessario() {
        if (grafoCompostoDesatualizado && construtorGrafos != null && grafoPedestre != null && grafoOnibus != null) {
            this.grafoComposto = construtorGrafos.construirComposto(grafoPedestre, grafoOnibus);
            this.grafoCompostoDesatualizado = false;
            return true;
        }
        return false;
    }

    // Procura ou cria uma linha de ônibus no mapa
    private ILinhaOnibus buscarOuCriarLinha(String codigoLinha) {
        for (ILinhaOnibus existente : mapa.getLinhas()) {
            if (codigoLinha.equalsIgnoreCase(existente.getCodigo())) {
                return existente;
            }
        }
        ILinhaOnibus nova = new LinhaOnibus(codigoLinha, "Linha " + codigoLinha);
        mapa.adicionarLinha(nova);
        return nova;
    }

    // Procura local pelo id no mapa ou nos grafos básicos (tolerante a maiúsculas/minúsculas)
    private ILocal buscarLocalPorId(String id) {
        if (id == null) {
            return null;
        }

        if (mapa != null) {
            ILocal encontrado = mapa.buscarPorId(id);
            if (encontrado != null) {
                return encontrado;
            }
            for (ILocal local : mapa.getLocais()) {
                if (id.equalsIgnoreCase(local.getId())) {
                    return local;
                }
            }
        }

        ILocal noPedestre = buscarLocalNoGrafo(grafoPedestre, id);
        if (noPedestre != null) {
            return noPedestre;
        }

        return buscarLocalNoGrafo(grafoCiclista, id);
    }

    // Procura local pelo id dentro dos vértices de um grafo específico
    private ILocal buscarLocalNoGrafo(IGrafo<ILocal> grafo, String id) {
        if (grafo == null || id == null) {
            return null;
        }
        for (Vertice<ILocal> v : grafo.getVertices()) {
            if (id.equalsIgnoreCase(v.getValor().getId())) {
                return v.getValor();
            }
        }
        return null;
    }

    // Acha a interseção viária mais próxima em linha reta
    private ILocal encontrarIntersecaoMaisProxima(ILocal novoLocal) {
        ILocal maisProximo = null;
        double menorDistancia = Double.POSITIVE_INFINITY;

        if (mapa != null) {
            for (ILocal candidato : mapa.getLocais()) {
                if (candidato.getTipo() == TipoLocal.INTERSECAO) {
                    double d = novoLocal.distanciaMetros(candidato);
                    if (d < menorDistancia) {
                        menorDistancia = d;
                        maisProximo = candidato;
                    }
                }
            }
        } else if (grafoPedestre != null) {
            for (Vertice<ILocal> v : grafoPedestre.getVertices()) {
                ILocal candidato = v.getValor();
                if (candidato.getTipo() == TipoLocal.INTERSECAO) {
                    double d = novoLocal.distanciaMetros(candidato);
                    if (d < menorDistancia) {
                        menorDistancia = d;
                        maisProximo = candidato;
                    }
                }
            }
        }

        return maisProximo;
    }

    // Classe simples para representar locais criados em tempo de execução
    private static class LocalSimples implements ILocal {

        private final String id;
        private final String nome;
        private final double latitude;
        private final double longitude;
        private final TipoLocal tipo;

        private static final double METROS_POR_GRAU_LAT = 111_320.0;
        private static final double METROS_POR_GRAU_LON = 104_430.0;

        public LocalSimples(String id, String nome, double latitude, double longitude, TipoLocal tipo) {
            this.id = id;
            this.nome = nome;
            this.latitude = latitude;
            this.longitude = longitude;
            this.tipo = tipo;
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getNome() {
            return nome;
        }

        @Override
        public double getLatitude() {
            return latitude;
        }

        @Override
        public double getLongitude() {
            return longitude;
        }

        @Override
        public TipoLocal getTipo() {
            return tipo;
        }

        @Override
        public double distanciaMetros(ILocal outro) {
            if (outro == null) {
                return 0.0;
            }
            double dy = (outro.getLatitude() - this.latitude) * METROS_POR_GRAU_LAT;
            double dx = (outro.getLongitude() - this.longitude) * METROS_POR_GRAU_LON;
            return Math.sqrt(dx * dx + dy * dy);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ILocal)) return false;
            ILocal that = (ILocal) o;
            return id != null && id.equalsIgnoreCase(that.getId());
        }

        @Override
        public int hashCode() {
            return (id != null) ? id.toLowerCase().hashCode() : 0;
        }

        @Override
        public String toString() {
            return nome + " [" + id + "]";
        }
    }
}
