package grafo;

/**
 * Aresta dirigida (origem → destino) com peso. Em grafo não dirigido, o {@code Grafo}
 * cria duas instâncias, uma em cada sentido.
 *
 * <p>Implementação esperada: classe {@code Aresta<T>} imutável, com os quatro campos abaixo.
 *
 * <p>O RÓTULO é texto livre, opaco para a biblioteca, que o aplicativo usa no guia da
 * trajetória — por exemplo, o nome da rua ("Rua Carlos Martins") ou o código da linha
 * ("0160-1"). A biblioteca não interpreta o rótulo: continua genérica.
 *
 * @param <T> tipo armazenado nos vértices
 */
public interface IAresta<T> {

    /** @return vértice de partida. */
    Vertice<T> getOrigem();

    /** @return vértice de chegada. */
    Vertice<T> getDestino();

    /**
     * @return peso da aresta. No aplicativo, SEMPRE tempo em minutos (≥ 0) — a distância
     *         é atributo do trecho e é convertida antes de criar a aresta.
     */
    float getPeso();

    /** @return rótulo para exibição (nome da via, código da linha…), ou {@code ""}. */
    String getRotulo();
}
