package grafo;

public final class Aresta<T> implements IAresta<T> {

    private final Vertice<T> origem;
    private final Vertice<T> destino;
    private final float peso;
    private final String rotulo;

    // Construtor aresta
    public Aresta(
            Vertice<T> origem,
            Vertice<T> destino,
            float peso,
            String rotulo
    ) {

        if (origem == null || destino == null) {
            throw new IllegalArgumentException(
                    "Os vértices da aresta não podem ser nulos."
            );
        }

        if (peso < 0 || Float.isNaN(peso) || Float.isInfinite(peso)) {
            throw new IllegalArgumentException(
                    "Peso inválido."
            );
        }

        this.origem = origem;
        this.destino = destino;
        this.peso = peso;
        this.rotulo = (rotulo == null) ? "" : rotulo;
    }

    // Retorna origem
    @Override
    public Vertice<T> getOrigem() {
        return origem;
    }

    // Retorna destino
    @Override
    public Vertice<T> getDestino() {
        return destino;
    }

    // Retorna peso
    @Override
    public float getPeso() {
        return peso;
    }

    // Retorna rotulo aresta
    @Override
    public String getRotulo() {
        return rotulo;
    }

    // Imprime aresta
    @Override
    public String toString() {

        return origem + " -> " + destino
                + " (" + peso + ")"
                + (rotulo.isEmpty() ? "" : " [" + rotulo + "]");
    }
}