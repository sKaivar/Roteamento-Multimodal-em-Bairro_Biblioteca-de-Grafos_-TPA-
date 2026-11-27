package apresentacao;

import dominio.ILocal;
import dominio.IMapa;
import dominio.IResultadoRota;
import dominio.Modalidade;
import dominio.SentidoBicicleta;
import estruturas.ILista;
import estruturas.ListaEncadeada;
import grafo.IGrafo;
import grafo.Vertice;
import persistencia.RepositorioMapaArquivo;
import servico.IConstrutorGrafos;
import servico.IModeloCusto;
import servico.IServicoMapa;
import servico.IServicoRotas;
import servico.RestricaoTempo;
import servico.ServicoMapa;
import servico.ServicoRotas;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Scanner;

/**
 * Interface de linha de comando (camada de apresentação) que permite ao usuário
 * consultar informações dos grafos, executar caminhamento em largura, calcular e
 * comparar rotas multimodais e acrescentar novos locais e trechos após a carga do arquivo.
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
     * Cria o menu console associado ao arquivo de dados informado.
     *
     * @param caminhoArquivo caminho do arquivo de entrada do bairro (ex.: "dados/bairro.txt")
     */
    public MenuConsole(String caminhoArquivo) {
        this.caminhoArquivo = caminhoArquivo;
        this.entrada = new Scanner(System.in);
        this.servicoMapa = new ServicoMapa();
        this.servicoRotas = new ServicoRotas();
    }

    /**
     * Carrega o mapa a partir do arquivo configurado e inicia o laço interativo do menu.
     */
    public void iniciar() {
        System.out.println("========================================");
        System.out.println("       SISTEMA DE ROTAS - JARDIM CAMBURI");
        System.out.println("========================================");

        carregarMapa();

        executarMenu();
    }

    /**
     * Configura os grafos e serviços externamente (sem construtor de grafos nem modelo de custo).
     *
     * @param mapa mapa carregado
     * @param pedestre grafo de pedestres
     * @param ciclista grafo de ciclistas
     * @param onibus grafo da rede de ônibus
     * @param composto grafo composto multimodal
     */
    public void configurarServicos(
            IMapa mapa,
            IGrafo<ILocal> pedestre,
            IGrafo<ILocal> ciclista,
            IGrafo<ILocal> onibus,
            IGrafo<ILocal> composto
    ) {
        configurarServicos(mapa, null, null, pedestre, ciclista, onibus, composto);
    }

    /**
     * Configura o mapa, os grafos e os serviços de custo/construção usados para
     * cálculos de rota e reconstrução do grafo composto após edições.
     *
     * @param mapa mapa carregado
     * @param construtorGrafos construtor de grafos para reconstrução do composto
     * @param modeloCusto modelo de conversão de distância em tempo
     * @param pedestre grafo de pedestres
     * @param ciclista grafo de ciclistas
     * @param onibus grafo da rede de ônibus
     * @param composto grafo composto multimodal
     */
    public void configurarServicos(
            IMapa mapa,
            IConstrutorGrafos construtorGrafos,
            IModeloCusto modeloCusto,
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

        ServicoMapa mapaService = new ServicoMapa(
                mapa,
                construtorGrafos,
                modeloCusto,
                pedestre,
                ciclista,
                onibus,
                composto
        );
        this.servicoMapa = mapaService;
        this.servicoRotas = new ServicoRotas(pedestre, ciclista, composto, mapaService);
    }

    // Lê o arquivo de dados do mapa
    private void carregarMapa() {
        System.out.println("\nCarregando dados do mapa...");
        System.out.println("Arquivo de entrada: " + caminhoArquivo);

        try {
            this.mapa = new RepositorioMapaArquivo().carregar(caminhoArquivo);
            if (servicoMapa instanceof ServicoMapa) {
                ((ServicoMapa) servicoMapa).setMapa(this.mapa);
            }
            System.out.println("Mapa lido com sucesso: " + mapa.getLocais().tamanho() + " locais.");
        } catch (Exception e) {
            System.out.println("Aviso: " + e.getMessage());
        }
    }

    // Laço principal que lê as escolhas do usuário
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

    // Mostra as opções disponíveis
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

    // Trata cada opção escolhida
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

    // Sincroniza o grafo composto quando houver acréscimos recentes no mapa
    private void sincronizarGrafoComposto() {
        if (servicoMapa instanceof ServicoMapa) {
            ServicoMapa mapaService = (ServicoMapa) servicoMapa;
            if (mapaService.isGrafoCompostoDesatualizado()) {
                boolean reconstruiu = mapaService.reconstruirCompostoSeNecessario();
                if (reconstruiu && mapaService.getGrafoComposto() != null) {
                    this.grafoComposto = mapaService.getGrafoComposto();
                    if (servicoRotas instanceof ServicoRotas) {
                        ((ServicoRotas) servicoRotas).setGrafoComposto(this.grafoComposto);
                    }
                }
            }
        }
    }

    // Opção 1: mostra quantidade de vértices e arestas
    private void exibirInformacoesMapa() {
        sincronizarGrafoComposto();

        System.out.println("\n--- Informações do Mapa ---");
        if (grafoPedestre == null && grafoCiclista == null && grafoOnibus == null && grafoComposto == null) {
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
        if (servicoMapa instanceof ServicoMapa && ((ServicoMapa) servicoMapa).isGrafoCompostoDesatualizado()) {
            System.out.println("Aviso: o grafo composto aguarda construtor de grafos para ser reconstruído.");
        }
    }

    // Opção 2: roda busca em largura a partir de um ponto
    private void executarBuscaLargura() {
        sincronizarGrafoComposto();

        System.out.println("\n--- Busca em Largura (BFS) ---");
        IGrafo<ILocal> grafoEscolhido = selecionarGrafo();

        if (grafoEscolhido == null) {
            System.out.println("Grafo indisponível.");
            return;
        }

        ILocal origem = obterLocalPorEntrada("Digite o ID ou nome do local de partida: ", grafoEscolhido);
        if (origem == null) {
            return;
        }

        System.out.println("\nExecutando caminhamento em largura a partir de: " + origem.getNome());
        String resultado = grafoEscolhido.caminharEmLargura(origem);
        System.out.println(resultado);
    }

    // Opção 3: calcula trajeto de uma modalidade específica
    private void calcularRota() {
        sincronizarGrafoComposto();

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
        sincronizarGrafoComposto();

        System.out.println("\n========================================");
        System.out.println("             RESULTADO");
        System.out.println("========================================");
        System.out.println(resultado.toString());
    }

    // Opção 4: compara a pé, bicicleta e ônibus e ordena pelo menor tempo
    private void compararModalidades() {
        sincronizarGrafoComposto();

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
        sincronizarGrafoComposto();

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

    // Opção 5: cadastra um local novo
    private void adicionarLocal() {
        System.out.println("\n--- Adicionar Novo Local ---");
        System.out.print("Nome do local: ");
        String nome = entrada.nextLine().trim();

        double lat = lerDouble("Latitude (ex: -20.264): ");
        double lon = lerDouble("Longitude (ex: -40.287): ");

        System.out.print("Conectar à interseção viária mais próxima a pé? (S/N): ");
        String resp = entrada.nextLine().trim();
        boolean ligar = resp.equalsIgnoreCase("S");

        try {
            ILocal novo = servicoMapa.adicionarLocal(nome, lat, lon, ligar);
            System.out.printf("\nLocal cadastrado com sucesso! ID gerado: %s (%s)\n", novo.getId(), novo.getNome());
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar local: " + e.getMessage());
        }
    }

    // Opção 6: cadastra um trecho de rua
    private void adicionarTrecho() {
        System.out.println("\n--- Adicionar Trecho Viário ---");
        System.out.print("ID do local de origem: ");
        String idOrigem = entrada.nextLine().trim();

        System.out.print("ID do local de destino: ");
        String idDestino = entrada.nextLine().trim();

        double distancia = lerDoubleNaoNegativo("Distância em metros: ");

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

    // Opção 7: cadastra trecho de linha de ônibus
    private void adicionarTrechoOnibus() {
        System.out.println("\n--- Adicionar Trecho de Ônibus ---");
        System.out.print("Código da linha (ex: 0160-1): ");
        String codigoLinha = entrada.nextLine().trim();

        System.out.print("ID da parada de origem (ex: P0001): ");
        String idOrigem = entrada.nextLine().trim();

        System.out.print("ID da parada de destino (ex: P0002): ");
        String idDestino = entrada.nextLine().trim();

        double distancia = lerDoubleNaoNegativo("Distância entre as paradas em metros: ");

        try {
            servicoMapa.adicionarTrechoOnibus(codigoLinha, idOrigem, idDestino, distancia);
            System.out.println("\nTrecho de ônibus adicionado com sucesso!");
        } catch (Exception e) {
            System.out.println("Erro ao cadastrar trecho de ônibus: " + e.getMessage());
        }
    }

    // Seleciona um dos quatro grafos
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

    // Pega o grafo correspondente à modalidade
    private IGrafo<ILocal> selecionarGrafoPorModalidade(Modalidade modalidade) {
        switch (modalidade) {
            case A_PE: return grafoPedestre;
            case BICICLETA: return grafoCiclista;
            case A_PE_ONIBUS: return grafoComposto;
            default: return null;
        }
    }

    // Busca local por ID exato (sem diferenciar maiúsculas) ou por trecho do nome com desambiguação
    private ILocal obterLocalPorEntrada(String prompt, IGrafo<ILocal> grafo) {
        System.out.print(prompt);
        String texto = entrada.nextLine().trim();

        if (texto.isEmpty()) {
            System.out.println("Entrada em branco.");
            return null;
        }

        // 1. Tenta busca exata por ID primeiro
        ILocal porId = buscarPorIdExato(texto, grafo);
        if (porId != null) {
            System.out.println("Selecionado: " + porId.getNome() + " [" + porId.getId() + "]");
            return porId;
        }

        // 2. Coleta todos os locais cujo nome contenha o termo buscado
        ILista<ILocal> candidatos = buscarCandidatosPorNome(texto, grafo);

        if (candidatos.estaVazia()) {
            System.out.println("Local \"" + texto + "\" não foi localizado neste grafo.");
            return null;
        }

        if (candidatos.tamanho() == 1) {
            ILocal unico = candidatos.iterator().next();
            System.out.println("Selecionado: " + unico.getNome() + " [" + unico.getId() + "]");
            return unico;
        }

        // 3. Havendo mais de um candidato, lista para o usuário escolher
        return escolherEntreCandidatos(candidatos);
    }

    // Busca exata por ID no grafo (se informado) e no mapa
    private ILocal buscarPorIdExato(String id, IGrafo<ILocal> grafo) {
        if (grafo != null) {
            for (Vertice<ILocal> v : grafo.getVertices()) {
                ILocal loc = v.getValor();
                if (loc.getId() != null && loc.getId().equalsIgnoreCase(id)) {
                    return loc;
                }
            }
            return null;
        }

        if (mapa != null) {
            for (ILocal loc : mapa.getLocais()) {
                if (loc.getId() != null && loc.getId().equalsIgnoreCase(id)) {
                    return loc;
                }
            }
        }
        return null;
    }

    // Coleta candidatos por nome usando a lista própria do projeto
    private ILista<ILocal> buscarCandidatosPorNome(String termo, IGrafo<ILocal> grafo) {
        ILista<ILocal> candidatos = new ListaEncadeada<>();

        if (mapa != null) {
            ILista<ILocal> encontradosNoMapa = mapa.buscarPorNome(termo);
            for (ILocal loc : encontradosNoMapa) {
                if (grafo == null || existeNoGrafo(grafo, loc)) {
                    candidatos.adicionar(loc);
                }
            }
        }

        // Se o mapa não estiver carregado ou o grafo tiver vértices próprios (ex.: grafo de ônibus)
        if (candidatos.estaVazia() && grafo != null) {
            String termoNorm = normalizarTexto(termo);
            for (Vertice<ILocal> v : grafo.getVertices()) {
                ILocal loc = v.getValor();
                if (loc.getNome() != null && normalizarTexto(loc.getNome()).contains(termoNorm)) {
                    candidatos.adicionar(loc);
                }
            }
        }

        return candidatos;
    }

    // Exibe os locais encontrados numerados e retorna o escolhido
    private ILocal escolherEntreCandidatos(ILista<ILocal> candidatos) {
        System.out.println("\nForam encontrados " + candidatos.tamanho() + " locais correspondentes:");

        int indice = 1;
        for (ILocal loc : candidatos) {
            System.out.printf("  %d - %s [%s] (%s)\n", indice++, loc.getNome(), loc.getId(), loc.getTipo());
        }
        System.out.println("  0 - Cancelar");

        while (true) {
            System.out.print("Escolha o número do local desejado: ");
            String linha = entrada.nextLine().trim();

            try {
                int escolha = Integer.parseInt(linha);
                if (escolha == 0) {
                    System.out.println("Seleção cancelada.");
                    return null;
                }
                if (escolha >= 1 && escolha <= candidatos.tamanho()) {
                    ILocal selecionado = obterPorPosicao(candidatos, escolha - 1);
                    if (selecionado != null) {
                        System.out.println("Selecionado: " + selecionado.getNome() + " [" + selecionado.getId() + "]");
                    }
                    return selecionado;
                }
            } catch (NumberFormatException ignored) {
                // Repete a solicitação ao usuário
            }

            System.out.println("Opção inválida. Digite um número entre 0 e " + candidatos.tamanho() + ".");
        }
    }

    // Percorre a lista própria até a posição desejada (sem usar coleções do Java)
    private ILocal obterPorPosicao(ILista<ILocal> lista, int posicaoAlvo) {
        int atual = 0;
        for (ILocal item : lista) {
            if (atual == posicaoAlvo) {
                return item;
            }
            atual++;
        }
        return null;
    }

    // Verifica se um local está presente no grafo informado
    private boolean existeNoGrafo(IGrafo<ILocal> grafo, ILocal alvo) {
        if (grafo.obterVertice(alvo) != null) {
            return true;
        }
        String idAlvo = alvo.getId();
        if (idAlvo == null) {
            return false;
        }
        for (Vertice<ILocal> v : grafo.getVertices()) {
            if (idAlvo.equalsIgnoreCase(v.getValor().getId())) {
                return true;
            }
        }
        return false;
    }

    // Remove acentos e coloca em minúsculas para comparação textual simples
    private String normalizarTexto(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }

    // Lê o limite de tempo digitado
    private float obterLimiteTempo() {
        System.out.print("Informe o limite de tempo em minutos (ou Enter para sem limite): ");
        String texto = entrada.nextLine().trim().replace(',', '.');

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

    // Lê número decimal válido (aceita ponto ou vírgula)
    private double lerDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String texto = entrada.nextLine().trim().replace(',', '.');
            try {
                double valor = Double.parseDouble(texto);
                if (!Double.isNaN(valor) && !Double.isInfinite(valor)) {
                    return valor;
                }
            } catch (NumberFormatException ignored) {
                // Repete a leitura
            }
            System.out.println("Valor inválido. Digite um número válido.");
        }
    }

    // Lê número decimal não negativo (para distâncias)
    private double lerDoubleNaoNegativo(String prompt) {
        while (true) {
            double valor = lerDouble(prompt);
            if (valor >= 0) {
                return valor;
            }
            System.out.println("O valor não pode ser negativo.");
        }
    }
}
