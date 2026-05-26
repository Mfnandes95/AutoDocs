// ── ESTADO GLOBAL ──
let todosOsTermos = [];

// ── INICIALIZAÇÃO ──
document.addEventListener('DOMContentLoaded', () => {
    // Captura a role definida no login para controle de visualização
    const role = localStorage.getItem("user_role");

    if (!role) {
        window.location.href = '/login.html';
        return;
    }
    
    // Se for do perfil RECEPCAO, o escopo deste script é travado visualmente
    if (role === 'RECEPCAO') {
        document.getElementById('dashboard').innerHTML = `
            <div class="empty-state">
                <span style="font-size:2rem">🚫</span>
                <p style="margin-top:8px;color:var(--danger);font-weight:700;">Acesso Não Autorizado</p>
                <p>Seu perfil está autorizado apenas para a emissão e geração de novos termos.</p>
            </div>`;
        document.getElementById('loading').style.display = 'none';
        return; 
    }

    // Mantém o escopo de execução original para GESTOR ou TECNICO
    carregarDados();
    document.getElementById('filtroPredio').addEventListener('input', filtrarPredio);
});

// ── TOAST ──
function toast(msg, tipo = 'ok') {
    const t = document.getElementById('toast');
    t.className = `toast ${tipo}`;
    t.innerHTML = (tipo === 'ok' ? '✅' : '❌') + ' ' + msg;
    t.classList.add('show');
    setTimeout(() => t.classList.remove('show'), 3500);
}

// ── CARREGAR DADOS ──
async function carregarDados() {
    const btn = document.getElementById('btnAtualizar');
    btn.classList.add('loading');
    btn.textContent = '⟳ Atualizando...';

    document.getElementById('loading').style.display = 'flex';
    document.getElementById('dashboard').innerHTML = '';

    try {
        const res = await fetch('/docs/listar-todos', {
            method: 'GET',
            credentials: 'same-origin',
            headers: { 'Accept': 'application/json' }
        });

        if (res.redirected && res.url.includes('login')) {
            window.location.href = '/login.html';
            return;
        }

        if (res.status === 401 || res.status === 403) {
            window.location.href = '/login.html';
            return;
        }

        if (!res.ok) throw new Error(`HTTP ${res.status}`);

        const raw = await res.text();
        console.log('[DASHBOARD] Resposta bruta:', raw);

        let lista = JSON.parse(raw);
        if (!Array.isArray(lista)) throw new Error('Formato inesperado.');

        console.log(`[DASHBOARD] ${lista.length} registro(s) recebido(s).`);
        if (lista.length > 0) console.log('[DASHBOARD] Exemplo de registro:', lista[0]);

        todosOsTermos = lista;
        atualizarStats(lista);
        renderizarDashboard(lista);

        if (lista.length === 0) {
            toast('Nenhum registro encontrado no banco.', 'err');
        } else {
            toast(`${lista.length} equipamento(s) carregado(s).`);
        }

    } catch (e) {
        console.error('[DASHBOARD] Erro:', e);
        document.getElementById('dashboard').innerHTML = `
            <div class="empty-state">
                <span style="font-size:2rem">⚠️</span>
                <p style="margin-top:8px;color:var(--danger);font-weight:700;">${e.message}</p>
                <p style="margin-top:4px;">Verifique o console para mais detalhes.</p>
            </div>`;
        toast('Erro ao carregar dados.', 'err');
    } finally {
        document.getElementById('loading').style.display = 'none';
        btn.classList.remove('loading');
        btn.textContent = '⟳ Atualizar';
    }
}

// ── STATS ──
function atualizarStats(lista) {
    const unidades  = new Set(lista.map(t => t.unidade).filter(Boolean));
    const ativos    = lista.filter(t => normalizar(t.statusAparelho) === 'ativo').length;
    const avariados = lista.filter(t => normalizar(t.statusAparelho) === 'avariado').length;

    document.getElementById('statTotal').textContent     = lista.length;
    document.getElementById('statUnidades').textContent  = unidades.size;
    document.getElementById('statAtivos').textContent    = ativos;
    document.getElementById('statAvariados').textContent = avariados;
}

// ── FILTRO ──
function filtrarPredio() {
    const termo = document.getElementById('filtroPredio').value.trim().toLowerCase();
    const filtrados = todosOsTermos.filter(t =>
        !termo || t.unidade?.toLowerCase().includes(termo)
    );
    atualizarStats(filtrados);
    renderizarDashboard(filtrados);
}

// ── RENDERIZAR DASHBOARD ──
function renderizarDashboard(lista) {
    const container = document.getElementById('dashboard');
    container.innerHTML = '';

    if (!lista || lista.length === 0) {
        container.innerHTML = `
            <div class="empty-state">
                <span style="font-size:2rem">📭</span>
                <p>Nenhum equipamento encontrado.</p>
            </div>`;
        return;
    }

    const porUnidade = lista.reduce((acc, t) => {
        const u = t.unidade || 'Sem unidade';
        if (!acc[u]) acc[u] = [];
        acc[u].push(t);
        return acc;
    }, {});

    Object.keys(porUnidade)
        .sort()
        .forEach(u => container.appendChild(criarCardUnidade(u, porUnidade[u])));
}

// ── CARD DE UNIDADE ──
function criarCardUnidade(unidade, itens) {
    const card = document.createElement('div');
    card.className = 'predio-card';

    const avariados = itens.filter(t => normalizar(t.statusAparelho) === 'avariado').length;
    const alertaBadge = avariados > 0
        ? `<span style="font-family:'JetBrains Mono',monospace;font-size:10px;font-weight:700;
                background:rgba(255,68,85,0.1);color:#ff4455;border:1px solid rgba(255,68,85,0.2);
                border-radius:20px;padding:3px 8px;">⚠ ${avariados} avaria(s)</span>`
        : '';

    card.innerHTML = `
        <div class="predio-header">
            <div class="predio-nome">
                <span class="ic">🏢</span>
                <span>${unidade}</span>
            </div>
            <div style="display:flex;gap:8px;align-items:center;">
                ${alertaBadge}
                <span class="predio-badge">${itens.length} item(s)</span>
            </div>
        </div>
        <div class="predio-body">
            <div class="equip-list">
                ${itens.map(criarItemEquipamento).join('')}
            </div>
        </div>`;

    return card;
}

// ── ITEM DE EQUIPAMENTO ──
function criarItemEquipamento(t) {
    const status      = t.statusAparelho ?? 'Desconhecido';
    const statusNorm  = normalizar(status);
    const role        = localStorage.getItem("user_role");

    const statusClass = statusNorm === 'ativo'
        ? 'status-ativo'
        : statusNorm === 'avariado'
            ? 'status-avariado'
            : 'status-outro';

    // Determina a ação baseada no status e na Role do usuário conectado
    let acaoBotao = '';
    
    if (statusNorm === 'devolvido') {
        acaoBotao = `<span style="font-size:11px;color:var(--muted);font-style:italic;">Devolvido ✓</span>`;
    } else {
        // Apenas GESTOR ou TECNICO visualizam e interagem com o botão de alteração de status
        if (role === 'GESTOR' || role === 'TECNICO') {
            acaoBotao = `<button
                    onclick="marcarDevolvido(${t.id})"
                    style="background:rgba(0,187,255,0.1);color:#00bbff;border:1px solid rgba(0,187,255,0.25);
                           border-radius:6px;padding:4px 10px;font-size:10px;font-family:'Syne',sans-serif;
                           font-weight:700;cursor:pointer;transition:all 0.2s;"
                    onmouseover="this.style.background='rgba(0,187,255,0.2)'"
                    onmouseout="this.style.background='rgba(0,187,255,0.1)'">
                ↩ Devolver
              </button>`;
        } else {
            acaoBotao = `<span style="font-size:11px;color:var(--muted);font-style:italic;">Apenas Leitura</span>`;
        }
    }

    return `
        <div class="equip-item" id="equip-${t.id}">
            <div class="equip-info">
                <span class="equip-patrimonio">${t.patrimonio ?? '—'}</span>
                <span class="equip-nome">${t.nomeColaborador ?? '—'}</span>
                <span class="equip-tipo">${t.info ?? '—'} · ${t.tipo ?? '—'}</span>
                <span class="equip-tipo">${formatarData(t.dataInicio)} → ${formatarData(t.dataTermino)}</span>
            </div>
            <div style="display:flex;flex-direction:column;align-items:flex-end;gap:6px;">
                <span class="equip-status ${statusClass}">${status}</span>
                ${acaoBotao}
            </div>
        </div>`;
}

// ── MARCAR COMO DEVOLVIDO ──
async function marcarDevolvido(id) {
    if (!confirm('Confirmar devolução deste equipamento?')) return;

    try {
        const res = await fetch(`/docs/${id}/status?status=DEVOLVIDO`, {
            method: 'PATCH',
            credentials: 'same-origin'
        });

        if (res.ok) {
            toast('Equipamento marcado como devolvido!');
            await carregarDados();
        } else {
            toast(`Erro ${res.status} ao atualizar status.`, 'err');
        }
    } catch (e) {
        toast('Erro de conexão.', 'err');
    }
}

// ── HELPERS ──
function normalizar(str) {
    if (!str) return '';
    return str.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

function formatarData(data) {
    if (!data) return '—';
    try {
        if (Array.isArray(data)) {
            const [ano, mes, dia] = data;
            return `${String(dia).padStart(2,'0')}/${String(mes).padStart(2,'0')}/${ano}`;
        }
        return new Date(data).toLocaleDateString('pt-BR');
    } catch {
        return String(data);
    }
}