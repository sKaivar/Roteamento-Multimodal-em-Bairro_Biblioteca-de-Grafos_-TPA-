package servico;

import dominio.Modalidade;

// Guarda o tempo limite das rotas
public class RestricaoTempo implements IRestricaoTempo {

    private final float limiteGlobal;
    private float limiteAPe = Float.NaN;
    private float limiteBicicleta = Float.NaN;
    private float limiteAPeOnibus = Float.NaN;

    // Construtor sem limite de tempo
    public RestricaoTempo() {
        this.limiteGlobal = Float.POSITIVE_INFINITY;
    }

    // Construtor com limite geral
    public RestricaoTempo(float limiteGlobal) {
        if (limiteGlobal < 0) {
            throw new IllegalArgumentException("Limite de tempo nao pode ser negativo.");
        }
        this.limiteGlobal = limiteGlobal;
    }

    // Define limite especifico para uma modalidade
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

    // Retorna o limite que vale para a modalidade
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

    // Retorna limite geral
    public float getLimiteGlobal() {
        return limiteGlobal;
    }
}
