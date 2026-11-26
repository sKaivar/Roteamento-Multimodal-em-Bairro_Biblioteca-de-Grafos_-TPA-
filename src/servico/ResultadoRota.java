package servico;

import dominio.IPassoGuia;
import dominio.IResultadoRota;
import dominio.MeioTransporte;
import dominio.Modalidade;
import estruturas.ILista;
import estruturas.ListaEncadeada;

/**
 * Guarda o resultado completo do cálculo de rota em uma modalidade.
 * Reúne as 4 saídas exigidas:
 * 1. Viabilidade (se deu para chegar e se ficou dentro do limite de tempo)
 * 2. Tempo total em minutos
 * 3. Tempo gasto em cada transporte (a pé, ônibus, espera, bike)
 * 4. Guia passo a passo com instruções
 */
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
            throw new IllegalArgumentException("A modalidade não pode ser nula.");
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

    // Cria um resultado para quando não há caminho possível
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

    // Modalidade calculada
    @Override
    public Modalidade getModalidade() {
        return modalidade;
    }

    // Indica se existe um trajeto interligando os dois pontos
    @Override
    public boolean existeRota() {
        return existeRota;
    }

    // Saída 1: Se existe rota e o tempo total não estourou o limite informado
    @Override
    public boolean isViavel() {
        return existeRota && (tempoTotal <= limiteMinutos);
    }

    // Limite de tempo aplicado nesta consulta
    @Override
    public float getLimiteMinutos() {
        return limiteMinutos;
    }

    // Saída 2: Tempo total de viagem em minutos
    @Override
    public float getTempoTotal() {
        return tempoTotal;
    }

    // Saída 3: Tempo gasto em um meio de transporte específico
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

    // Saída 4: Lista com o passo a passo da rota
    @Override
    public ILista<IPassoGuia> getGuia() {
        return guia;
    }

    // Permite ordenar do mais rápido para o mais demorado
    @Override
    public int compareTo(IResultadoRota outro) {
        if (outro == null) {
            return -1;
        }
        return Float.compare(this.tempoTotal, outro.getTempoTotal());
    }

    // Exibição em formato textual amigável
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Modalidade: ").append(modalidade).append("\n");

        if (!existeRota) {
            sb.append("Resultado: Inviável (não existe caminho conectando os dois locais).\n");
            return sb.toString();
        }

        sb.append(String.format("Tempo total: %.1f min (Limite: %.1f min)\n", tempoTotal, limiteMinutos));
        sb.append("Veredito: ").append(isViavel() ? "VIÁVEL" : "INVIÁVEL (tempo excedeu o limite)").append("\n");

        sb.append("Detalhamento do tempo:\n");
        if (tempoCaminhada > 0) sb.append(String.format("  - Caminhada: %.1f min\n", tempoCaminhada));
        if (tempoEspera > 0) sb.append(String.format("  - Espera em ponto: %.1f min\n", tempoEspera));
        if (tempoOnibus > 0) sb.append(String.format("  - Ônibus: %.1f min\n", tempoOnibus));
        if (tempoBicicleta > 0) sb.append(String.format("  - Bicicleta: %.1f min\n", tempoBicicleta));

        sb.append("Guia do trajeto:\n");
        for (IPassoGuia passo : guia) {
            sb.append("  ").append(passo.toString()).append("\n");
        }

        return sb.toString();
    }
}
