// Estado global controlado
let todosOsTermos = [];

// Gatilho executado automaticamente ao carregar a página
document.addEventListener('DOMContentLoaded', () => {
    atualizarDashboard();
});

async function atualizarDashboard() {
    const btn = document.getElementById('btnAtualizar');
    const container = document.getElementById('dashboard');
    
    if (btn) btn.disabled = true;
    if (container) container.style.opacity = '0.5';

    try {
        const response = await fetch('/api/docs/listar-todos', {
            method: 'GET',
            credentials: 'same-origin'
        });

        if (!response.ok) {
            throw new Error(`Erro no servidor: HTTP ${response.status}`);
        }

        const result = await response.json();

        if (!result.sucesso) {
            throw new Error(result.mensagem || "Falha ao obter dados");
        }

        todosOsTermos = result.dados || [];

        renderizarStats(todosOsTermos);
        renderizarLista(todosOsTermos);

    } catch (e) {
        console.error("Erro na sincronização:", e);
        mostrarToast("Erro ao sincronizar com motor: " + e.message, 'err');
    } finally {
        if (btn) btn.disabled = false;
        if (container) container.style.opacity = '1';

        const loadingElement = document.getElementById('loading') || document.querySelector('.loading-spinner');
        if (loadingElement) {
            loadingElement.style.display = 'none';
        }
    }
}

function renderizarStats(lista) {
    if (!lista) lista = [];

    // 1. Total de Ativos registrados
    const total = lista.length;

    // 2. Unidades únicas
    const unidadesUnicas = new Set(
        lista
            .map(i => i.unidade || i.nome_unidade || i.nomeUnidade)
            .filter(Boolean)
    ).size;

    // 3. Quantidade de Avariados
    const avariados = lista.filter(i => {
        const st = (i.tipo || i.info || i.status || i.status_aparelho || '').toUpperCase();
        return st.includes('AVARIADO');
    }).length;

    // 4. Ativos operacionais (Total menos os avariados)
    const ativosOperacionais = total - avariados;

    // Atualiza os 4 cards do topo apontando para os IDs corretos do HTML
    const elTotal = document.getElementById('statTotal');
    if (elTotal) elTotal.innerText = total;

    const elUnidades = document.getElementById('statUnidades');
    if (elUnidades) elUnidades.innerText = unidadesUnicas;

    const elAtivos = document.getElementById('statAtivos');
    if (elAtivos) elAtivos.innerText = ativosOperacionais;

    const elAvariados = document.getElementById('statAvariados');
    if (elAvariados) elAvariados.innerText = avariados;
}

function renderizarLista(lista) {
    const container = document.getElementById('dashboard');
    if (!container) return;
    
    container.innerHTML = '';

    if (!lista || lista.length === 0) {
        container.innerHTML = '<p style="text-align: center; color: var(--muted, #888); padding: 20px;">Nenhum documento encontrado.</p>';
        return;
    }

    lista.forEach(item => {
        // 1. Patrimônio
        const patrimonio = item.patrimonio || item.codigo_patrimonio || 'SEM PATRIMÔNIO';
        
        // 2. Colaborador
        const colaborador = item.nome_colaborador 
            || item.nomeColaborador 
            || item.colaborador 
            || 'Sem Colaborador';
            
        // 3. Unidade
        const unidade = item.unidade || 'N/A';
            
        // 4. Status / Tipo / Info
        let status = item.tipo || item.status || item.info || 'Indefinido';
        if (item.tipo && item.info) {
            status = `${item.tipo} (${item.info})`;
        }

        // Correção InfoSec (XSS armazenado): montamos os nós via DOM/textContent
        // em vez de innerHTML, já que os valores vêm de dados cadastrados por usuários.
        const div = document.createElement('div');
        div.className = 'card-item';

        const linha1 = document.createElement('div');
        const strong = document.createElement('strong');
        strong.textContent = patrimonio;
        linha1.appendChild(strong);
        linha1.appendChild(document.createTextNode(` - ${colaborador}`));

        const linha2 = document.createElement('small');
        linha2.textContent = `${unidade} | ${status}`;

        const wrapper = document.createElement('div');
        wrapper.appendChild(linha1);
        wrapper.appendChild(document.createElement('br'));
        wrapper.appendChild(linha2);

        div.appendChild(wrapper);
        container.appendChild(div);
    });
}

// Marcar Devolvido (PATCH com CSRF Token)
async function marcarDevolvido(id) {
    try {
        const response = await fetch(`/api/docs/${id}/status?status=DEVOLVIDO`, { 
            method: 'PATCH',
            headers: {
                'X-XSRF-TOKEN': getCookie('XSRF-TOKEN')
            },
            credentials: 'same-origin'
        });

        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const result = await response.json();

        if (result.sucesso) {
            atualizarDashboard();
        } else {
            mostrarToast(result.mensagem || "Erro ao atualizar status", 'err');
        }
    } catch (e) {
        mostrarToast("Erro de comunicação com motor", 'err');
    }
}

// Funções utilitárias auxiliares
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return '';
}

function mostrarToast(msg, tipo = 'ok') {
    if (typeof window.toast === 'function') {
        window.toast(msg, tipo);
        return;
    }
    const toastEl = document.getElementById("toast");
    if (!toastEl) return;
    toastEl.textContent = msg;
    toastEl.className = `toast show ${tipo}`;
    setTimeout(() => { toastEl.className = "toast"; }, 3000);
}

// Exportação global de funções
window.carregarDados = atualizarDashboard;
window.atualizarDashboard = atualizarDashboard;
window.marcarDevolvido = marcarDevolvido;