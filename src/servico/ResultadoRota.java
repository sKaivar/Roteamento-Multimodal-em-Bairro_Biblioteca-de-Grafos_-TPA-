package servico;

import dominio.IPassoGuia;
import dominio.IResultadoRota;
import dominio.MeioTransporte;
import dominio.Modalidade;
import estruturas.ILista;
import estruturas.ListaEncadeada;

// Guarda o resultado de uma rota calculada
public class ResultadoRota implements IResultadoRota {

    private final Modalidade modalidade;
    private final boolean existeRota;
    private final float limiteMinutos;
    private final float tempoTotal;
    private final float tempoCaminhada;
    private final float tempoEspera;
    private final float tempoOnibus;
    private final float tempoBicicleta;
    private final ILista<IPassoGuia> guia;

    // Construtor completo
    public ResultadoRota(
            Modalidade modalidade,
            boolean existeRota,
            float limiteMinutos,
            float tempoTotal,
            float tempoCaminhada,
            float tempoEspera,
            float tempoOnibus,
            float tempoBicicleta,
            ILista<IPassoGuia> guia
    ) {
        if (modalidade == null) {
            throw new IllegalArgumentException("Modalidade nao pode ser nula.");
        }

        this.modalidade = modalidade;
        this.existeRota = existeRota;
        this.limiteMinutos = limiteMinutos;
        this.tempoTotal = tempoTotal;
        this.tempoCaminhada = tempoCaminhada;
        this.tempoEspera = tempoEspera;
        this.tempoOnibus = tempoOnibus;
        this.tempoBicicleta = tempoBicicleta;
        this.guia = (guia == null) ? new ListaEncadeada<>() : guia;
    }

    // Cria resultado quando nao ha caminho possivel
    public static ResultadoRota semRota(Modalidade modalidade, float limiteMinutos) {
        return new ResultadoRota(
                modalidade,
                false,
                limiteMinutos,
                Float.POSITIVE_INFINITY,
                0,
                0,
                0,
                0,
                new ListaEncadeada<>()
        );
    }

    // Retorna a modalidade
    @Override
    public Modalidade getModalidade() {
        return modalidade;
    }

    // Informa se achou caminho entre os dois pontos
    @Override
    public boolean existeRota() {
        return existeRota;
    }

    // Informa se existe rota e nao estourou o limite de tempo
    @Override
    public boolean isViavel() {
        return existeRota && (tempoTotal <= limiteMinutos);
    }

    // Retorna o limite aplicado
    @Override
    public float getLimiteMinutos() {
        return limiteMinutos;
    }

    // Retorna tempo total em minutos
    @Override
    public float getTempoTotal() {
        return tempoTotal;
    }

    // Retorna tempo gasto em um meio especifico
    @Override
    public float getTempoPor(MeioTransporte meio) {
        if (meio == null) {
            return 0;
        }

        switch (meio) {
            case CAMINHADA:
                return tempoCaminhada;
            case ESPERA:
                return tempoEspera;
            case ONIBUS:
                return tempoOnibus;
            case BICICLETA:
                return tempoBicicleta;
            default:
                return 0;
        }
    }

    // Retorna os passos do caminho
    @Override
    public ILista<IPassoGuia> getGuia() {
        return guia;
    }

    // Ordena do menor para o maior tempo
    @Override
    public int compareTo(IResultadoRota outro) {
        if (outro == null) {
            return -1;
        }
        return Float.compare(this.tempoTotal, outro.getTempoTotal());
    }

    // Formata o resultado para impressao
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Modalidade: ").append(modalidade).append("\n");

        if (!existeRota) {
            sb.append("Resultado: Inviavel (nao existe caminho entre os locais).\n");
            return sb.toString();
        }

        sb.append(String.format("Tempo total: %.1f min (Limite: %.1f min)\n", tempoTotal, limiteMinutos));
        sb.append("Veredito: ").append(isViavel() ? "VIAVEL" : "INVIAVEL (tempo estourou o limite)").append("\n");

        sb.append("Detalhamento do tempo:\n");
        if (tempoCaminhada > 0) sb.append(String.format("  - Caminhada: %.1f min\n", tempoCaminhada));
        if (tempoEspera > 0) sb.append(String.format("  - Espera no ponto: %.1f min\n", tempoEspera));
        if (tempoOnibus > 0) sb.append(String.format("  - Onibus: %.1f min\n", tempoOnibus));
        if (tempoBicicleta > 0) sb.append(String.format("  - Bicicleta: %.1f min\n", tempoBicicleta));

        sb.append("Guia do trajeto:\n");
        for (IPassoGuia passo : guia) {
            sb.append("  ").append(passo.toString()).append("\n");
        }

        return sb.toString();
    }
}
