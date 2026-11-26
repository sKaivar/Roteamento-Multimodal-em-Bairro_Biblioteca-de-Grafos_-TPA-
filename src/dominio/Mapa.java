package dominio;

public abstract class Mapa implements IMapa {
    private final String nome;
    private final String descricao;

    public Mapa(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }
    
}
