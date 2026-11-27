package dominio;

public class TrechoOnibus implements ITrechoOnibus {
    private final ILinhaOnibus linha;
    private final ILocal origem;
    private final ILocal destino;
    private final double distanciaMetros;

    public TrechoOnibus(ILinhaOnibus linha, ILocal origem, ILocal destino, double distanciaMetros) {
        this.linha = linha;
        this.origem = origem;
        this.destino = destino;
        this.distanciaMetros = distanciaMetros;
    }

    @Override
    public ILinhaOnibus getLinha() {
        return linha;
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

}
