package estruturas;

public class Fila<T> implements IFila<T> {

    private final ILista<T> elementos;

    //Cria fila vazia
    public Fila() {
        elementos = new ListaEncadeada<>();
    }

    //Adiciona elemento ao final da fila
    @Override
    public void enfileirar(T elemento) {
        elementos.adicionar(elemento);
    }

    //Remove o primeiro elemento e retorna ele
    @Override
    public T desenfileirar() {
        return elementos.removerPrimeiro();
    }

    //Verifica se a fila esta vazia
    @Override
    public boolean estaVazia() {
        return elementos.estaVazia();
    }

    //Retorna tamanho da fila
    @Override
    public int tamanho() {
        return elementos.tamanho();
    }
}