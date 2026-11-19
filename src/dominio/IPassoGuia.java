package dominio;

/**
 * Um passo do guia textual (saída 4). Arestas consecutivas com o mesmo rótulo e o mesmo
 * meio devem ser FUNDIDAS num único passo: "caminhe 4 min pela Rua X", e não quatro passos
 * de um minuto pela mesma rua.
 */
public interface IPassoGuia {

    MeioTransporte getMeio();

    /** @return frase do passo (ex.: "Embarque na linha 0160-1 no Ponto Edifício Veneza"). */
    String getDescricao();

    /** @return duração do passo em minutos. */
    float getMinutos();
}
