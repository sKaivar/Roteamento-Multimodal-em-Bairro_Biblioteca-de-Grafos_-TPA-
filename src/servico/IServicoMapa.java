package servico;

import dominio.ILocal;
import dominio.SentidoBicicleta;

/**
 * Acréscimos ao bairro DEPOIS da leitura do arquivo (exigência da Etapa 3: "permitir que
 * sejam acrescentados novos nós e arestas").
 *
 * <p>Cada operação atualiza o {@link dominio.IMapa} e insere diretamente as arestas nos
 * grafos pedestre/ciclista/ônibus já construídos; o grafo composto é marcado como
 * desatualizado e reconstruído na próxima consulta da modalidade II.
 */
public interface IServicoMapa {

    /**
     * Cria um local e, se {@code ligarAoMaisProximo}, liga-o por trecho de acesso a pé à
     * interseção mais próxima (premissa da especificação).
     *
     * @return o local criado
     */
    ILocal adicionarLocal(String nome, double latitude, double longitude, boolean ligarAoMaisProximo);

    /** Acrescenta um trecho viário entre dois locais existentes (ids). */
    void adicionarTrecho(String idOrigem, String idDestino, double metros, String nomeVia,
                         boolean pedestre, SentidoBicicleta bici, boolean ciclovia);

    /** Acrescenta um trecho de ônibus a uma linha existente ou nova. */
    void adicionarTrechoOnibus(String codigoLinha, String idParadaOrigem, String idParadaDestino,
                               double metros);
}
