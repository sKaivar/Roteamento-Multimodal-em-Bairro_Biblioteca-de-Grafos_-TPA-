package persistencia;

import dominio.IMapa;

public class TesteCarga {
    public static void main(String[] args) throws Exception {
        IMapa mapa = new RepositorioMapaArquivo().carregar("dados/bairro.txt");
        System.out.println("Locais: " + mapa.getLocais().tamanho());   // esperado 443
        System.out.println("Trechos: " + mapa.getTrechos().tamanho()); // esperado 620
        System.out.println("Linhas: " + mapa.getLinhas().tamanho());   // esperado 42
        System.out.println("P0004: " + mapa.buscarPorId("P0004"));
    }
}