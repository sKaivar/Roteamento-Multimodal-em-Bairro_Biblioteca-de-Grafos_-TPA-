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

// Servico que calcula e compara rotas entre locais do bairro
public class ServicoRotas implements IServicoRotas {

    private IGrafo<ILocal> grafoPedestre;
    private IGrafo<ILocal> grafoCiclista;
    private IGrafo<ILocal> grafoComposto;

    // Construtor vazio
    public ServicoRotas() {
    }

    // Construtor recebendo os grafos
    public ServicoRotas(
            IGrafo<ILocal> grafoPedestre,
            IGrafo<ILocal> grafoCiclista,
            IGrafo<ILocal> grafoComposto
    ) {
        this.grafoPedestre = grafoPedestre;
        this.grafoCiclista = grafoCiclista;
        this.grafoComposto = grafoComposto;
    }

    // Atualiza grafo pedestre
    public void setGrafoPedestre(IGrafo<ILocal> grafoPedestre) {
        this.grafoPedestre = grafoPedestre;
    }

    // Atualiza grafo ciclista
    public void setGrafoCiclista(IGrafo<ILocal> grafoCiclista) {
        this.grafoCiclista = grafoCiclista;
    }

    // Atualiza grafo composto
    public void setGrafoComposto(IGrafo<ILocal> grafoComposto) {
        this.grafoComposto = grafoComposto;
    }

    // Calcula rota para uma modalidade especifica
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

        // Pega limite de tempo ou infinito se nao tiver
        float limite = (restricao != null) ? restricao.limitePara(modalidade) : Float.POSITIVE_INFINITY;

        // Pega o grafo certo da modalidade
        IGrafo<ILocal> grafo = escolherGrafo(modalidade);

        if (grafo == null) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Checa se os dois locais existem no grafo
        if (grafo.obterVertice(p) == null || grafo.obterVertice(q) == null) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Se origem e destino sao iguais, ja chegou
        if (p.equals(q)) {
            ILista<IPassoGuia> guiaMesmoPonto = new ListaEncadeada<>();
            guiaMesmoPonto.adicionar(new PassoGuia(
                    MeioTransporte.CAMINHADA,
                    "Voce ja esta no local (" + p.getNome() + ").",
                    0.0f
            ));
            return new ResultadoRota(modalidade, true, limite, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, guiaMesmoPonto);
        }

        // Checa primeiro por busca em largura se da pra alcancar o destino
        boolean alcancavel = grafo.existeCaminho(p, q);

        if (!alcancavel) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Roda o dijkstra para pegar o caminho de menor tempo
        ICaminho<ILocal> caminho = grafo.menorCaminho(p, q);

        if (caminho == null || !caminho.existe()) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Monta os passos e soma os tempos
        return processarCaminho(modalidade, caminho, limite);
    }

    // Executa as tres modalidades e devolve as viaveis ordenadas
    @Override
    public ILista<IResultadoRota> compararModalidades(
            ILocal p,
            ILocal q,
            IRestricaoTempo restricao
    ) {

        if (p == null || q == null) {
            throw new IllegalArgumentException("Origem e destino nao podem ser nulos.");
        }

        // Calcula cada uma das tres
        IResultadoRota rPedestre = calcular(Modalidade.A_PE, p, q, restricao);
        IResultadoRota rComposto = calcular(Modalidade.A_PE_ONIBUS, p, q, restricao);
        IResultadoRota rCiclista = calcular(Modalidade.BICICLETA, p, q, restricao);

        // Guarda as viaveis ordenadas do menor para o maior tempo
        ILista<IResultadoRota> viaveisOrdenadas = new ListaEncadeada<>();

        inserirOrdenado(viaveisOrdenadas, rPedestre);
        inserirOrdenado(viaveisOrdenadas, rComposto);
        inserirOrdenado(viaveisOrdenadas, rCiclista);

        return viaveisOrdenadas;
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

        // Variaveis para acumular passos seguidos na mesma rua
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

        // Descarrega o ultimo passo
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

        // No grafo composto, checa pelos ids dos vertices:
        // id com '@' e parada de linha (camada B - onibus). Sem '@' e ponto fisico (camada A).
        String idOrigem = aresta.getOrigem().getValor().getId();
        String idDestino = aresta.getDestino().getValor().getId();

        boolean origemOnibus = idOrigem != null && idOrigem.contains("@");
        boolean destinoOnibus = idDestino != null && idDestino.contains("@");

        if (!origemOnibus && !destinoOnibus) {
            return MeioTransporte.CAMINHADA;
        } else if (!origemOnibus && destinoOnibus) {
            return MeioTransporte.ESPERA; // Transicao A -> B (embarque)
        } else if (origemOnibus && destinoOnibus) {
            return MeioTransporte.ONIBUS; // Trecho na linha B -> B
        } else {
            return MeioTransporte.CAMINHADA; // Transicao B -> A (desembarque, custo 0)
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
