package dominio;

import estruturas.ILista;

/**
 * Conteúdo integral do arquivo do bairro, já convertido em objetos de domínio — a fronteira
 * entre a camada de persistência (que o produz) e a de serviço (que constrói os grafos).
 * Também recebe os acréscimos feitos pelo usuário após a leitura.
 */
public interface IMapa {

    ILista<ILocal> getLocais();

    ILista<ITrecho> getTrechos();

    ILista<ILinhaOnibus> getLinhas();

    /** @return o local de id exato, ou {@code null}. */
    ILocal buscarPorId(String id);

    /**
     * Busca por trecho do nome, sem diferenciar maiúsculas/acentos — para o usuário digitar
     * "carlos martins" em vez de "I0042".
     *
     * @return locais cujo nome contém o termo (possivelmente vazia)
     */
    ILista<ILocal> buscarPorNome(String termo);

    void adicionarLocal(ILocal local);

    void adicionarTrecho(ITrecho trecho);

    void adicionarLinha(ILinhaOnibus linha);
}
