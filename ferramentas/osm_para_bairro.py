#!/usr/bin/env python3
"""
osm_para_bairro.py — converte um recorte do OpenStreetMap (.osm.xml) no arquivo
textual de dados do projeto "Roteamento Multimodal Intrabairro".

Uso:  python3 osm_para_bairro.py <entrada.osm.xml> <saida.txt>

Decisões de modelagem (espelham a especificação aprovada):
  * Só entram nós dentro do <bounds> do recorte; vias que cruzam a borda são cortadas.
  * Vértices = interseções, extremidades de via, pontos de ônibus e locais de interesse;
    os nós intermediários de uma via (meras curvas do desenho) são contraídos, e a
    distância do trecho é a soma geodésica (haversine) dos segmentos contraídos.
  * Cada TRECHO traz: distância (m) — atributo do qual a aplicação deriva o tempo —,
    nome da via, permissão a pé (S/N), sentido para bicicleta (AMBOS/IDA/NENHUM) e ciclovia (S/N).
      - pedestre ignora mão única; não usa ciclovia exclusiva nem vias privadas;
      - bicicleta respeita mão única das ruas; ciclovias (highway=cycleway) são bidirecionais;
        não usa escadarias nem calçadas sem bicycle=yes.
  * Cada linha de ônibus (relação route=bus) gera uma LINHA e TRECHOS_ONIBUS entre paradas
    consecutivas dentro do recorte; a distância segue as vias da própria relação
    (caminho mínimo dirigido sobre elas), com recurso à malha carroçável completa.
"""
import sys, math, heapq, xml.etree.ElementTree as ET
from collections import defaultdict, Counter
from datetime import date

ARQ_IN, ARQ_OUT = sys.argv[1], sys.argv[2]
raiz = ET.parse(ARQ_IN).getroot()
b = raiz.find('bounds')
MINLAT, MINLON, MAXLAT, MAXLON = (float(b.get(k)) for k in ('minlat', 'minlon', 'maxlat', 'maxlon'))

def tags(el):
    return {t.get('k'): t.get('v') for t in el.iter('tag')}

def dentro(nid):
    la, lo = coord[nid]
    return MINLAT <= la <= MAXLAT and MINLON <= lo <= MAXLON

def hav(a, b):
    (la1, lo1), (la2, lo2) = coord[a], coord[b]
    R = 6371008.8
    p1, p2 = math.radians(la1), math.radians(la2)
    dp, dl = p2 - p1, math.radians(lo2 - lo1)
    h = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * R * math.asin(math.sqrt(h))

coord, ntags = {}, {}
for n in raiz.iter('node'):
    coord[n.get('id')] = (float(n.get('lat')), float(n.get('lon')))
    t = tags(n)
    if t:
        ntags[n.get('id')] = t

vias = {}
for w in raiz.iter('way'):
    vias[w.get('id')] = ([nd.get('ref') for nd in w.iter('nd') if nd.get('ref') in coord], tags(w))

# ---------------------------------------------------------------- classificação das vias
CARRO = {'motorway', 'trunk', 'primary', 'secondary', 'tertiary', 'unclassified', 'residential',
         'living_street', 'service', 'motorway_link', 'trunk_link', 'primary_link',
         'secondary_link', 'tertiary_link', 'busway'}
PEDONAL = {'footway', 'path', 'steps', 'pedestrian', 'track'}

def regra_pe(t):
    hw = t.get('highway')
    if hw is None or t.get('area') == 'yes':
        return False
    if t.get('foot') in ('no',):
        return False
    if t.get('access') in ('private', 'no') and t.get('foot') not in ('yes', 'designated'):
        return False
    if hw in ('motorway', 'motorway_link', 'trunk', 'busway'):
        return False
    if hw == 'cycleway':                     # ciclovia exclusiva: só se liberada a pé
        return t.get('foot') in ('yes', 'designated')
    if hw == 'service' and t.get('service') == 'parking_aisle':
        return False
    return hw in CARRO or hw in PEDONAL

def regra_bici(t):
    """Devolve (sentido, ciclovia): sentido em {'AMBOS','IDA','VOLTA','NENHUM'}."""
    hw = t.get('highway')
    if hw is None or t.get('area') == 'yes' or hw == 'steps':
        return 'NENHUM', False
    if t.get('bicycle') == 'no':
        return 'NENHUM', False
    if t.get('access') in ('private', 'no') and t.get('bicycle') not in ('yes', 'designated'):
        return 'NENHUM', False
    if hw == 'cycleway':
        return 'AMBOS', True                 # ciclovias: sempre bidirecionais (premissa do grupo)
    if hw in ('footway', 'path', 'pedestrian', 'track'):
        if t.get('bicycle') in ('yes', 'designated'):
            return 'AMBOS', False
        return 'NENHUM', False
    if hw in ('motorway', 'motorway_link', 'trunk', 'trunk_link'):
        return 'NENHUM', False
    if hw not in CARRO:
        return 'NENHUM', False
    ow = t.get('oneway')
    if t.get('junction') in ('roundabout', 'circular') and ow is None:
        ow = 'yes'
    excecao = t.get('oneway:bicycle') == 'no' or str(t.get('cycleway', '')).startswith('opposite')
    if ow in ('yes', 'true', '1') and not excecao:
        return 'IDA', False
    if ow == '-1' and not excecao:
        return 'VOLTA', False
    return 'AMBOS', False

def sentido_carro(t):
    ow = t.get('oneway')
    if t.get('junction') in ('roundabout', 'circular') and ow is None:
        ow = 'yes'
    if t.get('oneway:bus') == 'no' or t.get('oneway:psv') == 'no':
        return 'AMBOS'
    return {'yes': 'IDA', 'true': 'IDA', '1': 'IDA', '-1': 'VOLTA'}.get(ow, 'AMBOS')

uteis = {}   # wid -> (nds, tags, pe, bici, ciclovia)
for wid, (nds, t) in vias.items():
    if len(nds) < 2 or 'highway' not in t:
        continue
    pe = regra_pe(t)
    bici, cv = regra_bici(t)
    if pe or bici != 'NENHUM':
        if bici == 'VOLTA':                  # normaliza: inverte a via e trata como IDA
            nds = list(reversed(nds)); bici = 'IDA'
        uteis[wid] = (nds, t, pe, bici, cv)

nome_via = lambda t: t.get('name') or {
    'footway': 'calçada/passagem', 'path': 'trilha', 'steps': 'escadaria', 'cycleway': 'ciclovia',
    'service': 'via de serviço', 'pedestrian': 'via de pedestres'}.get(t.get('highway'), 'via sem nome')
def nome_trecho(t):
    """Nome exibido no guia textual; travessias recebem rótulo próprio."""
    if t.get('footway') == 'crossing' or t.get('highway') == 'crossing':
        return 'faixa de travessia'
    return nome_via(t)

# ---------------------------------------------------------------- paradas de ônibus
rotas = []
for rel in raiz.iter('relation'):
    t = tags(rel)
    if t.get('type') != 'route' or t.get('route') not in ('bus', 'trolleybus'):
        continue
    membros = [(m.get('type'), m.get('ref'), m.get('role') or '') for m in rel.iter('member')]
    rotas.append((rel.get('id'), t, membros))

nos_carro_por_via = {}
for wid, (nds, t) in vias.items():
    if t.get('highway') in CARRO:
        nos_carro_por_via[wid] = nds

def mais_proximo(nid, candidatos):
    melhor, dm = None, 1e18
    for c in candidatos:
        d = hav(nid, c)
        if d < dm:
            melhor, dm = c, d
    return melhor, dm

# nós caminháveis dentro do recorte, restritos ao maior componente conexo da malha a pé
# (assim, paradas e locais de interesse nunca são ancorados numa "ilha" desconexa)
viz = defaultdict(set)
for wid, (nds, t, pe, bici, cv) in uteis.items():
    if not pe: continue
    for x, y in zip(nds, nds[1:]):
        if dentro(x) and dentro(y):
            viz[x].add(y); viz[y].add(x)
maior = set()
for s in list(viz):
    if s in maior: continue
    comp, pilha = set(), [s]
    while pilha:
        u = pilha.pop()
        if u in comp: continue
        comp.add(u); pilha.extend(viz[u] - comp)
    if len(comp) > len(maior): maior = comp
nos_pe_dentro = maior

parada_de = {}             # nó OSM (posição/plataforma) -> nó viário que representa a parada
nome_parada = {}           # nó viário -> nome
seq_rotas = []             # (relid, tags, [nós viários das paradas, ou None se fora do recorte])
for relid, t, membros in rotas:
    vias_rota = [r for ty, r, ro in membros if ty == 'way' and r in nos_carro_por_via]
    cand = {n for w in vias_rota for n in nos_carro_por_via[w] if n in coord and dentro(n) and n in nos_pe_dentro}
    if not cand:
        cand = {n for w in nos_carro_por_via.values() for n in w if dentro(n) and n in nos_pe_dentro}
    st = [r for ty, r, ro in membros if ty == 'node' and ro.startswith('stop')]
    if not st:
        st = [r for ty, r, ro in membros if ty == 'node' and ro.startswith('platform')]
    seq = []
    for s in st:
        if s not in coord or not dentro(s):
            seq.append(None); continue
        if s in cand:
            alvo, d = s, 0.0
        else:
            alvo, d = mais_proximo(s, cand)
        if alvo is None or d > 60:
            seq.append(None); continue
        parada_de[s] = alvo
        nm = ntags.get(s, {}).get('name')
        if nm and alvo not in nome_parada:
            nome_parada[alvo] = nm
        seq.append(alvo)
    seq_rotas.append((relid, t, seq))

paradas = set(parada_de.values())

# ---------------------------------------------------------------- locais de interesse
CHAVES_POI = ('amenity', 'shop', 'leisure', 'tourism', 'office')
EXCLUI = {'taxi', 'parking_entrance', 'parking', 'bench', 'waste_basket', 'parking_space'}
pois = []   # (chave, nome, lat, lon)
for nid, t in ntags.items():
    if 'name' in t and dentro(nid) and any(k in t for k in CHAVES_POI):
        tipo = next(t[k] for k in CHAVES_POI if k in t)
        if tipo not in EXCLUI and t.get('highway') is None and t.get('public_transport') is None:
            pois.append(('n' + nid, t['name'], *coord[nid]))
for wid, (nds, t) in vias.items():
    if 'name' in t and 'highway' not in t and any(k in t for k in CHAVES_POI) and nds:
        tipo = next(t[k] for k in CHAVES_POI if k in t)
        if tipo in EXCLUI:
            continue
        la = sum(coord[n][0] for n in nds) / len(nds); lo = sum(coord[n][1] for n in nds) / len(nds)
        if MINLAT <= la <= MAXLAT and MINLON <= lo <= MAXLON:
            pois.append(('w' + wid, t['name'], la, lo))
# nó viário de acesso de cada local de interesse
for i, (k, nm, la, lo) in enumerate(pois):
    coord['POI_' + k] = (la, lo)
acesso_poi = {}
for k, nm, la, lo in pois:
    alvo, d = mais_proximo('POI_' + k, nos_pe_dentro)
    if alvo is not None and d <= 150:
        acesso_poi[k] = (alvo, d)

# ---------------------------------------------------------------- nós relevantes e trechos
uso = Counter()
for wid, (nds, t, pe, bici, cv) in uteis.items():
    for n in set(nds):
        if dentro(n):
            uso[n] += 1
relevante = {n for n, c in uso.items() if c >= 2} | paradas | {a for a, _ in acesso_poi.values()}
for wid, (nds, t, pe, bici, cv) in uteis.items():
    corridas, atual = [], []
    for n in nds:                       # corridas contíguas dentro do recorte
        if dentro(n):
            atual.append(n)
        else:
            if len(atual) >= 2: corridas.append(atual)
            atual = []
    if len(atual) >= 2: corridas.append(atual)
    for c in corridas:
        relevante.add(c[0]); relevante.add(c[-1])

trechos = {}   # (a,b,via,pe,bici,cv) -> menor distância
for wid, (nds, t, pe, bici, cv) in uteis.items():
    corridas, atual = [], []
    for n in nds:
        if dentro(n): atual.append(n)
        else:
            if len(atual) >= 2: corridas.append(atual)
            atual = []
    if len(atual) >= 2: corridas.append(atual)
    for c in corridas:
        ini, dist = c[0], 0.0
        for x, y in zip(c, c[1:]):
            dist += hav(x, y)
            if y in relevante:
                if y != ini:
                    chave = (ini, y, nome_trecho(t), pe, bici, cv)
                    if chave not in trechos or dist < trechos[chave]:
                        trechos[chave] = dist
                ini, dist = y, 0.0

# remove trechos inúteis e mantém o componente principal (a pé) — ilhas soltas confundem a demo
adj = defaultdict(set)
for (a, b_, via, pe, bici, cv) in trechos:
    adj[a].add(b_); adj[b_].add(a)
vistos, comps = set(), []
for s in adj:
    if s in vistos: continue
    comp, pilha = set(), [s]
    while pilha:
        u = pilha.pop()
        if u in comp: continue
        comp.add(u); pilha.extend(adj[u] - comp)
    vistos |= comp; comps.append(comp)
principal = max(comps, key=len)
trechos = {k: v for k, v in trechos.items() if k[0] in principal and k[1] in principal}
paradas &= principal

# ---------------------------------------------------------------- trechos de ônibus
grafo_carro = defaultdict(list)       # malha carroçável completa (inclusive fora do recorte)
def arestas_via(nds, sent):
    for x, y in zip(nds, nds[1:]):
        d = hav(x, y)
        if sent in ('IDA', 'AMBOS'): yield x, y, d
        if sent in ('VOLTA', 'AMBOS'): yield y, x, d
for wid, nds in nos_carro_por_via.items():
    for x, y, d in arestas_via(nds, sentido_carro(vias[wid][1])):
        grafo_carro[x].append((y, d))

def dijkstra(g, s, alvo, limite=6000):
    dist = {s: 0.0}; pq = [(0.0, s)]
    while pq:
        d, u = heapq.heappop(pq)
        if u == alvo: return d
        if d > dist.get(u, 1e18) or d > limite: continue
        for v, w in g[u]:
            nd = d + w
            if nd < dist.get(v, 1e18):
                dist[v] = nd; heapq.heappush(pq, (nd, v))
    return None

linhas, trechos_onibus, vistos_seq = [], [], set()
contagem_ref = Counter()
for relid, t, seq in seq_rotas:
    ref = (t.get('ref') or relid).strip()
    pares = [(a, b_) for a, b_ in zip(seq, seq[1:]) if a and b_ and a != b_ and a in paradas and b_ in paradas]
    if not pares: continue
    assinatura = (ref, tuple(pares))
    if assinatura in vistos_seq: continue          # variantes idênticas dentro do recorte
    vistos_seq.add(assinatura)
    g_rota = defaultdict(list)
    _, _, membros = next(r for r in rotas if r[0] == relid)
    for ty, r, ro in membros:
        if ty == 'way' and r in vias and vias[r][1].get('highway') in CARRO:
            for x, y, d in arestas_via(vias[r][0], sentido_carro(vias[r][1])):
                g_rota[x].append((y, d))
    validos = []
    for a, b_ in pares:
        d = dijkstra(g_rota, a, b_)
        if d is None: d = dijkstra(grafo_carro, a, b_)
        if d is None or d > 4 * hav(a, b_) + 800:   # trajeto implausível: descarta
            continue
        validos.append((a, b_, d))
    if not validos: continue
    contagem_ref[ref] += 1
    codigo = f"{ref}-{contagem_ref[ref]}"
    nome = f"{ref} {t.get('from', '?')} → {t.get('to', '?')}"
    linhas.append((codigo, nome))
    for a, b_, d in validos:
        trechos_onibus.append((codigo, a, b_, d))

paradas_usadas = {a for _, a, _, _ in trechos_onibus} | {b_ for _, _, b_, _ in trechos_onibus}

# ---------------------------------------------------------------- identificadores e nomes
nos_usados = set()
for (a, b_, *_r) in trechos: nos_usados.update((a, b_))
nomes_incidentes = defaultdict(list)
for (a, b_, via, pe, bici, cv) in trechos:
    for n in (a, b_):
        if via not in nomes_incidentes[n] and via not in ('faixa de travessia',):
            nomes_incidentes[n].append(via)

def nome_intersecao(n):
    vs = [v for v in nomes_incidentes[n] if v[0].isupper()] or nomes_incidentes[n]
    if not vs: return 'Interseção'
    if len(vs) == 1: return vs[0]
    return ' × '.join(vs[:2])

ids, nomes_vistos, vertices = {}, Counter(), []
def registra(n, prefixo, nome, tipo, la, lo):
    nomes_vistos[nome] += 1
    if nomes_vistos[nome] > 1: nome = f"{nome} ({nomes_vistos[nome]})"
    ident = f"{prefixo}{sum(1 for v in vertices if v[0].startswith(prefixo)) + 1:04d}"
    ids[n] = ident
    vertices.append((ident, nome, la, lo, tipo))

ordem = sorted(nos_usados, key=lambda n: (-coord[n][0], coord[n][1]))   # norte→sul, oeste→leste
for n in ordem:
    if n in paradas_usadas:
        nm = nome_parada.get(n)
        rua = nome_intersecao(n).split(' × ')[0]
        registra(n, 'P', f"Ponto {nm}" if nm else f"Ponto {rua}", 'PARADA', *coord[n])
for n in ordem:
    if n not in ids:
        registra(n, 'I', nome_intersecao(n), 'INTERSECAO', *coord[n])
for k, nm, la, lo in sorted(pois, key=lambda p: p[1]):
    if k in acesso_poi and acesso_poi[k][0] in ids:
        registra('POI_' + k, 'L', nm, 'INTERESSE', la, lo)

def f6(x): return f"{x:.7f}"
with open(ARQ_OUT, 'w', encoding='utf-8') as f:
    w = f.write
    w("# ======================================================================\n")
    w("# Roteamento Multimodal Intrabairro — dados do bairro (recorte de Jardim Camburi, Vitória/ES)\n")
    w(f"# Gerado em {date.today().isoformat()} a partir de OpenStreetMap (© colaboradores do OSM, licença ODbL)\n")
    w(f"# Recorte: lat [{MINLAT}, {MAXLAT}]  lon [{MINLON}, {MAXLON}]\n")
    w("# Formato: linhas iniciadas por '#' são comentários; campos separados por ';'.\n")
    w("# Distâncias em metros — são ATRIBUTOS; o peso de cada aresta (minutos) é derivado na aplicação.\n")
    w("# ======================================================================\n\n")
    w("[VERTICES]\n# id;nome;latitude;longitude;tipo   (tipo: INTERSECAO | PARADA | INTERESSE)\n")
    for v in vertices:
        w(f"{v[0]};{v[1]};{f6(v[2])};{f6(v[3])};{v[4]}\n")
    w("\n[TRECHOS]\n")
    w("# origem;destino;distancia_m;via;pe;bici;ciclovia\n")
    w("#   pe       S|N            — trecho caminhável (pedestre ignora mão única)\n")
    w("#   bici     AMBOS|IDA|NENHUM — IDA = pedalável apenas no sentido origem→destino\n")
    w("#   ciclovia S|N            — S: tempo de bicicleta multiplicado por 1/2\n")
    linhas_tr = []
    for (a, b_, via, pe, bici, cv), d in trechos.items():
        linhas_tr.append((ids[a], ids[b_], d, via, 'S' if pe else 'N', bici, 'S' if cv else 'N'))
    for k, nm, la, lo in pois:
        if 'POI_' + k in ids:
            alvo, d = acesso_poi[k]
            linhas_tr.append((ids['POI_' + k], ids[alvo], max(d, 1.0), f"acesso a {nm}", 'S', 'AMBOS', 'N'))
    for r in sorted(linhas_tr):
        w(f"{r[0]};{r[1]};{r[2]:.1f};{r[3]};{r[4]};{r[5]};{r[6]}\n")
    w("\n[LINHAS]\n# codigo;nome   (código = número da linha + variante de sentido)\n")
    for c, nm in linhas:
        w(f"{c};{nm}\n")
    w("\n[TRECHOS_ONIBUS]\n# linha;parada_origem;parada_destino;distancia_m   (dirigido, na ordem de percurso)\n")
    for c, a, b_, d in trechos_onibus:
        w(f"{c};{ids[a]};{ids[b_]};{d:.1f}\n")

tipos = Counter(v[4] for v in vertices)
print('vértices', len(vertices), dict(tipos))
print('trechos', len(linhas_tr), 'pe', sum(r[4] == 'S' for r in linhas_tr),
      'bici', Counter(r[5] for r in linhas_tr), 'ciclovia', sum(r[6] == 'S' for r in linhas_tr))
print('linhas', len(linhas), 'trechos ônibus', len(trechos_onibus), 'paradas', len(paradas_usadas))
