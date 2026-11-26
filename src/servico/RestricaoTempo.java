package servico;

import dominio.Modalidade;

/**
 * Controla os limites de tempo aceitáveis para as rotas.
 * Permite definir um limite geral e também limites específicos para cada meio de transporte.
 */
public class RestricaoTempo implements IRestricaoTempo {

    private final float limiteGlobal;
    private float limiteAPe = Float.NaN;
    private float limiteBicicleta = Float.NaN;
    private float limiteAPeOnibus = Float.NaN;

    // Construtor sem limite (tempo livre)
    public RestricaoTempo() {
        this.limiteGlobal = Float.POSITIVE_INFINITY;
    }

    // Construtor com limite geral em minutos
    public RestricaoTempo(float limiteGlobal) {
        if (limiteGlobal < 0) {
            throw new IllegalArgumentException("O limite de tempo não pode ser negativo.");
        }
        this.limiteGlobal = limiteGlobal;
    }

    // Define um limite específico para uma modalidade
    public void definirLimite(Modalidade modalidade, float limite) {
        if (modalidade == null) {
            throw new IllegalArgumentException("A modalidade não pode ser nula.");
        }
        if (limite < 0) {
            throw new IllegalArgumentException("O limite não pode ser negativo.");
        }

        switch (modalidade) {
            case A_PE:
                this.limiteAPe = limite;
                break;
            case BICICLETA:
                this.limiteBicicleta = limite;
                break;
            case A_PE_ONIBUS:
                this.limiteAPeOnibus = limite;
                break;
        }
    }

    // Retorna o limite que vale para a modalidade consultada
    @Override
    public float limitePara(Modalidade modalidade) {
        if (modalidade == null) {
            return limiteGlobal;
        }

        switch (modalidade) {
            case A_PE:
                return Float.isNaN(limiteAPe) ? limiteGlobal : limiteAPe;
            case BICICLETA:
                return Float.isNaN(limiteBicicleta) ? limiteGlobal : limiteBicicleta;
            case A_PE_ONIBUS:
                return Float.isNaN(limiteAPeOnibus) ? limiteGlobal : limiteAPeOnibus;
            default:
                return limiteGlobal;
        }
    }

    // Retorna o limite geral
    public float getLimiteGlobal() {
        return limiteGlobal;
    }
}
