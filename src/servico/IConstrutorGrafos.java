package servico;

import dominio.ILocal;
import dominio.IMapa;
import grafo.IGrafo;

/**
 * Converte o {@link IMapa} (metros, atributos) nos grafos ponderados por TEMPO.
 * Todos os pesos passam por {@link IModeloCusto}.
 */
public interface IConstrutorGrafos {

    /**
     * Grafo NÃO dirigido: um vértice por local; uma aresta por trecho com pe = S,
     * peso = tempoCaminhada, rótulo = nome da via.
     */
    IGrafo<ILocal> construirPedestre(IMapa mapa);

    /**
     * Grafo DIRIGIDO: para cada trecho com bici ≠ NENHUM, aresta origem→destino; e também
     * destino→origem se bici = AMBOS (ruas de mão dupla e ciclovias). Peso =
     * tempoBicicleta(distância, ciclovia).
     */
    IGrafo<ILocal> construirCiclista(IMapa mapa);

    /**
     * Grafo DIRIGIDO de ônibus: vértices PARADA_LINHA ("P0004@0160-1"), uma aresta por
     * trecho de ônibus, peso = tempoOnibus, rótulo = código da linha.
     */
    IGrafo<ILocal> construirOnibus(IMapa mapa);

    /**
     * Grafo composto em camadas (modalidade II), DIRIGIDO:
     * <ul>
     *   <li>camada A — cópia das arestas do pedestre, nos dois sentidos;</li>
     *   <li>camada B — cópia das arestas do ônibus;</li>
     *   <li>embarque A→B: de cada PARADA física para cada "parada@linha" que a serve, peso = 8;</li>
     *   <li>desembarque B→A: o inverso, peso = 0.</li>
     * </ul>
     * Um único Dijkstra sobre ele resolve a modalidade II, inclusive com baldeações.
     */
    IGrafo<ILocal> construirComposto(IGrafo<ILocal> pedestre, IGrafo<ILocal> onibus);
}
