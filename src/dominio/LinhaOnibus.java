package dominio;
import estruturas.ILista;
import estruturas.ListaEncadeada;

public class LinhaOnibus implements ILinhaOnibus {
    private final String codigo;
    private final String nome;
    private ILista<ITrechoOnibus> trechos = new ListaEncadeada<>();

    public LinhaOnibus(String codigo, String nome) {
        this.codigo = codigo;
        this.nome = nome;
    }

    @Override
    public String getCodigo() {
        return codigo;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public ILista<ITrechoOnibus> getTrechos() {
        return trechos;
    }
}
