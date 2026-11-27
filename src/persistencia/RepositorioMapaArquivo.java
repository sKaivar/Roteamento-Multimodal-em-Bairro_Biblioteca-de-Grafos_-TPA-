package persistencia;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import dominio.ILinhaOnibus;
import dominio.ILocal;
import dominio.IMapa;
import dominio.LinhaOnibus;
import dominio.Local;
import dominio.Mapa;
import dominio.SentidoBicicleta;
import dominio.TipoLocal;
import dominio.Trecho;
import dominio.TrechoOnibus;

/**
 * Lê o arquivo textual do bairro (dados/bairro.txt) e devolve um Mapa preenchido.
 * Linhas vazias e iniciadas por '#' são ignoradas; "[SEÇÃO]" troca a seção corrente;
 * os campos são separados por ';'.
 */
public class RepositorioMapaArquivo implements IRepositorioMapa {

    @Override
    public IMapa carregar(String caminho) throws IOException, FormatoInvalidoException {
        Mapa mapa = new Mapa();
        String secao = null;   // seção corrente (máquina de estados)
        int n = 0;             // número da linha, para as mensagens de erro

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(caminho), StandardCharsets.UTF_8))) {

            String linha;
            while ((linha = br.readLine()) != null) {
                n++;
                linha = linha.trim();
                if (linha.isEmpty() || linha.startsWith("#")) continue;
                if (linha.startsWith("[")) { secao = linha; continue; } // troca de seção
                if (secao == null) throw new FormatoInvalidoException(n, "dado antes de qualquer seção");

                String[] c = linha.split(";");
                try {
                    switch (secao) {
                        case "[VERTICES]" -> mapa.adicionarLocal(new Local(
                                c[0], c[1], Double.parseDouble(c[2]), Double.parseDouble(c[3]),
                                TipoLocal.valueOf(c[4])));

                        case "[TRECHOS]" -> mapa.adicionarTrecho(new Trecho(
                                exigirLocal(mapa, c[0], n), exigirLocal(mapa, c[1], n),
                                Double.parseDouble(c[2]), c[3],
                                c[4].equals("S"), SentidoBicicleta.valueOf(c[5]), c[6].equals("S")));

                        case "[LINHAS]" -> mapa.adicionarLinha(new LinhaOnibus(c[0], c[1]));

                        case "[TRECHOS_ONIBUS]" -> {
                            ILinhaOnibus l = exigirLinha(mapa, c[0], n);
                            l.getTrechos().adicionar(new TrechoOnibus(
                                    l, exigirLocal(mapa, c[1], n), exigirLocal(mapa, c[2], n),
                                    Double.parseDouble(c[3])));
                        }

                        default -> throw new FormatoInvalidoException(n, "seção desconhecida: " + secao);
                    }
                } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException e) {
                    // faltam campos, número malformado ou tipo/sentido inexistente no enum
                    throw new FormatoInvalidoException(n, "campos inválidos: " + linha);
                }
            }
        }
        return mapa;
    }

    /** Devolve o local com o id dado, ou acusa erro na linha n. */
    private ILocal exigirLocal(IMapa mapa, String id, int n) throws FormatoInvalidoException {
        ILocal local = mapa.buscarPorId(id);
        if (local == null) throw new FormatoInvalidoException(n, "local inexistente: " + id);
        return local;
    }

    /** Devolve a linha de ônibus com o código dado, ou acusa erro na linha n. */
    private ILinhaOnibus exigirLinha(IMapa mapa, String codigo, int n) throws FormatoInvalidoException {
        for (ILinhaOnibus l : mapa.getLinhas()) {
            if (l.getCodigo().equals(codigo)) return l;
        }
        throw new FormatoInvalidoException(n, "linha inexistente: " + codigo);
    }
}