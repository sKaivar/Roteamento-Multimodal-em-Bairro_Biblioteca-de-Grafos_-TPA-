package apresentacao;

/**
 * Ponto de entrada da aplicação.
 */
public class Main {

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