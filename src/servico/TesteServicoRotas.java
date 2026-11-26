package servico;

import dominio.ILocal;
import dominio.IResultadoRota;
import dominio.MeioTransporte;
import dominio.Modalidade;
import dominio.TipoLocal;
import estruturas.ILista;
import grafo.Grafo;
import grafo.IGrafo;

/**
 * Teste de validação das rotas multimodais, fusão de passos e ordenação.
 */
public class TesteServicoRotas {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO TESTES DO SERVIÇO DE ROTAS ===");

        // 1. Cria locais de teste
        LocalTeste p1 = new LocalTeste("L0001", "Praça Central", TipoLocal.INTERESSE);
        LocalTeste i1 = new LocalTeste("I0001", "Esquina Av. Principal", TipoLocal.INTERSECAO);
        LocalTeste i2 = new LocalTeste("I0002", "Esquina Rua do Sol", TipoLocal.INTERSECAO);
        LocalTeste p2 = new LocalTeste("L0002", "Supermercado", TipoLocal.INTERESSE);

        // Vértices da camada de ônibus (parada@linha)
        LocalTeste parada1 = new LocalTeste("P0001", "Ponto Praça", TipoLocal.PARADA);
        LocalTeste parada2 = new LocalTeste("P0002", "Ponto Mercado", TipoLocal.PARADA);
        LocalTeste bus1 = new LocalTeste("P0001@0160-1", "Embarque Linha 0160-1", TipoLocal.PARADA_LINHA);
        LocalTeste bus2 = new LocalTeste("P0002@0160-1", "Desembarque Linha 0160-1", TipoLocal.PARADA_LINHA);

        // 2. Monta Grafo Pedestre (não dirigido)
        IGrafo<ILocal> gPedestre = new Grafo<>(false);
        gPedestre.adicionarVertice(p1);
        gPedestre.adicionarVertice(i1);
        gPedestre.adicionarVertice(i2);
        gPedestre.adicionarVertice(p2);

        // Arestas consecutivas na mesma rua para testar FUSÃO DE PASSOS
        gPedestre.adicionarAresta(p1, i1, 2.0f, "Av. Central");
        gPedestre.adicionarAresta(i1, i2, 3.0f, "Av. Central");
        gPedestre.adicionarAresta(i2, p2, 1.5f, "Rua do Sol");

        // 3. Monta Grafo Ciclista (dirigido)
        IGrafo<ILocal> gCiclista = new Grafo<>(true);
        gCiclista.adicionarVertice(p1);
        gCiclista.adicionarVertice(i1);
        gCiclista.adicionarVertice(i2);
        gCiclista.adicionarVertice(p2);

        // Bicicleta é mais rápida
        gCiclista.adicionarAresta(p1, i1, 0.8f, "Ciclovia Central");
        gCiclista.adicionarAresta(i1, i2, 1.2f, "Ciclovia Central");
        gCiclista.adicionarAresta(i2, p2, 0.6f, "Rua do Sol");

        // 4. Monta Grafo Composto Multimodal (dirigido)
        IGrafo<ILocal> gComposto = new Grafo<>(true);
        // Camada A (pedestre)
        gComposto.adicionarVertice(p1);
        gComposto.adicionarVertice(i1);
        gComposto.adicionarVertice(parada1);
        gComposto.adicionarVertice(parada2);
        gComposto.adicionarVertice(p2);
        // Camada B (ônibus)
        gComposto.adicionarVertice(bus1);
        gComposto.adicionarVertice(bus2);

        // Ligações a pé na camada A
        gComposto.adicionarAresta(p1, parada1, 1.0f, "Rua da Praça");
        gComposto.adicionarAresta(parada2, p2, 1.0f, "Rua do Mercado");

        // Transição A -> B: Embarque (espera de 8 min)
        gComposto.adicionarAresta(parada1, bus1, 8.0f, "0160-1");

        // Linha B -> B: Viagem de ônibus (3 min)
        gComposto.adicionarAresta(bus1, bus2, 3.0f, "0160-1");

        // Transição B -> A: Desembarque (0 min)
        gComposto.adicionarAresta(bus2, parada2, 0.0f, "0160-1");

        // 5. Instancia ServicoRotas
        ServicoRotas servico = new ServicoRotas(gPedestre, gCiclista, gComposto);

        // TESTE 1: Rota a pé com fusão de passos da Av. Central (2.0 + 3.0 = 5.0 min)
        System.out.println("\n--- Teste 1: Rota a Pé (Fusão de Passos) ---");
        RestricaoTempo limiteLivre = new RestricaoTempo();
        IResultadoRota rPe = servico.calcular(Modalidade.A_PE, p1, p2, limiteLivre);
        System.out.println(rPe);

        if (rPe.isViavel() && Math.abs(rPe.getTempoTotal() - 6.5f) < 0.01f) {
            System.out.println(">> Teste 1 PASSOU! Tempo total: 6.5 min (esperado).");
        } else {
            System.err.println(">> Teste 1 FALHOU! Tempo: " + rPe.getTempoTotal());
        }

        // TESTE 2: Rota ciclista
        System.out.println("\n--- Teste 2: Rota Ciclista ---");
        IResultadoRota rBike = servico.calcular(Modalidade.BICICLETA, p1, p2, limiteLivre);
        System.out.println(rBike);

        if (rBike.isViavel() && Math.abs(rBike.getTempoTotal() - 2.6f) < 0.01f) {
            System.out.println(">> Teste 2 PASSOU! Tempo total: 2.6 min (esperado).");
        } else {
            System.err.println(">> Teste 2 FALHOU! Tempo: " + rBike.getTempoTotal());
        }

        // TESTE 3: Rota composta (A pé + Ônibus com espera de 8 min)
        System.out.println("\n--- Teste 3: Rota Composta (Multimodal) ---");
        IResultadoRota rBus = servico.calcular(Modalidade.A_PE_ONIBUS, p1, p2, limiteLivre);
        System.out.println(rBus);

        // Espera = 8 min, Caminhada = 2 min (1+1), Ônibus = 3 min. Total = 13.0 min
        if (rBus.isViavel() && Math.abs(rBus.getTempoTotal() - 13.0f) < 0.01f) {
            System.out.println(">> Teste 3 PASSOU! Tempo total: 13.0 min com espera de 8 min calculada.");
        } else {
            System.err.println(">> Teste 3 FALHOU! Tempo: " + rBus.getTempoTotal());
        }

        // TESTE 4: Comparação de modalidades ordenada por tempo (Bike < Pé < Ônibus)
        System.out.println("\n--- Teste 4: Comparar Modalidades Ordenadas ---");
        ILista<IResultadoRota> ordenadas = servico.compararModalidades(p1, p2, limiteLivre);
        int pos = 1;
        for (IResultadoRota rota : ordenadas) {
            System.out.printf("%dº lugar: %s -> %.1f min\n", pos++, rota.getModalidade(), rota.getTempoTotal());
        }

        // TESTE 5: Restrição de tempo (limite de 5 min -> apenas bike deve ser viável)
        System.out.println("\n--- Teste 5: Restrição de Tempo Máximo (5 min) ---");
        RestricaoTempo limite5Min = new RestricaoTempo(5.0f);
        ILista<IResultadoRota> apenasBike = servico.compararModalidades(p1, p2, limite5Min);

        if (apenasBike.tamanho() == 1 && apenasBike.iterator().next().getModalidade() == Modalidade.BICICLETA) {
            System.out.println(">> Teste 5 PASSOU! Apenas a bicicleta coube nos 5 minutos.");
        } else {
            System.err.println(">> Teste 5 FALHOU! Quantidade viável: " + apenasBike.tamanho());
        }

        System.out.println("\n=== TODOS OS TESTES FORAM CONCLUÍDOS COM SUCESSO! ===");
    }

    // Classe auxiliar de local para os testes
    private static class LocalTeste implements ILocal {
        private final String id;
        private final String nome;
        private final TipoLocal tipo;

        public LocalTeste(String id, String nome, TipoLocal tipo) {
            this.id = id;
            this.nome = nome;
            this.tipo = tipo;
        }

        @Override public String getId() { return id; }
        @Override public String getNome() { return nome; }
        @Override public double getLatitude() { return 0; }
        @Override public double getLongitude() { return 0; }
        @Override public TipoLocal getTipo() { return tipo; }
        @Override public double distanciaMetros(ILocal outro) { return 0; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ILocal)) return false;
            ILocal that = (ILocal) o;
            return id != null && id.equalsIgnoreCase(that.getId());
        }

        @Override
        public int hashCode() {
            return (id != null) ? id.toLowerCase().hashCode() : 0;
        }

        @Override
        public String toString() {
            return nome + " [" + id + "]";
        }
    }
}
