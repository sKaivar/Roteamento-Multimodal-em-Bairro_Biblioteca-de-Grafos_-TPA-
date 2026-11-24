package estruturas;

class NoLista<T> {

    private T valor;
    private NoLista<T> prox;

    //Cria novo no
    NoLista(T valor) {
        this.valor = valor;
        this.prox = null;
    }
    public T getValor() {
        return valor;
    }

    public void setValor(T valor) {
        this.valor = valor;
    }

    public NoLista<T> getProx() {
        return prox;
    }

    public void setProx(NoLista<T> prox) {
        this.prox = prox;
    }
}