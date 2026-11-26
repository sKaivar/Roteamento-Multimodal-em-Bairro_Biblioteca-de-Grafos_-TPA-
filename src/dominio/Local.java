package dominio;

public abstract class Local implements ILocal {
    private final String id;
    private final String nome;
    private double latitude;
    private double longitude;
    private TipoLocal tipo;
    private static final double METROS_POR_GRAU_LAT = 111_320.0;
    private static final double METROS_POR_GRAU_LON = 104_430.0; // 111 320 × cos(20,26°)

    public Local(String id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getNome() {
        return nome;
    }

    @Override
    public double getLatitude() {
        return latitude;
    }

    @Override
    public double getLongitude() {
        return longitude;
    }

    @Override
    public TipoLocal getTipo() {
        return tipo;
    }

    @Override
    public double distanciaMetros(ILocal outro) {
        double dy = (outro.getLatitude()  - this.latitude)  * METROS_POR_GRAU_LAT;
        double dx = (outro.getLongitude() - this.longitude) * METROS_POR_GRAU_LON;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
