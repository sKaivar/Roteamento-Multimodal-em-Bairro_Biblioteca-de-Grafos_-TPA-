package apresentacao;

import dominio.ILocal;
import dominio.IMapa;
import dominio.IResultadoRota;
import dominio.Modalidade;
import dominio.SentidoBicicleta;
import estruturas.ILista;
import grafo.IGrafo;
import grafo.Vertice;
import servico.IServicoMapa;
import servico.IServicoRotas;
import servico.RestricaoTempo;
import servico.ServicoMapa;
import servico.ServicoRotas;

import java.util.Scanner;

/**
 * Interface de interação com o usuário através do console.
 *
 * Responsável por apresentar o menu e coordenar
 * as operações disponíveis no aplicativo.
 */
public class MenuConsole {

    private final String caminhoArquivo;
    private final Scanner entrada;

    private IMapa mapa;
    private IGrafo<ILocal> grafoPedestre;
    private IGrafo<ILocal> grafoCiclista;
    private IGrafo<ILocal> grafoOnibus;
    private IGrafo<ILocal> grafoComposto;

    private IServicoRotas servicoRotas;
    private IServicoMapa servicoMapa;

    /**
     * Construtor do menu.
     *
     * @param caminhoArquivo caminho do arquivo de dados
     */
    public MenuConsole(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        this.entrada = new Scanner(System.in);
        this.servicoRotas = new ServicoRotas();
        this.servicoMapa = new ServicoMapa();
    }

    /**
     * Inicia o aplicativo.
     */
    public void iniciar() {
        System.out.println("========================================");
        System.out.println("       SISTEMA DE ROTAS - JARDIM CAMBURI");
        System.out.println("========================================");

        carregarMapa();

        executarMenu();
    }

    // ==========================================
    // CARREGAMENTO DO MAPA
    // ==========================================

    /**
     * Carrega o mapa a partir do arquivo TXT
     * e constrói os grafos utilizados pelo sistema.
     */
    private void carregarMapa() {
        System.out.println("\nCarregando dados do mapa...");
        System.out.println("Arquivo de entrada: " + caminhoArquivo);

        /*
         * Integração com as camadas de persistência e serviço:
         * Quando o Membro 2 finalizar o RepositorioMapaArquivo e ConstrutorGrafos,
         * o carregamento real do arquivo bairro.txt alimentará este ponto.
         */
        if (mapa == null) {
            System.out.println("Aviso: Leitor de arquivo (Membro 2) em desenvolvimento.");
            System.out.println("A estrutura de serviços e rotas está pronta para receber os grafos.");
        }
    }

    // Configura os grafos no menu para uso imediato (inclusive em testes)
    public void configurarServicos(
            IMapa mapa,
            IGrafo<ILocal> pedestre,
            IGrafo<ILocal> ciclista,
            IGrafo<ILocal> onibus,
            IGrafo<ILocal> composto
    ) {
        this.mapa = mapa;
        this.grafoPedestre = pedestre;
        this.grafoCiclista = ciclista;
        this.grafoOnibus = onibus;
        this.grafoComposto = composto;

        ServicoRotas rotas = new ServicoRotas(pedestre, ciclista, composto);
        this.servicoRotas = rotas;

        ServicoMapa servMapa = new ServicoMapa(mapa, null, null, pedestre, ciclista, onibus, composto);
        this.servicoMapa = servMapa;
    }

    // ==========================================
    // MENU PRINCIPAL
    // ==========================================

    /**
     * Apresenta o menu e recebe as opções do usuário.
     */
    private void executarMenu() {
        int opcao;

        do {
            mostrarMenu();
            System.out.print("\nEscolha uma opção: ");

            try {
                opcao = Integer.parseInt(entrada.nextLine().trim());
                processarOpcao(opcao);
            } catch (NumberFormatException e) {
                opcao = -1;
                System.out.println("Por favor, digite um número correspondente ao menu.");
            }

        } while (opcao != 0);
    }

    /**
     * Exibe as opções disponíveis.
     */
    private void mostrarMenu() {
        System.out.println("\n========================================");
        System.out.println("              MENU PRINCIPAL");
        System.out.println("========================================");
        System.out.println("1. Exibir informações do mapa");
        System.out.println("2. Executar busca em largura (BFS)");
        System.out.println("3. Calcular rota");
        System.out.println("4. Comparar modalidades de transporte");
        System.out.println("----------------------------------------");
        System.out.println("5. Adicionar novo local");
        System.out.println("6. Adicionar novo trecho");
        System.out.println("7. Adicionar trecho de ônibus");
        System.out.println("----------------------------------------");
        System.out.println("0. Sair");
    }

    /**
     * Direciona a opção selecionada para sua funcionalidade.
     */
    private void processarOpcao(int opcao) {
        switch (opcao) {
            case 1:
                exibirInformacoesMapa();
                break;
            case 2:
                executarBuscaLargura();
                break;
            case 3:
                calcularRota();
                break;
            case 4:
                compararModalidades();
                break;
            case 5:
                adicionarLocal();
                break;
            case 6:
                adicionarTrecho();
                break;
            case 7:
                adicionarTrechoOnibus();
                break;
            case 0:
                System.out.println("\nEncerrando aplicação...");
                break;
            default:
                System.out.println("\nOpção inválida.");
        }
    }

    // ==========================================
    // FUNCIONALIDADES DO APLICATIVO
    // ==========================================

    private void exibirInformacoesMapa() {
        System.out.println("\n--- Informações do Mapa ---");
        if (grafoPedestre == null && grafoCiclista == null && grafoComposto == null) {
            System.out.println("Nenhum grafo carregado no momento.");
            return;
        }

        if (grafoPedestre != null) {
            System.out.printf("Grafo de Pedestres: %d vértices, %d arestas\n",
                    grafoPedestre.quantidadeVertices(), grafoPedestre.quantidadeArestas());
        }
        if (grafoCiclista != null) {
            System.out.printf("Grafo de Ciclistas: %d vértices, %d arestas\n",
                    grafoCiclista.quantidadeVertices(), grafoCiclista.quantidadeArestas());
        }
        if (grafoOnibus != null) {
            System.out.printf("Grafo de Ônibus: %d vértices, %d arestas\n",
                    grafoOnibus.quantidadeVertices(), grafoOnibus.quantidadeArestas());
        }
        if (grafoComposto != null) {
            System.out.printf("Grafo Composto (Multimodal): %d vértices, %d arestas\n",
                    grafoComposto.quantidadeVertices(), grafoComposto.quantidadeArestas());
        }
    }

    private void executarBuscaLargura() {
        System.out.println("\n--- Busca em Largura (BFS) ---");
        IGrafo<ILocal> grafoEscolhido = selecionarGrafo();

        if (grafoEscolhido == null) {
            System.out.println("Grafo indisponível.");
            return;
        }

        ILocal origem = obterLocalPorEntrada("Digite o ID ou nome do local de partida: ", grafoEscolhido);
        if (origem == null) {
            System.out.println("Local não encontrado.");
            return;
        }

        System.out.println("\nExecutando caminhamento em largura a partir de: " + origem.getNome());
        String resultado = grafoEscolhido.caminharEmLargura(origem);
        System.out.println(resultado);
    }

    private void calcularRota() {
        System.out.println("\n--- Calcular Rota ---");

        System.out.println("Escolha a modalidade:");
        System.out.println("1 - A pé");
        System.out.println("2 - A pé + Ônibus");
        System.out.println("3 - Bicicleta");
        System.out.print("Opção: ");

        String opcMod = entrada.nextLine().trim();
        Modalidade modalidade;

        if ("1".equals(opcMod)) {
            modalidade = Modalidade.A_PE;
        } else if ("2".equals(opcMod)) {
            modalidade = Modalidade.A_PE_ONIBUS;
        } else if ("3".equals(opcMod)) {
            modalidade = Modalidade.BICICLETA;
        } else {
            System.out.println("Modalidade inválida.");
            return;
        }

        IGrafo<ILocal> grafo = selecionarGrafoPorModalidade(modalidade);
        if (grafo == null) {
            System.out.println("Grafo correspondente ainda não carregado.");
            return;
        }

        ILocal p = obterLocalPorEntrada("Informe o local de ORIGEM (P): ", grafo);
        if (p == null) return;

        ILocal q = obterLocalPorEntrada("Informe o local de DESTINO (Q): ", grafo);
        if (q == null) return;

        float limite = obterLimiteTempo();
        RestricaoTempo restricao = new RestricaoTempo(limite);

        System.out.println("\nCalculando melhor rota...");
        IResultadoRota resultado = servicoRotas.calcular(modalidade, p, q, restricao);

        System.out.println("\n========================================");
        System.out.println("             RESULTADO");
        System.out.println("========================================");
        System.out.println(resultado.toString());
    }

    private void compararModalidades() {
        System.out.println("\n--- Comparação de Modalidades ---");

        if (grafoPedestre == null || grafoCiclista == null || grafoComposto == null) {
            System.out.println("Os grafos das modalidades precisam estar carregados para comparar.");
            return;
        }

        ILocal p = obterLocalPorEntrada("Informe o local de ORIGEM (P): ", grafoPedestre);
        if (p == null) return;

        ILocal q = obterLocalPorEntrada("Informe o local de DESTINO (Q): ", grafoPedestre);
        if (q == null) return;

        float limite = obterLimiteTempo();
        RestricaoTempo restricao = new RestricaoTempo(limite);

        System.out.println("\nProcessando alternativas de deslocamento...");
        ILista<IResultadoRota> alternativas = servicoRotas.compararModalidades(p, q, restricao);

        System.out.println("\n========================================");
        System.out.println("     MODALIDADES VIÁVEIS (ORDENADAS)");
        System.out.println("========================================");

        if (alternativas.estaVazia()) {
            System.out.println("Nenhuma modalidade é viável para o limite de tempo informado ou não há conexão.");
            return;
        }

        int posicao = 1;
        for (IResultadoRota rota : alternativas) {
            System.out.printf("\n[%dª OPÇÃO] %s - Tempo: %.1f min\n", posicao++, rota.getModalidade(), rota.getTempoTotal());
            System.out.println(rota.toString());
        }
    }

    private void adicionarLocal() {
        System.out.println("\n--- Adicionar Novo Local ---");
        System.out.print("Nome do local: ");
        String nome = entrada.nextLine().trim();

        System.out.print("Latitude (ex: -20.264): ");
        double lat = lerDouble();

        System.out.print("Longitude (ex: -40.287): ");
        double lon = lerDouble();

        System.out.print("Conectar à interseção viária mais próxima a pé? (S/N): ");
        String resp = entrada.nextLine().trim();
        boolean ligar = resp.equalsIgnoreCase("S");

        ILocal novo = servicoMapa.adicionarLocal(nome, lat, lon, ligar);
        System.out.printf("\nLocal cadastrado com sucesso! ID gerado: %s (%s)\n", novo.getId(), novo.getNome());
    }

    private void adicionarTrecho() {
        System.out.println("\n--- Adicionar Trecho Viário ---");
        System.out.print("ID do local de origem: ");
        String idOrigem = entrada.nextLine().trim();

        System.out.print("ID do local de destino: ");
        String idDestino = entrada.nextLine().trim();

        System.out.print("Distância em metros: ");
        double distancia = lerDouble();

        System.out.print("Nome da via (rua/avenida): ");
        String nomeVia = entrada.nextLine().trim();

        System.out.print("Permite pedestre? (S/N): ");
        boolean pe = entrada.nextLine().trim().equalsIgnoreCase("S");

        System.out.println("Sentido para bicicleta:");
        System.out.println("1 - Ambos (mão dupla)");
        System.out.println("2 - Apenas ida (mão única)");
        System.out.println("3 - Não permitido");
        System.out.print("Opção: ");
        String opcBici = entrada.nextLine().trim();

        SentidoBicicleta bici = SentidoBicicleta.NENHUM;
        if ("1".equals(opcBici)) bici = SentidoBicicleta.AMBOS;
        else if ("2".equals(opcBici)) bici = SentidoBicicleta.IDA;

        System.out.print("Possui ciclovia? (S/N): ");
        boolean ciclovia = entrada.nextLine().trim().equalsIgnoreCase("S");

        try {
            servicoMapa.adicionarTrecho(idOrigem, idDestino, distancia, nomeVia, pe, bici, ciclovia);
            System.out.println("\nTrecho viário cadastrado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar trecho: " + e.getMessage());
        }
    }

    private void adicionarTrechoOnibus() {
        System.out.println("\n--- Adicionar Trecho de Ônibus ---");
        System.out.print("Código da linha (ex: 0160-1): ");
        String codigoLinha = entrada.nextLine().trim();

        System.out.print("ID da parada de origem (ex: P0001): ");
        String idOrigem = entrada.nextLine().trim();

        System.out.print("ID da parada de destino (ex: P0002): ");
        String idDestino = entrada.nextLine().trim();

        System.out.print("Distância entre as paradas em metros: ");
        double distancia = lerDouble();

        try {
            servicoMapa.adicionarTrechoOnibus(codigoLinha, idOrigem, idDestino, distancia);
            System.out.println("\nTrecho de ônibus adicionado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar trecho de ônibus: " + e.getMessage());
        }
    }

    // ==========================================
    // MÉTODOS DE APOIO / ENTRADA DE DADOS
    // ==========================================

    private IGrafo<ILocal> selecionarGrafo() {
        System.out.println("Selecione o grafo desejado:");
        System.out.println("1 - Grafo Pedestre");
        System.out.println("2 - Grafo Ciclista");
        System.out.println("3 - Grafo Ônibus");
        System.out.println("4 - Grafo Composto Multimodal");
        System.out.print("Opção: ");

        String opc = entrada.nextLine().trim();
        switch (opc) {
            case "1": return grafoPedestre;
            case "2": return grafoCiclista;
            case "3": return grafoOnibus;
            case "4": return grafoComposto;
            default:
                System.out.println("Opção inválida.");
                return null;
        }
    }

    private IGrafo<ILocal> selecionarGrafoPorModalidade(Modalidade modalidade) {
        switch (modalidade) {
            case A_PE: return grafoPedestre;
            case BICICLETA: return grafoCiclista;
            case A_PE_ONIBUS: return grafoComposto;
            default: return null;
        }
    }

    private ILocal obterLocalPorEntrada(String prompt, IGrafo<ILocal> grafo) {
        System.out.print(prompt);
        String texto = entrada.nextLine().trim();

        if (texto.isEmpty()) {
            System.out.println("Entrada em branco.");
            return null;
        }

        // Tenta buscar no mapa se disponível
        if (mapa != null) {
            ILocal porId = mapa.buscarPorId(texto);
            if (porId != null) return porId;

            ILista<ILocal> porNome = mapa.buscarPorNome(texto);
            if (porNome != null && !porNome.estaVazia()) {
                return porNome.iterator().next();
            }
        }

        // Busca nos vértices do grafo
        if (grafo != null) {
            for (Vertice<ILocal> v : grafo.getVertices()) {
                ILocal loc = v.getValor();
                if (loc.getId().equalsIgnoreCase(texto) || loc.getNome().toLowerCase().contains(texto.toLowerCase())) {
                    return loc;
                }
            }
        }

        System.out.println("Local \"" + texto + "\" não foi localizado no mapa.");
        return null;
    }

    private float obterLimiteTempo() {
        System.out.print("Informe o limite de tempo em minutos (ou Enter para sem limite): ");
        String texto = entrada.nextLine().trim();

        if (texto.isEmpty()) {
            return Float.POSITIVE_INFINITY;
        }

        try {
            float val = Float.parseFloat(texto);
            return (val < 0) ? Float.POSITIVE_INFINITY : val;
        } catch (NumberFormatException e) {
            System.out.println("Valor não numérico. Nenhum limite de tempo será aplicado.");
            return Float.POSITIVE_INFINITY;
        }
    }

    private double lerDouble() {
        try {
            return Double.parseDouble(entrada.nextLine().trim());
        } catch (Exception e) {
            return 0.0;
        }
    }
}
