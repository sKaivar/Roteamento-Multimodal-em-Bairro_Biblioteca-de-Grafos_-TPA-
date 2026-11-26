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

/**
 * Serviço responsável por calcular e comparar rotas entre locais do bairro.
 *
 * Utiliza os grafos de pedestre, ciclista e o grafo composto (a pé + ônibus),
 * executando busca em largura para verificar viabilidade e Dijkstra para
 * encontrar o caminho de menor tempo.
 */
public class ServicoRotas implements IServicoRotas {

    private IGrafo<ILocal> grafoPedestre;
    private IGrafo<ILocal> grafoCiclista;
    private IGrafo<ILocal> grafoComposto;

    // Construtor padrão
    public ServicoRotas() {
    }

    // Construtor recebendo os grafos já montados
    public ServicoRotas(
            IGrafo<ILocal> grafoPedestre,
            IGrafo<ILocal> grafoCiclista,
            IGrafo<ILocal> grafoComposto
    ) {
        this.grafoPedestre = grafoPedestre;
        this.grafoCiclista = grafoCiclista;
        this.grafoComposto = grafoComposto;
    }

    // Atualiza o grafo de pedestres
    public void setGrafoPedestre(IGrafo<ILocal> grafoPedestre) {
        this.grafoPedestre = grafoPedestre;
    }

    // Atualiza o grafo de ciclistas
    public void setGrafoCiclista(IGrafo<ILocal> grafoCiclista) {
        this.grafoCiclista = grafoCiclista;
    }

    // Atualiza o grafo composto (ônibus + caminhada)
    public void setGrafoComposto(IGrafo<ILocal> grafoComposto) {
        this.grafoComposto = grafoComposto;
    }

    /**
     * Calcula o trajeto mínimo para uma modalidade específica.
     */
    @Override
    public IResultadoRota calcular(
            Modalidade modalidade,
            ILocal p,
            ILocal q,
            IRestricaoTempo restricao
    ) {

        if (modalidade == null) {
            throw new IllegalArgumentException("A modalidade não pode ser nula.");
        }
        if (p == null || q == null) {
            throw new IllegalArgumentException("Os locais de origem e destino devem ser informados.");
        }

        // Aplica o limite definido na restrição (ou infinito se não houver)
        float limite = (restricao != null) ? restricao.limitePara(modalidade) : Float.POSITIVE_INFINITY;

        // Seleciona o grafo adequado para a modalidade escolhida
        IGrafo<ILocal> grafo = escolherGrafo(modalidade);

        if (grafo == null) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Verifica se os locais existem no grafo
        if (grafo.obterVertice(p) == null || grafo.obterVertice(q) == null) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Caso especial: origem e destino são o mesmo ponto
        if (p.equals(q)) {
            ILista<IPassoGuia> guiaMesmoPonto = new ListaEncadeada<>();
            guiaMesmoPonto.adicionar(new PassoGuia(
                    MeioTransporte.CAMINHADA,
                    "Você já está no destino (" + p.getNome() + ").",
                    0.0f
            ));
            return new ResultadoRota(modalidade, true, limite, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, guiaMesmoPonto);
        }

        // Passo 1: Busca em largura para checar se o destino é alcançável
        boolean alcancavel = grafo.existeCaminho(p, q);

        if (!alcancavel) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Passo 2: Dijkstra para obter o caminho com menor tempo total
        ICaminho<ILocal> caminho = grafo.menorCaminho(p, q);

        if (caminho == null || !caminho.existe()) {
            return ResultadoRota.semRota(modalidade, limite);
        }

        // Passo 3: Percorre as arestas calculando tempos e montando o guia textual
        return processarCaminho(modalidade, caminho, limite);
    }

    /**
     * Executa as 3 modalidades e devolve apenas as viáveis, ordenadas pelo menor tempo total.
     */
    @Override
    public ILista<IResultadoRota> compararModalidades(
            ILocal p,
            ILocal q,
            IRestricaoTempo restricao
    ) {

        if (p == null || q == null) {
            throw new IllegalArgumentException("Origem e destino não podem ser nulos.");
        }

        // Calcula cada uma das três opções
        IResultadoRota rPedestre = calcular(Modalidade.A_PE, p, q, restricao);
        IResultadoRota rComposto = calcular(Modalidade.A_PE_ONIBUS, p, q, restricao);
        IResultadoRota rCiclista = calcular(Modalidade.BICICLETA, p, q, restricao);

        // Lista encadeada própria para guardar apenas as viáveis ordenadas
        ILista<IResultadoRota> viaveisOrdenadas = new ListaEncadeada<>();

        inserirOrdenado(viaveisOrdenadas, rPedestre);
        inserirOrdenado(viaveisOrdenadas, rComposto);
        inserirOrdenado(viaveisOrdenadas, rCiclista);

        return viaveisOrdenadas;
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    // Escolhe o grafo de acordo com a modalidade solicitada
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

    // Processa as arestas do caminho, soma os tempos parciais e agrupa passos consecutivos
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

        // Variáveis para acumular passos consecutivos na mesma via
        MeioTransporte meioAtual = null;
        String rotuloAtual = null;
        float minutosAcumulados = 0.0f;
        String ultimoDestinoNome = "";

        for (IAresta<ILocal> aresta : arestas) {

            MeioTransporte meioAresta = identificarMeio(modalidade, aresta);
            float peso = aresta.getPeso();
            String rotulo = aresta.getRotulo();
            String nomeDestino = aresta.getDestino().getValor().getNome();

            // Soma nos acumuladores de tempo por categoria
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

            // Regra de fusão de passos: se for o mesmo meio e mesmo rótulo, agrupa
            if (meioAtual != null && meioAtual == meioAresta && rotulo != null && rotulo.equalsIgnoreCase(rotuloAtual)) {
                minutosAcumulados += peso;
                ultimoDestinoNome = nomeDestino;
            } else {
                // Descarrega o passo anterior agrupado, se houver
                if (meioAtual != null) {
                    guia.adicionar(criarPasso(meioAtual, rotuloAtual, minutosAcumulados, ultimoDestinoNome));
                }

                // Inicia novo grupo de passos
                meioAtual = meioAresta;
                rotuloAtual = rotulo;
                minutosAcumulados = peso;
                ultimoDestinoNome = nomeDestino;
            }
        }

        // Descarrega o último passo restante após o fim do laço
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

    // Identifica o meio de transporte de uma aresta
    private MeioTransporte identificarMeio(Modalidade modalidade, IAresta<ILocal> aresta) {
        if (modalidade == Modalidade.A_PE) {
            return MeioTransporte.CAMINHADA;
        }

        if (modalidade == Modalidade.BICICLETA) {
            return MeioTransporte.BICICLETA;
        }

        // No grafo composto (modalidade A_PE_ONIBUS), deduzimos pelas camadas dos vértices:
        // Se o id tem '@', pertence à camada B (ônibus por linha).
        // Se não tem '@', pertence à camada A (física / pedestre).
        String idOrigem = aresta.getOrigem().getValor().getId();
        String idDestino = aresta.getDestino().getValor().getId();

        boolean origemOnibus = idOrigem != null && idOrigem.contains("@");
        boolean destinoOnibus = idDestino != null && idDestino.contains("@");

        if (!origemOnibus && !destinoOnibus) {
            return MeioTransporte.CAMINHADA;
        } else if (!origemOnibus && destinoOnibus) {
            return MeioTransporte.ESPERA; // Transição A -> B (embarque)
        } else if (origemOnibus && destinoOnibus) {
            return MeioTransporte.ONIBUS; // Viagem na linha B -> B
        } else {
            return MeioTransporte.CAMINHADA; // Transição B -> A (desembarque: custo zero)
        }
    }

    // Gera a descrição amigável de cada passo
    private IPassoGuia criarPasso(MeioTransporte meio, String rotulo, float minutos, String destinoNome) {
        String descricao;

        switch (meio) {
            case CAMINHADA:
                if (rotulo != null && !rotulo.trim().isEmpty()) {
                    descricao = String.format("Caminhe por %.1f min pela %s até %s", minutos, rotulo, destinoNome);
                } else {
                    descricao = String.format("Caminhe por %.1f min até %s", minutos, destinoNome);
                }
                break;

            case ESPERA:
                if (rotulo != null && !rotulo.trim().isEmpty()) {
                    descricao = String.format("Aguarde no ponto e embarque na linha %s (espera de %.1f min)", rotulo, minutos);
                } else {
                    descricao = String.format("Aguarde no ponto e embarque no ônibus (espera de %.1f min)", minutos);
                }
                break;

            case ONIBUS:
                if (rotulo != null && !rotulo.trim().isEmpty()) {
                    descricao = String.format("Siga no ônibus (linha %s) por %.1f min até %s", rotulo, minutos, destinoNome);
                } else {
                    descricao = String.format("Siga no ônibus por %.1f min até %s", minutos, destinoNome);
                }
                break;

            case BICICLETA:
                if (rotulo != null && !rotulo.trim().isEmpty()) {
                    descricao = String.format("Pedale por %.1f min pela via %s até %s", minutos, rotulo, destinoNome);
                } else {
                    descricao = String.format("Pedale por %.1f min até %s", minutos, destinoNome);
                }
                break;

            default:
                descricao = String.format("Siga por %.1f min até %s", minutos, destinoNome);
        }

        return new PassoGuia(meio, descricao, minutos);
    }

    // Insere ordenado pelo tempo total, mantendo apenas rotas viáveis
    private void inserirOrdenado(ILista<IResultadoRota> lista, IResultadoRota rota) {
        if (rota == null || !rota.isViavel()) {
            return;
        }

        // Se a lista estiver vazia, apenas adiciona
        if (lista.estaVazia()) {
            lista.adicionar(rota);
            return;
        }

        // Cria uma nova lista auxiliar para reconstruir na ordem correta
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

        // Limpa e repassa os elementos para a lista original
        while (!lista.estaVazia()) {
            lista.removerPrimeiro();
        }
        for (IResultadoRota item : ordenada) {
            lista.adicionar(item);
        }
    }
}
