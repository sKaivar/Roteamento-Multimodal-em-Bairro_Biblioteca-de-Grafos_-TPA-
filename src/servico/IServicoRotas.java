package servico;

import dominio.ILocal;
import dominio.IResultadoRota;
import dominio.Modalidade;
import estruturas.ILista;

/**
 * Fachada que a camada de apresentação usa para o processamento principal.
 *
 * <p>Fluxo de {@link #calcular}: (1) escolhe o grafo da modalidade (pedestre, ciclista ou
 * composto); (2) {@code existeCaminho} por largura — se falso, resultado sem rota;
 * (3) {@code menorCaminho} (Dijkstra); (4) percorre as arestas do caminho somando os tempos
 * por {@link dominio.MeioTransporte} e montando o guia; (5) compara com o limite.
 */
public interface IServicoRotas {

    /** @return o resultado de uma modalidade entre {@code p} e {@code q}. */
    IResultadoRota calcular(Modalidade modalidade, ILocal p, ILocal q, IRestricaoTempo restricao);

    /**
     * Executa as três modalidades e devolve SOMENTE as viáveis, ordenadas por tempo total
     * (inserção ordenada na lista própria, ou ordenação por {@code compareTo}).
     */
    ILista<IResultadoRota> compararModalidades(ILocal p, ILocal q, IRestricaoTempo restricao);
}
