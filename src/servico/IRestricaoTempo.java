package servico;

import dominio.Modalidade;

/**
 * Tempo máximo aceitável, global ou por modalidade. Implementação esperada:
 * {@code RestricaoTempo} com um limite global e sobrescritas opcionais por modalidade.
 */
public interface IRestricaoTempo {

    /** @return limite em minutos para a modalidade ({@code Float.POSITIVE_INFINITY} = sem limite). */
    float limitePara(Modalidade modalidade);
}
