package servico;

import dominio.ILocal;
import dominio.IMapa;
import dominio.SentidoBicicleta;
import dominio.TipoLocal;
import estruturas.ILista;
import grafo.IGrafo;
import grafo.Vertice;

// Servico para adicionar coisas novas no mapa depois de carregado
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

    // Construtor vazio
    public ServicoMapa() {
    }

    // Construtor recebendo as referencias
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

    public IMapa getMapa() {
        return mapa;
    }

    public void setMapa(IMapa mapa) {
        this.mapa = mapa;
    }

    public void setConstrutorGrafos(IConstrutorGrafos construtorGrafos) {
        this.construtorGrafos = construtorGrafos;
    }

    public void setModeloCusto(IModeloCusto modeloCusto) {
        this.modeloCusto = modeloCusto;
    }

    public IGrafo<ILocal> getGrafoPedestre() {
        return grafoPedestre;
    }

    public void setGrafoPedestre(IGrafo<ILocal> grafoPedestre) {
        this.grafoPedestre = grafoPedestre;
    }

    public IGrafo<ILocal> getGrafoCiclista() {
        return grafoCiclista;
    }

    public void setGrafoCiclista(IGrafo<ILocal> grafoCiclista) {
        this.grafoCiclista = grafoCiclista;
    }

    public IGrafo<ILocal> getGrafoOnibus() {
        return grafoOnibus;
    }

    public void setGrafoOnibus(IGrafo<ILocal> grafoOnibus) {
        this.grafoOnibus = grafoOnibus;
    }

    public IGrafo<ILocal> getGrafoComposto() {
        return grafoComposto;
    }

    public void setGrafoComposto(IGrafo<ILocal> grafoComposto) {
        this.grafoComposto = grafoComposto;
    }

    public boolean isGrafoCompostoDesatualizado() {
        return grafoCompostoDesatualizado;
    }

    // Cria um local novo e conecta no mais proximo a pe se pedir
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

        String novoId = "L" + (++contadorLocaisNovos);
        ILocal novoLocal = new LocalSimples(novoId, nome, latitude, longitude, TipoLocal.INTERESSE);

        // Adiciona aos grafos basicos
        if (grafoPedestre != null) {
            grafoPedestre.adicionarVertice(novoLocal);
        }
        if (grafoCiclista != null) {
            grafoCiclista.adicionarVertice(novoLocal);
        }

        // Se pediu, acha a esquina mais proxima e liga a pe
        if (ligarAoMaisProximo) {
            ILocal maisProximo = encontrarIntersecaoMaisProxima(novoLocal);

            if (maisProximo != null && grafoPedestre != null && modeloCusto != null) {
                double distancia = novoLocal.distanciaMetros(maisProximo);
                float tempoMinutos = modeloCusto.tempoCaminhada(distancia);

                // Em grafo nao dirigido, cria ida e volta
                grafoPedestre.adicionarAresta(novoLocal, maisProximo, tempoMinutos, "Acesso a pe");
            }
        }

        // Adiciona no mapa se tiver
        if (mapa != null) {
            mapa.adicionarLocal(novoLocal);
        }

        // Marca que o composto precisa ser refeito na proxima consulta
        this.grafoCompostoDesatualizado = true;

        return novoLocal;
    }

    // Adiciona uma nova rua entre dois locais
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
        if (metros < 0) {
            throw new IllegalArgumentException("Distancia nao pode ser negativa.");
        }

        ILocal origem = buscarLocalPorId(idOrigem);
        ILocal destino = buscarLocalPorId(idDestino);

        if (origem == null || destino == null) {
            throw new IllegalArgumentException("Local nao encontrado no mapa.");
        }

        String rotulo = (nomeVia != null) ? nomeVia : "";

        // Adiciona no grafo de pedestres se for permitido a pe
        if (pedestre && grafoPedestre != null && modeloCusto != null) {
            float tempoPe = modeloCusto.tempoCaminhada(metros);
            grafoPedestre.adicionarAresta(origem, destino, tempoPe, rotulo);
        }

        // Adiciona no grafo de bicicleta se permitido
        if (bici != null && bici != SentidoBicicleta.NENHUM && grafoCiclista != null && modeloCusto != null) {
            float tempoBike = modeloCusto.tempoBicicleta(metros, ciclovia);

            grafoCiclista.adicionarAresta(origem, destino, tempoBike, rotulo);

            // Se for mao dupla ou ciclovia, adiciona o sentido inverso
            if (bici == SentidoBicicleta.AMBOS || ciclovia) {
                grafoCiclista.adicionarAresta(destino, origem, tempoBike, rotulo);
            }
        }

        this.grafoCompostoDesatualizado = true;
    }

    // Adiciona trecho de linha de onibus
    @Override
    public void adicionarTrechoOnibus(
            String codigoLinha,
            String idParadaOrigem,
            String idParadaDestino,
            double metros
    ) {

        if (codigoLinha == null || idParadaOrigem == null || idParadaDestino == null) {
            throw new IllegalArgumentException("Linha e paradas devem ser informadas.");
        }
        if (metros < 0) {
            throw new IllegalArgumentException("Distancia invalida.");
        }

        if (grafoOnibus != null && modeloCusto != null) {
            String idVerticeOrigem = idParadaOrigem + "@" + codigoLinha;
            String idVerticeDestino = idParadaDestino + "@" + codigoLinha;

            ILocal localVerticeOrigem = new LocalSimples(idVerticeOrigem, "Ponto " + idParadaOrigem + " (" + codigoLinha + ")", 0, 0, TipoLocal.PARADA);
            ILocal localVerticeDestino = new LocalSimples(idVerticeDestino, "Ponto " + idParadaDestino + " (" + codigoLinha + ")", 0, 0, TipoLocal.PARADA);

            grafoOnibus.adicionarVertice(localVerticeOrigem);
            grafoOnibus.adicionarVertice(localVerticeDestino);

            float tempoOnibus = modeloCusto.tempoOnibus(metros);
            grafoOnibus.adicionarAresta(localVerticeOrigem, localVerticeDestino, tempoOnibus, codigoLinha);
        }

        this.grafoCompostoDesatualizado = true;
    }

    // Reconstrói o grafo composto se tiver mudanca recente
    public void reconstruirCompostoSeNecessario() {
        if (grafoCompostoDesatualizado && construtorGrafos != null && grafoPedestre != null && grafoOnibus != null) {
            this.grafoComposto = construtorGrafos.construirComposto(grafoPedestre, grafoOnibus);
            this.grafoCompostoDesatualizado = false;
        }
    }

    // Procura local pelo id no mapa ou nos grafos
    private ILocal buscarLocalPorId(String id) {
        if (id == null) {
            return null;
        }

        if (mapa != null) {
            ILocal encontrado = mapa.buscarPorId(id);
            if (encontrado != null) {
                return encontrado;
            }
        }

        if (grafoPedestre != null) {
            for (Vertice<ILocal> v : grafoPedestre.getVertices()) {
                if (id.equalsIgnoreCase(v.getValor().getId())) {
                    return v.getValor();
                }
            }
        }

        return null;
    }

    // Acha a intersecao viaria mais proxima em linha reta
    private ILocal encontrarIntersecaoMaisProxima(ILocal novoLocal) {
        ILocal maisProximo = null;
        double menorDistancia = Double.POSITIVE_INFINITY;

        ILista<ILocal> locais = (mapa != null) ? mapa.getLocais() : null;

        if (locais != null) {
            for (ILocal candidato : locais) {
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

    // Classe simples para representar locais criados em tempo de execucao
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
