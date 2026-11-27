package dominio;

public class Trecho implements ITrecho {
    private final ILocal origem;
    private final ILocal destino;
    private final double distanciaMetros;
    private final String nomeVia;
    private final boolean permitePedestre;
    private final SentidoBicicleta sentidoBicicleta;
    private final boolean temCiclovia;

    public Trecho(ILocal origem, ILocal destino, double distanciaMetros, String nomeVia,
                  boolean permitePedestre, SentidoBicicleta sentidoBicicleta, boolean temCiclovia) {
        this.origem = origem;
        this.destino = destino;
        this.distanciaMetros = distanciaMetros;
        this.nomeVia = nomeVia;
        this.permitePedestre = permitePedestre;
        this.sentidoBicicleta = sentidoBicicleta;
        this.temCiclovia = temCiclovia;
    }

    @Override
    public ILocal getOrigem() {
        return origem;
    }

    @Override
    public ILocal getDestino() {
        return destino;
    }

    @Override
    public double getDistanciaMetros() {
        return distanciaMetros;
    }

    @Override
    public String getNomeVia() {
        return nomeVia;
    }

    @Override
    public boolean permitePedestre() {
        return permitePedestre;
    }

    @Override
    public SentidoBicicleta getSentidoBicicleta() {
        return sentidoBicicleta;
    }

    @Override
    public boolean temCiclovia() {
        return temCiclovia;
    }
    
}
