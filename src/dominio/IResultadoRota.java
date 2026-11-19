package dominio;

import estruturas.ILista;

/** Resultado de UMA modalidade: as quatro saídas da especificação. */
public interface IResultadoRota extends Comparable<IResultadoRota> {

    Modalidade getModalidade();

    /** @return {@code false} se não houver caminho entre P e Q nesta modalidade. */
    boolean existeRota();

    /** Saída 1 — @return {@code true} se existe rota e tempo total ≤ limite. */
    boolean isViavel();

    /** @return limite de tempo aplicado a esta modalidade (minutos). */
    float getLimiteMinutos();

    /** Saída 2 — @return tempo total em minutos. */
    float getTempoTotal();

    /** Saída 3 — @return minutos gastos no meio indicado (0 se não usado). */
    float getTempoPor(MeioTransporte meio);

    /** Saída 4 — @return passos do guia, em ordem. */
    ILista<IPassoGuia> getGuia();

    /** Ordena por tempo total (crescente), para listar as alternativas viáveis. */
    @Override
    int compareTo(IResultadoRota outro);
}
