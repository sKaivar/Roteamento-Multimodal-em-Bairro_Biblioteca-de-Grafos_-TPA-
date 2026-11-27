package apresentacao;

/**
 * Ponto de entrada da aplicação de roteamento multimodal intrabairro.
 */
public class Main {

    /**
     * Inicia o sistema carregando o arquivo de dados padrão (ou o caminho
     * passado por argumento) e abrindo o menu interativo no console.
     *
     * @param args argumentos de linha de comando; {@code args[0]} opcionalmente define o arquivo de entrada
     */
    public static void main(String[] args) {

        // Caminho padrão do arquivo de dados
        String caminhoArquivo = "dados/bairro.txt";

        // Permite informar outro arquivo ao executar o programa
        if (args.length > 0) {
            caminhoArquivo = args[0];
        }

        try {

            MenuConsole menu = new MenuConsole(caminhoArquivo);

            menu.iniciar();

        } catch (Exception e) {

            System.err.println(
                    "Erro ao iniciar a aplicação: " + e.getMessage()
            );

            e.printStackTrace();
        }
    }
}
