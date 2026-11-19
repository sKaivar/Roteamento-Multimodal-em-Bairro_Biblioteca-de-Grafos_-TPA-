package dominio;

import estruturas.ILista;

/**
 * Linha de ônibus num sentido (seção [LINHAS]). O código inclui a variante de sentido:
 * "0160-1", "508-1", "508-2"…
 */
public interface ILinhaOnibus {

    String getCodigo();

    /** @return nome legível (ex.: "508 Terminal Laranjeiras → Terminal Vila Velha"). */
    String getNome();

    /** @return trechos da linha na ordem de percurso (preenchidos pelo carregador). */
    ILista<ITrechoOnibus> getTrechos();
}
