package persistencia;

import dominio.IMapa;
import java.io.IOException;

/**
 * Leitura do arquivo textual do bairro (formato documentado no cabeçalho de bairro.txt).
 *
 * <p>Implementação esperada: {@code RepositorioMapaArquivo}, lendo linha a linha com
 * {@code BufferedReader} (E/S não é coleção: permitido), ignorando linhas vazias e
 * iniciadas por '#', trocando de seção ao encontrar "[VERTICES]", "[TRECHOS]", "[LINHAS]" e
 * "[TRECHOS_ONIBUS]", e separando campos por ';'. Ordem de leitura garantida pelo arquivo:
 * locais antes de trechos; linhas antes de trechos de ônibus.
 */
public interface IRepositorioMapa {

    /**
     * @param caminho caminho do arquivo
     * @return o mapa completo
     * @throws IOException             falha de leitura
     * @throws FormatoInvalidoException linha malformada (informe o número da linha!)
     */
    IMapa carregar(String caminho) throws IOException, FormatoInvalidoException;

    /** Erro de formato do arquivo, com o número da linha problemática. */
    class FormatoInvalidoException extends Exception {
        private static final long serialVersionUID = 1L;

        public FormatoInvalidoException(int linha, String motivo) {
            super("Linha " + linha + ": " + motivo);
        }
    }
}
