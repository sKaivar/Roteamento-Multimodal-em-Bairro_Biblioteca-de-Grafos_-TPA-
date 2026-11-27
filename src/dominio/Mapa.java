package dominio;

import estruturas.ILista;
import estruturas.ListaEncadeada;

import java.text.Normalizer;
import java.util.Locale;

// Apenas armazena e consulta
public class Mapa implements IMapa {

    private final ILista<ILocal> locais = new ListaEncadeada<>();
    private final ILista<ITrecho> trechos = new ListaEncadeada<>();
    private final ILista<ILinhaOnibus> linhas = new ListaEncadeada<>();

    @Override
    public ILista<ILocal> getLocais() {
        return locais;
    }

    @Override
    public ILista<ITrecho> getTrechos() {
        return trechos;
    }

    @Override
    public ILista<ILinhaOnibus> getLinhas() {
        return linhas;
    }

    @Override
    public ILocal buscarPorId(String id) {
        if (id == null) return null;
        for (ILocal local : locais) {
            if (local.getId().equals(id)) { // ponto equals lá
                return local;
            }
        }
        return null;
    }

    @Override
    public ILista<ILocal> buscarPorNome(String termo) {
        ILista<ILocal> encontrados = new ListaEncadeada<>();
        if (termo == null || termo.isBlank()) return encontrados;

        String alvo = normalizar(termo);
        for (ILocal local : locais) {
            if (normalizar(local.getNome()).contains(alvo)) {
                encontrados.adicionar(local);
            }
        }
        return encontrados;
    }

    @Override
    public void adicionarLocal(ILocal local) {
        if (local == null) throw new IllegalArgumentException("Local nulo.");
        if (buscarPorId(local.getId()) != null) {
            throw new IllegalArgumentException("Já existe local com id " + local.getId() + ".");
        }
        locais.adicionar(local);
    }

    @Override
    public void adicionarTrecho(ITrecho trecho) {
        if (trecho == null) throw new IllegalArgumentException("Trecho nulo.");
        trechos.adicionar(trecho);
    }

    @Override
    public void adicionarLinha(ILinhaOnibus linha) {
        if (linha == null) throw new IllegalArgumentException("Linha nula.");
        linhas.adicionar(linha);
    }


    //"Gelú" -> "gelu"
    private static String normalizar(String texto) {
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return semAcento.toLowerCase(Locale.ROOT).trim();
    }
}