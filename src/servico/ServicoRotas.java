package servico;

import dominio.ILocal;
import dominio.IPassoGuia;
import dominio.IResultadoRota;
import dominio.MeioTransporte;
import dominio.Modalidade;
import estruturas.ILista;
import estruturas.ListaEncadeada;
import grafo.IAresta;
import grafo.ICaminho;
import grafo.IGrafo;
import grafo.Vertice;

/**
 * Serviço principal de cálculo e comparação de rotas entre dois locais do bairro,
 * utilizando os grafos de pedestres, ciclistas e o grafo composto multimodal.
 */
public class ServicoRotas implements IServicoRotas {

    private IGrafo<ILocal> grafoPedestre;
    private IGrafo<ILocal> grafoCiclista;
    private IGrafo<ILocal> grafoComposto;
    private ServicoMapa servicoMapa;

    /**
     * Cria o serviço de rotas sem grafos pré-configurados.
     */
    public ServicoRotas() {
    }

    /**
     * Cria o serviço de rotas com os grafos das três modalidades.
     *
     * @param grafoPedestre grafo não dirigido de deslocamento a pé
     * @param grafoCiclista grafo dirigido de deslocamento por bicicleta
     * @param grafoComposto grafo composto em camadas (a pé + ônibus)
     */
    public ServicoRotas(
            IGrafo<ILocal> grafoPedestre,
            IGrafo<ILocal> grafoCiclista,
            IGrafo<ILocal> grafoComposto
    ) {
        this.grafoPedestre = grafoPedestre;
        this.grafoCiclista = grafoCiclista;
        this.grafoComposto = grafoComposto;
    }

    /**
     * Cria o serviço de rotas integrado ao {@link ServicoMapa}, permitindo
     * reconstruir automaticamente o grafo composto quando houver alterações no mapa.
     *
     * @param grafoPedestre grafo não dirigido de deslocamento a pé
     * @param grafoCiclista grafo dirigido de deslocamento por bicicleta
     * @param grafoComposto grafo composto em camadas (a pé + ônibus)
     * @param servicoMapa serviço de manutenção do mapa para sincronização do composto
     */
    public ServicoRotas(
            IGrafo<ILocal> grafoPedestre,
            IGrafo<ILocal> grafoCiclista,
            IGrafo<ILocal> grafoComposto,
            ServicoMapa servicoMapa
    ) {
        this.grafoPedestre = grafoPedestre;
        this.grafoCiclista = grafoCiclista;
        this.grafoComposto = grafoComposto;
        this.servicoMapa = servicoMapa;
    }

    /**
     * Atualiza a referência do grafo de pedestres.
     *
     * @param grafoPedestre novo grafo de pedestres
     */
    public void setGrafoPedestre(IGrafo<ILocal> grafoPedestre) {
        this.grafoPedestre = grafoPedestre;
    }

    /**
     * Atualiza a referência do grafo de ciclistas.
     *
     * @param grafoCiclista novo grafo de ciclistas
     */
    public void setGrafoCiclista(IGrafo<ILocal> grafoCiclista) {
        this.grafoCiclista = grafoCiclista;
    }

    /**
     * Atualiza a referência do grafo composto multimodal.
     *
     * @param grafoComposto novo grafo composto
     */
    public void setGrafoComposto(IGrafo<ILocal> grafoComposto) {
        this.grafoComposto = grafoComposto;
    }

    /**
     * Associa o serviço de manutenção do mapa para reconstruir o grafo composto
     * sob demanda antes de consultas da modalidade {@link Modalidade#A_PE_ONIBUS}.
     *
     * @param servicoMapa serviço de mapa associado
     */
    public void setServicoMapa(ServicoMapa servicoMapa) {
        this.servicoMapa = servicoMapa;
    }

    /**
     * Calcula a melhor rota para uma modalidade específica entre dois locais,
     * verificando alcançabilidade por busca em largura, executando Dijkstra e
     * avaliando a restrição de tempo.
     *
     * @param modalidade modalidade desejada
     * @param p local de origem
     * @param q local de destino
     * @param restricao restrição de tempo aplicada (pode ser nula para sem limite)
     * @return resultado consolidado da rota com guia passo a passo e veredito
     * @throws IllegalArgumentException se modalidade, origem ou destino forem nulos
     */
    @Override
    public IResultadoRota calcular(
            Modalidade modalidade,
            ILocal p,
            ILocal q,
            IRestricaoTempo restricao
    ) {

        if (modalidade == null) {
            throw new IllegalArgumentException("Modalidade nao pode ser nula.");
        }
        if (p == null || q == null) {
            throw new IllegalArgumentException("Origem e destino devem ser informados.");
        }

        // Reconstrói o grafo composto sob demanda caso o mapa tenha sido alterado
        if (modalidade == Modalidade.A_PE_ONIBUS) {
            sincronizarComposto();
        }

        // Pega limite de tempo ou infinito se não tiver
        float limite = (restricao != null) ? restricao.limitePara(modalidade) : Float.POSITIVE_INFINITY;

        // Pega o grafo certo da modalidade
        IGrafo<ILocal> grafo = escolherGrafo(modalidade);

        if (grafo == null) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Localiza os vértices reais no grafo (tolerante caso equals do domínio não esteja por id)
        ILocal origemGrafo = localizarNoGrafo(grafo, p);
        ILocal destinoGrafo = localizarNoGrafo(grafo, q);

        if (origemGrafo == null || destinoGrafo == null) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Se origem e destino são iguais, já chegou
        if (origemGrafo == destinoGrafo || mesmoId(origemGrafo, destinoGrafo)) {
            ILista<IPassoGuia> guiaMesmoPonto = new ListaEncadeada<>();
            guiaMesmoPonto.adicionar(new PassoGuia(
                    MeioTransporte.CAMINHADA,
                    "Voce ja esta no local (" + origemGrafo.getNome() + ").",
                    0.0f
            ));
            return new ResultadoRota(modalidade, true, limite, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, guiaMesmoPonto);
        }

        // Checa primeiro por busca em largura se dá para alcançar o destino
        boolean alcancavel = grafo.existeCaminho(origemGrafo, destinoGrafo);

        if (!alcancavel) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Roda o Dijkstra para pegar o caminho de menor tempo
        ICaminho<ILocal> caminho = grafo.menorCaminho(origemGrafo, destinoGrafo);

        if (caminho == null || !caminho.existe()) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Monta os passos e soma os tempos
        return processarCaminho(modalidade, caminho, limite);
    }

    /**
     * Executa o cálculo para as três modalidades e devolve apenas as opções viáveis
     * dentro da restrição de tempo, ordenadas do menor para o maior tempo total.
     *
     * @param p local de origem
     * @param q local de destino
     * @param restricao restrição de tempo aplicada (pode ser nula)
     * @return lista ordenada das modalidades viáveis
     * @throws IllegalArgumentException se origem ou destino forem nulos
     */
    @Override
    public ILista<IResultadoRota> compararModalidades(
            ILocal p,
            ILocal q,
            IRestricaoTempo restricao
    ) {

        if (p == null || q == null) {
            throw new IllegalArgumentException("Origem e destino nao podem ser nulos.");
        }

        // Calcula cada uma das três
        IResultadoRota rPedestre = calcular(Modalidade.A_PE, p, q, restricao);
        IResultadoRota rComposto = calcular(Modalidade.A_PE_ONIBUS, p, q, restricao);
        IResultadoRota rCiclista = calcular(Modalidade.BICICLETA, p, q, restricao);

        // Guarda as viáveis ordenadas do menor para o maior tempo
        ILista<IResultadoRota> viaveisOrdenadas = new ListaEncadeada<>();

        inserirOrdenado(viaveisOrdenadas, rPedestre);
        inserirOrdenado(viaveisOrdenadas, rComposto);
        inserirOrdenado(viaveisOrdenadas, rCiclista);

        return viaveisOrdenadas;
    }

    // Atualiza o grafo composto se o ServicoMapa indicar alterações pendentes
    private void sincronizarComposto() {
        if (servicoMapa != null && servicoMapa.isGrafoCompostoDesatualizado()) {
            servicoMapa.reconstruirCompostoSeNecessario();
            if (!servicoMapa.isGrafoCompostoDesatualizado() && servicoMapa.getGrafoComposto() != null) {
                this.grafoComposto = servicoMapa.getGrafoComposto();
            }
        }
    }

    // Localiza a instância exata de ILocal armazenada no grafo
    private ILocal localizarNoGrafo(IGrafo<ILocal> grafo, ILocal alvo) {
        if (grafo.obterVertice(alvo) != null) {
            return alvo;
        }
        String idAlvo = alvo.getId();
        if (idAlvo == null) {
            return null;
        }
        for (Vertice<ILocal> v : grafo.getVertices()) {
            if (idAlvo.equalsIgnoreCase(v.getValor().getId())) {
                return v.getValor();
            }
        }
        return null;
    }

    private boolean mesmoId(ILocal a, ILocal b) {
        return a.getId() != null && b.getId() != null && a.getId().equalsIgnoreCase(b.getId());
    }

    // Escolhe o grafo de acordo com a modalidade
    private IGrafo<ILocal> escolherGrafo(Modalidade modalidade) {
        switch (modalidade) {
            case A_PE:
                return grafoPedestre;
            case BICICLETA:
                return grafoCiclista;
            case A_PE_ONIBUS:
                return grafoComposto;
            default:
                return null;
        }
    }

    // Processa arestas, soma tempos parciais e junta passos da mesma rua
    private IResultadoRota processarCaminho(
            Modalidade modalidade,
            ICaminho<ILocal> caminho,
            float limite
    ) {

        float tempoCaminhada = 0.0f;
        float tempoEspera = 0.0f;
        float tempoOnibus = 0.0f;
        float tempoBicicleta = 0.0f;

        ILista<IAresta<ILocal>> arestas = caminho.getArestas();
        ILista<IPassoGuia> guia = new ListaEncadeada<>();

        // Variáveis para acumular passos seguidos na mesma rua
        MeioTransporte meioAtual = null;
        String rotuloAtual = null;
        float minutosAcumulados = 0.0f;
        String ultimoDestinoNome = "";

        for (IAresta<ILocal> aresta : arestas) {

            MeioTransporte meioAresta = identificarMeio(modalidade, aresta);
            float peso = aresta.getPeso();
            String rotulo = aresta.getRotulo();
            String nomeDestino = aresta.getDestino().getValor().getNome();

            // Soma no tempo do transporte correspondente
            switch (meioAresta) {
                case CAMINHADA:
                    tempoCaminhada += peso;
                    break;
                case ESPERA:
                    tempoEspera += peso;
                    break;
                case ONIBUS:
                    tempoOnibus += peso;
                    break;
                case BICICLETA:
                    tempoBicicleta += peso;
                    break;
            }

            // Se for o mesmo transporte e mesma rua, acumula o tempo
            if (meioAtual != null && meioAtual == meioAresta && rotulo != null && rotulo.equalsIgnoreCase(rotuloAtual)) {
                minutosAcumulados += peso;
                ultimoDestinoNome = nomeDestino;
            } else {
                // Mudou de rua ou de transporte, descarrega o passo anterior
                if (meioAtual != null) {
                    guia.adicionar(criarPasso(meioAtual, rotuloAtual, minutosAcumulados, ultimoDestinoNome));
                }

                // Inicia novo passo
                meioAtual = meioAresta;
                rotuloAtual = rotulo;
                minutosAcumulados = peso;
                ultimoDestinoNome = nomeDestino;
            }
        }

        // Descarrega o último passo
        if (meioAtual != null) {
            guia.adicionar(criarPasso(meioAtual, rotuloAtual, minutosAcumulados, ultimoDestinoNome));
        }

        float tempoTotal = tempoCaminhada + tempoEspera + tempoOnibus + tempoBicicleta;

        return new ResultadoRota(
                modalidade,
                true,
                limite,
                tempoTotal,
                tempoCaminhada,
                tempoEspera,
                tempoOnibus,
                tempoBicicleta,
                guia
        );
    }

    // Descobre o meio de transporte de cada aresta
    private MeioTransporte identificarMeio(Modalidade modalidade, IAresta<ILocal> aresta) {
        if (modalidade == Modalidade.A_PE) {
            return MeioTransporte.CAMINHADA;
        }

        if (modalidade == Modalidade.BICICLETA) {
            return MeioTransporte.BICICLETA;
        }

        // No grafo composto, checa pelos ids dos vértices:
        // id com '@' é parada de linha (camada B - ônibus). Sem '@' é ponto físico (camada A).
        String idOrigem = aresta.getOrigem().getValor().getId();
        String idDestino = aresta.getDestino().getValor().getId();

        boolean origemOnibus = idOrigem != null && idOrigem.contains("@");
        boolean destinoOnibus = idDestino != null && idDestino.contains("@");

        if (!origemOnibus && !destinoOnibus) {
            return MeioTransporte.CAMINHADA;
        } else if (!origemOnibus && destinoOnibus) {
            return MeioTransporte.ESPERA; // Transição A -> B (embarque)
        } else if (origemOnibus && destinoOnibus) {
            return MeioTransporte.ONIBUS; // Trecho na linha B -> B
        } else {
            return MeioTransporte.CAMINHADA; // Transição B -> A (desembarque, custo 0)
        }
    }

    // Monta o texto de cada passo
    private IPassoGuia criarPasso(MeioTransporte meio, String rotulo, float minutos, String destinoNome) {
        String descricao;

        // Se o tempo for zero e tiver destino, trata como desembarque
        if (minutos <= 0.0001f && meio == MeioTransporte.CAMINHADA && destinoNome != null) {
            descricao = String.format("Desembarque no ponto %s", destinoNome);
            return new PassoGuia(meio, descricao, 0.0f);
        }

        switch (meio) {
            case CAMINHADA:
                if (rotulo != null && !rotulo.trim().isEmpty()) {
                    descricao = String.format("Caminhe por %.1f min pela %s ate %s", minutos, rotulo, destinoNome);
                } else {
                    descricao = String.format("Caminhe por %.1f min ate %s", minutos, destinoNome);
                }
                break;

            case ESPERA:
                if (rotulo != null && !rotulo.trim().isEmpty()) {
                    descricao = String.format("Aguarde no ponto e embarque na linha %s (espera de %.1f min)", rotulo, minutos);
                } else {
                    descricao = String.format("Aguarde no ponto e embarque no onibus (espera de %.1f min)", minutos);
                }
                break;

            case ONIBUS:
                if (rotulo != null && !rotulo.trim().isEmpty()) {
                    descricao = String.format("Siga no onibus (linha %s) por %.1f min ate %s", rotulo, minutos, destinoNome);
                } else {
                    descricao = String.format("Siga no onibus por %.1f min ate %s", minutos, destinoNome);
                }
                break;

            case BICICLETA:
                if (rotulo != null && !rotulo.trim().isEmpty()) {
                    descricao = String.format("Pedale por %.1f min pela via %s ate %s", minutos, rotulo, destinoNome);
                } else {
                    descricao = String.format("Pedale por %.1f min ate %s", minutos, destinoNome);
                }
                break;

            default:
                descricao = String.format("Siga por %.1f min ate %s", minutos, destinoNome);
        }

        return new PassoGuia(meio, descricao, minutos);
    }

    // Insere ordenado pelo menor tempo total
    private void inserirOrdenado(ILista<IResultadoRota> lista, IResultadoRota rota) {
        if (rota == null || !rota.isViavel()) {
            return;
        }

        // Se a lista estiver vazia, adiciona direto
        if (lista.estaVazia()) {
            lista.adicionar(rota);
            return;
        }

        // Lista auxiliar para reconstruir na ordem
        ILista<IResultadoRota> ordenada = new ListaEncadeada<>();
        boolean inserido = false;

        for (IResultadoRota atual : lista) {
            if (!inserido && rota.compareTo(atual) < 0) {
                ordenada.adicionar(rota);
                inserido = true;
            }
            ordenada.adicionar(atual);
        }

        if (!inserido) {
            ordenada.adicionar(rota);
        }

        // Devolve os itens ordenados para a lista original
        while (!lista.estaVazia()) {
            lista.removerPrimeiro();
        }
        for (IResultadoRota item : ordenada) {
            lista.adicionar(item);
        }
    }
}
