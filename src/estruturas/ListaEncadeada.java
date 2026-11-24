package estruturas;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class ListaEncadeada<T> implements ILista<T> {

    private NoLista<T> prim;
    private NoLista<T> ult;
    private int quant;

    //Construtor
    public ListaEncadeada() {
        prim = null;
        ult = null;
        quant = 0;
    }

    //Adiciona elemento ao final da lista
    @Override
    public boolean adicionar(T elemento) {

        Objects.requireNonNull(elemento);//Checa se elemento tentado adicionar existe

        NoLista<T> novoNo = new NoLista<>(elemento);

        if (prim == null) {
            prim = ult = novoNo;
        } else {
            ult.setProx(novoNo);
            ult = novoNo;
        }

        quant++;

        return true;
    }

    //Adicionar um elemento ao final da lista
    @Override
    public void adicionarNoInicio(T elemento) {

        Objects.requireNonNull(elemento);//Checa se elemento tentado adicionar existe

        NoLista<T> novoNo = new NoLista<>(elemento);

        if (prim == null) {
            prim = ult = novoNo;
        } else {
            novoNo.setProx(prim);
            prim = novoNo;
        }

        quant++;
    }

    //Remove o primeiro elemento da lista, e retorna o mesmo
    @Override
    public T removerPrimeiro() {

        if (prim == null) {
            return null;
        }

        T valor = prim.getValor();

        prim = prim.getProx();

        if (prim == null) {
            ult = null;
        }

        quant--;

        return valor;
    }

    //Tamanho total da lista
    @Override
    public int tamanho() {
        return quant;
    }

    //Checa se esta vazia
    @Override
    public boolean estaVazia() {
        return quant == 0;
    }

    //Retorna ultimo elemento da lista
    public T ultimoValor() {
        return (ult == null) ? null : ult.getValor();
    }

    //Percorre por elementos da lista
    @Override
    public Iterator<T> iterator() {

        return new Iterator<T>() {

            private NoLista<T> atual = prim;

            @Override
            public boolean hasNext() {
                return atual != null;
            }

            @Override
            public T next() {

                if (atual == null) {
                    throw new NoSuchElementException();
                }

                T valor = atual.getValor();

                atual = atual.getProx();

                return valor;
            }
        };
    }

    //Metodo de impressao da lista
    @Override
    public String toString() {

        NoLista<T> atual = prim;

        StringBuilder s = new StringBuilder("[");

        while (atual != null) {

            s.append(atual.getValor());

            if (atual.getProx() != null) {
                s.append(", ");
            }

            atual = atual.getProx();
        }

        return s.append("]").toString();
    }
}