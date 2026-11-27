package servico;

import dominio.Modalidade;

/**
 * Armazena os limites máximos de tempo permitidos para uma consulta de rota,
 * permitindo um limite global e eventuais sobrescritas por modalidade.
 */
public class RestricaoTempo implements IRestricaoTempo {

    private final float limiteGlobal;
    private float limiteAPe = Float.NaN;
    private float limiteBicicleta = Float.NaN;
    private float limiteAPeOnibus = Float.NaN;

    /**
     * Cria uma restrição sem limite de tempo (limite infinito).
     */
    public RestricaoTempo() {
        this.limiteGlobal = Float.POSITIVE_INFINITY;
    }

    /**
     * Cria uma restrição com um limite global único para todas as modalidades.
     *
     * @param limiteGlobal tempo máximo em minutos (não negativo)
     * @throws IllegalArgumentException se o limite for negativo
     */
    public RestricaoTempo(float limiteGlobal) {
        if (limiteGlobal < 0) {
            throw new IllegalArgumentException("Limite de tempo nao pode ser negativo.");
        }
        this.limiteGlobal = limiteGlobal;
    }

    /**
     * Define um limite específico para uma modalidade, sobrescrevendo o limite global.
     *
     * @param modalidade modalidade que receberá o limite específico
     * @param limite tempo máximo em minutos para essa modalidade
     * @throws IllegalArgumentException se a modalidade for nula ou o limite for negativo
     */
    public void definirLimite(Modalidade modalidade, float limite) {
        if (modalidade == null) {
            throw new IllegalArgumentException("Modalidade nao pode ser nula.");
        }
        if (limite < 0) {
            throw new IllegalArgumentException("Limite nao pode ser negativo.");
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

    /**
     * Retorna o limite aplicável à modalidade informada.
     *
     * @param modalidade modalidade consultada
     * @return limite específico da modalidade ou o limite global caso não haja sobrescrita
     */
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

    /**
     * @return o limite global padrão em minutos
     */
    public float getLimiteGlobal() {
        return limiteGlobal;
    }
}
