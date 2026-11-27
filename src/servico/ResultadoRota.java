package servico;

import dominio.IPassoGuia;
import dominio.IResultadoRota;
import dominio.MeioTransporte;
import dominio.Modalidade;
import estruturas.ILista;
import estruturas.ListaEncadeada;

/**
 * Consolida o resultado do cálculo de uma rota para uma modalidade:
 * existência de caminho, viabilidade frente ao limite de tempo, detalhamento
 * por meio de transporte e guia passo a passo.
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

    /**
     * Cria um resultado completo de rota.
     *
     * @param modalidade modalidade avaliada
     * @param existeRota indica se existe caminho entre origem e destino
     * @param limiteMinutos limite de tempo aplicado na avaliação
     * @param tempoTotal soma dos tempos de todos os trechos da rota
     * @param tempoCaminhada tempo gasto caminhando em minutos
     * @param tempoEspera tempo gasto aguardando embarque em minutos
     * @param tempoOnibus tempo gasto dentro do ônibus em minutos
     * @param tempoBicicleta tempo gasto pedalando em minutos
     * @param guia lista de passos textuais da rota
     * @throws IllegalArgumentException se a modalidade for nula
     */
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

    /**
     * Cria um resultado padrão indicando que não há caminho possível na modalidade.
     *
     * @param modalidade modalidade consultada
     * @param limiteMinutos limite de tempo configurado
     * @return resultado sem rota e com tempo total infinito
     */
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

    /**
     * @return a modalidade avaliada neste resultado
     */
    @Override
    public Modalidade getModalidade() {
        return modalidade;
    }

    /**
     * @return {@code true} se existe caminho conectando origem e destino
     */
    @Override
    public boolean existeRota() {
        return existeRota;
    }

    /**
     * @return {@code true} se existe rota e o tempo total não ultrapassa o limite
     */
    @Override
    public boolean isViavel() {
        return existeRota && (tempoTotal <= limiteMinutos);
    }

    /**
     * @return limite de tempo aplicado em minutos
     */
    @Override
    public float getLimiteMinutos() {
        return limiteMinutos;
    }

    /**
     * @return tempo total estimado do trajeto em minutos
     */
    @Override
    public float getTempoTotal() {
        return tempoTotal;
    }

    /**
     * Retorna o tempo gasto em um meio de transporte específico dentro da rota.
     *
     * @param meio meio de transporte consultado
     * @return duração em minutos correspondente ao meio informado
     */
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

    /**
     * @return lista ordenada de passos textuais que compõem o trajeto
     */
    @Override
    public ILista<IPassoGuia> getGuia() {
        return guia;
    }

    /**
     * Compara duas rotas pelo tempo total (menor tempo primeiro).
     *
     * @param outro outro resultado de rota para comparação
     * @return valor negativo, zero ou positivo conforme a ordem de tempo total
     */
    @Override
    public int compareTo(IResultadoRota outro) {
        if (outro == null) {
            return -1;
        }
        return Float.compare(this.tempoTotal, outro.getTempoTotal());
    }

    /**
     * @return relatório textual completo da rota, com veredito, tempos parciais e guia
     */
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
