package apresentacao;

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

    /**
     * Construtor do menu.
     *
     * @param caminhoArquivo caminho do arquivo de dados
     */
    public MenuConsole(String caminhoArquivo) {

        this.caminhoArquivo = caminhoArquivo;
        this.entrada = new Scanner(System.in);
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
        System.out.println("Arquivo: " + caminhoArquivo);

        /*
         * IMPLEMENTAÇÃO FUTURA:
         *
         * 1. Utilizar RepositorioMapaArquivo
         *    para interpretar o arquivo TXT.
         *
         * 2. Criar o objeto Mapa com os dados carregados.
         *
         * 3. Utilizar ConstrutorGrafos para construir:
         *      - Grafo de pedestres;
         *      - Grafo de ciclistas;
         *      - Grafo de ônibus;
         *      - Grafo composto.
         *
         * 4. Armazenar o mapa e os grafos para
         *    utilização nas opções do menu.
         */

        System.out.println(
                "Leitura do arquivo e construção dos grafos ainda não implementadas."
        );
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

                opcao = Integer.parseInt(entrada.nextLine());

                processarOpcao(opcao);

            } catch (NumberFormatException e) {

                opcao = -1;

                System.out.println("Digite uma opção numérica.");

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
     *
     * @param opcao opção escolhida pelo usuário
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

        // TODO: Exibir quantidade de vértices e arestas.
        System.out.println("Informações do mapa.");
    }

    private void executarBuscaLargura() {

        // TODO: Solicitar origem e executar BFS.
        System.out.println("Busca em largura.");
    }

    private void calcularRota() {

        // TODO: Solicitar origem, destino e modalidade.
        System.out.println("Cálculo de rota.");
    }

    private void compararModalidades() {

        // TODO: Calcular e comparar as três modalidades.
        System.out.println("Comparação de modalidades.");
    }

    private void adicionarLocal() {

        // TODO: Solicitar dados e adicionar novo local.
        System.out.println("Adição de novo local.");
    }

    private void adicionarTrecho() {

        // TODO: Solicitar dados e adicionar novo trecho.
        System.out.println("Adição de novo trecho.");
    }

    private void adicionarTrechoOnibus() {

        // TODO: Solicitar dados e adicionar trecho de ônibus.
        System.out.println("Adição de trecho de ônibus.");
    }
}