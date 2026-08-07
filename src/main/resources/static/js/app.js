/**
 * AutoDocs - Frontend Architecture
 * Gerenciador de Estado das Views, Dashboards/Gráficos, Inventário e Termos.
 */

let officeChartInstance = null;
let equipmentChartInstance = null;

// --- 1. NAVEGAÇÃO SPA, MENU MOBILE E INICIALIZAÇÃO ---
document.addEventListener('DOMContentLoaded', () => {
    const navItems = document.querySelectorAll('.nav-item');
    const appSections = document.querySelectorAll('.app-section');
    const sidebar = document.getElementById('sidebar');
    const menuToggle = document.getElementById('menu-toggle');
    const closeMenu = document.getElementById('close-menu');

    // Navegação entre seções
    navItems.forEach(item => {
        item.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = item.getAttribute('data-target');

            navItems.forEach(nav => nav.classList.remove('active'));
            item.classList.add('active');

            appSections.forEach(section => {
                if (section.id === targetId) {
                    section.classList.remove('d-none');
                } else {
                    section.classList.add('d-none');
                }
            });

            // Dispara requisições de acordo com a aba visível
            if (targetId === 'section-dashboard') {
                loadDashboardCharts();
            } else if (targetId === 'section-inventory' || targetId === 'section-inventario') {
                loadInventoryList();
            }

            if (window.innerWidth <= 768 && sidebar) {
                sidebar.classList.remove('active');
            }
        });
    });

    menuToggle?.addEventListener('click', () => sidebar?.classList.add('active'));
    closeMenu?.addEventListener('click', () => sidebar?.classList.remove('active'));

    // Vinculação automática do input de importação de planilha
    const importInput = document.getElementById('file-inventario') || document.getElementById('inventory-import-input');
    if (importInput) {
        importInput.addEventListener('change', handleImportInventory);
    }

    // Formulário de Login
    const loginForm = document.getElementById('form-login');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLoginSubmit);
    }

    // Inicialização da View Principal (caso já carregue autenticado)
    if (document.getElementById('view-app') && !document.getElementById('view-app').classList.contains('d-none')) {
        loadDashboardCharts();
        loadInventoryList();
    }
});

// --- 2. CONTROLE DE AUTENTICAÇÃO (LOGIN / REGISTRO / LOGOUT) ---
window.switchAuthView = function(view) {
    const boxLogin = document.getElementById('box-login');
    const boxRegister = document.getElementById('box-register');

    if (view === 'register') {
        boxLogin?.classList.add('d-none');
        boxRegister?.classList.remove('d-none');
    } else {
        boxRegister?.classList.add('d-none');
        boxLogin?.classList.remove('d-none');
    }
};

async function handleLoginSubmit(e) {
    e.preventDefault();
    const loginForm = e.target;

    const btn = loginForm.querySelector('button[type="submit"]');
    const originalText = btn ? btn.innerHTML : '';

    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Entrando...';
    }

    const emailInput = loginForm.querySelector('input[name="email"], #login-email');
    const passwordInput = loginForm.querySelector('input[name="password"], #login-password');

    const emailValue = emailInput ? emailInput.value.trim() : '';
    const passwordValue = passwordInput ? passwordInput.value : '';

    const bodyParams = new URLSearchParams();
    bodyParams.append('email', emailValue);
    bodyParams.append('password', passwordValue);

    try {
        const response = await fetch('/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                'X-XSRF-TOKEN': getCsrfToken()
            },
            credentials: 'include',
            body: bodyParams
        });

        if (response.ok || response.status === 200) {
            document.getElementById('view-auth')?.classList.add('d-none');
            document.getElementById('view-app')?.classList.remove('d-none');
            
            loadDashboardCharts();
            loadInventoryList();
            showToast('Login realizado com sucesso!', 'success');
        } else {
            let mensagem = 'Usuário ou senha inválidos.';
            try {
                const corpo = await response.json();
                if (corpo?.mensagem) mensagem = corpo.mensagem;
            } catch (_) {}
            showToast(mensagem, 'error');
        }
    } catch (error) {
        console.error('Erro na requisição de login:', error);
        showToast('Falha na comunicação com o servidor.', 'error');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = originalText;
        }
    }
}

window.executeLogout = function() {
    closeModal('modal-logout');
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/logout';

    const csrf = document.createElement('input');
    csrf.type = 'hidden';
    csrf.name = '_csrf';
    csrf.value = getCsrfToken();
    form.appendChild(csrf);

    document.body.appendChild(form);
    form.submit();
};

// --- 3. RELATÓRIOS E GRÁFICOS (CHART.JS) ---
window.loadDashboardCharts = async function() {
    if (typeof Chart === 'undefined') return;

    Chart.defaults.color = '#9ca3af';
    Chart.defaults.font.family = "'Inter', system-ui, sans-serif";

    let officeLabels = [];
    let officeValues = [];
    let equipmentLabels = [];
    let equipmentValues = [];

    try {
        const response = await fetch('/api/termos/dashboard', {
            method: 'GET',
            credentials: 'include',
            headers: { 'X-XSRF-TOKEN': getCsrfToken() }
        });

        if (response.ok) {
            const envelope = await response.json();
            const data = envelope?.dados || envelope || {};

            officeLabels = Object.keys(data.porUnidade || {});
            officeValues = Object.values(data.porUnidade || {});
            equipmentLabels = Object.keys(data.porTipo || {});
            equipmentValues = Object.values(data.porTipo || {});

            const totalBorrowed = document.getElementById('kpi-total-borrowed');
            const totalOffices = document.getElementById('kpi-total-offices');
            const totalTypes = document.getElementById('kpi-total-types');
            const expiringSoon = document.getElementById('kpi-expiring-soon');

            if (totalBorrowed) totalBorrowed.innerText = data.estatisticasGerais?.totalTermosGerados || data.totalBorrowings || 0;
            if (totalOffices) totalOffices.innerText = officeLabels.length;
            if (totalTypes) totalTypes.innerText = equipmentLabels.length;
            if (expiringSoon) expiringSoon.innerText = data.totalAVencer || data.expiringSoon || 0;
        }
    } catch (e) {
        console.warn('Erro ao carregar métricas do dashboard:', e);
    }

    const ctxOffice = document.getElementById('chartOffices');
    if (ctxOffice) {
        if (officeChartInstance) officeChartInstance.destroy();
        officeChartInstance = new Chart(ctxOffice, {
            type: 'bar',
            data: {
                labels: officeLabels.length ? officeLabels : ['Sem dados'],
                datasets: [{
                    label: 'Ativos Alocados',
                    data: officeValues.length ? officeValues : [0],
                    backgroundColor: 'rgba(16, 185, 129, 0.75)',
                    borderColor: '#10b981',
                    borderWidth: 1,
                    borderRadius: 6
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    x: { grid: { color: 'rgba(255, 255, 255, 0.05)' } },
                    y: { beginAtZero: true, grid: { color: 'rgba(255, 255, 255, 0.05)' }, ticks: { stepSize: 1 } }
                }
            }
        });
    }

    const ctxEquipment = document.getElementById('chartEquipment');
    if (ctxEquipment) {
        if (equipmentChartInstance) equipmentChartInstance.destroy();
        equipmentChartInstance = new Chart(ctxEquipment, {
            type: 'doughnut',
            data: {
                labels: equipmentLabels.length ? equipmentLabels : ['Sem registros'],
                datasets: [{
                    data: equipmentValues.length ? equipmentValues : [1],
                    backgroundColor: equipmentValues.length 
                        ? ['#10b981', '#3b82f6', '#f59e0b', '#8b5cf6'] 
                        : ['rgba(255, 255, 255, 0.1)'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { position: 'bottom', labels: { padding: 15, usePointStyle: true } } },
                cutout: '68%'
            }
        });
    }
};

// --- 4. GERADOR DE TERMOS DE RESPONSABILIDADE ---
window.addTermItemRow = function() {
    const listContainer = document.getElementById('term-items-list');
    if (!listContainer) return;

    const row = document.createElement('div');
    row.className = 'term-item-row form-row align-items-center mb-2';
    row.style.cssText = 'display: flex; gap: 10px; margin-bottom: 8px;';
    row.innerHTML = `
        <div class="form-group flex-grow-1 mb-0" style="flex: 1;">
            <input type="text" class="form-control item-patrimonio" placeholder="Nº Patrimônio / Tag" required oninput="validateTermForm()">
        </div>
        <div class="form-group flex-grow-1 mb-0" style="flex: 1;">
            <input type="text" class="form-control item-equipamento" placeholder="Nome / Descrição do equipamento" oninput="validateTermForm()">
        </div>
        <button type="button" class="action-btn delete btn-remove-item" onclick="removeTermItemRow(this)" title="Remover item">
            <i class="fas fa-trash"></i>
        </button>
    `;

    listContainer.appendChild(row);
    validateTermForm();
};

window.removeTermItemRow = function(button) {
    const rows = document.querySelectorAll('.term-item-row');
    if (rows.length > 1) {
        button.closest('.term-item-row').remove();
        validateTermForm();
    } else {
        showToast('O termo deve conter ao menos 1 item.', 'info');
    }
};

window.validateTermForm = function() {
    const user = document.getElementById('term-user')?.value.trim();
    const dateStart = document.getElementById('term-date-start')?.value;
    const dateEnd = document.getElementById('term-date-end')?.value;
    const templateFileInput = document.getElementById('term-template');
    const btn = document.getElementById('btn-generate-doc');

    if (!btn) return;

    const hasFile = templateFileInput && templateFileInput.files.length > 0;
    const itemRows = document.querySelectorAll('.term-item-row');
    let hasValidItems = itemRows.length > 0;

    itemRows.forEach(row => {
        const patrimonio = row.querySelector('.item-patrimonio')?.value.trim();
        if (!patrimonio) hasValidItems = false;
    });

    const isDatesValid = dateStart && dateEnd && new Date(dateEnd) >= new Date(dateStart);
    btn.disabled = !(user && isDatesValid && hasValidItems && hasFile);
};

window.handleGenerateTerm = async function(event) {
    if (event) event.preventDefault();

    const btn = document.getElementById('btn-generate-doc');
    const fileInput = document.getElementById('term-template');

    if (!fileInput || !fileInput.files[0]) {
        showToast('Selecione um arquivo modelo (.docx)', 'error');
        return;
    }

    const itemRows = document.querySelectorAll('.term-item-row');
    const itens = [];
    itemRows.forEach(row => {
        const patrimonio  = row.querySelector('.item-patrimonio')?.value.trim();
        const equipamento = row.querySelector('.item-equipamento')?.value.trim() || '';
        if (patrimonio) itens.push({ patrimonio, equipamento });
    });

    // Mapeamento perfeitamente alinhado com o DTO do backend (incluindo nomeColaborador)
    const payload = {
        nomeColaborador: document.getElementById('term-user')?.value.trim()    || '',
        colaborador:     document.getElementById('term-user')?.value.trim()    || '',
        dataInicio:      document.getElementById('term-date-start')?.value     || '',
        dataTermino:     document.getElementById('term-date-end')?.value       || '',
        unidade:         document.getElementById('term-unidade')?.value?.trim() || '',
        tipo:            document.getElementById('term-tipo')?.value?.trim()    || '',
        observacoes:     document.getElementById('term-info')?.value?.trim()   || '',
        itens: itens
    };

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    formData.append('dto', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    try {
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Gerando...';
        }

        const response = await fetch('/api/termos/gerar', {
            method: 'POST',
            credentials: 'include',
            headers: { 'X-XSRF-TOKEN': getCsrfToken() },
            body: formData
        });

        // Caso a resposta não seja 2xx, tenta ler a mensagem retornada no JSON
        if (!response.ok) {
            const errorJson = await response.json().catch(() => null);
            const errorMsg = errorJson?.mensagem || errorJson?.message || `Erro ${response.status} ao processar o documento.`;
            throw new Error(errorMsg);
        }

        // Se deu certo, obtém o arquivo como Blob e faz o download
        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);

        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = 'Termo_Responsabilidade.docx';
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(downloadUrl);

        showToast('Termo gerado com sucesso!', 'success');
        if (typeof loadDashboardCharts === 'function') loadDashboardCharts();

    } catch (error) {
        console.error('Erro ao gerar termo:', error);
        showToast(error.message, 'error');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = '<i class="fas fa-file-word"></i> Gerar Documento';
        }
    }
};

// --- 5. CADASTRO, EXCLUSÃO E IMPORTAÇÃO DE INVENTÁRIO ---

window.loadInventoryList = async function() {
    try {
        const response = await fetch('/api/inventario', {
            method: 'GET',
            credentials: 'include',
            headers: { 'X-XSRF-TOKEN': getCsrfToken() }
        });

        if (!response.ok) return;

        const data = await response.json();
        const itens = Array.isArray(data) ? data : (data.dados || []);
        const tbody = document.getElementById('inventory-table-body');
        if (!tbody) return;

        tbody.innerHTML = '';
        if (!itens || itens.length === 0) {
            tbody.innerHTML = '<tr id="empty-inventory-row"><td colspan="5" style="text-align:center;">Nenhum ativo cadastrado.</td></tr>';
            return;
        }

        // Adiciona preservando a ordem vinda da consulta do banco
        itens.forEach(item => addInventoryRowToTable(item, true));
    } catch (e) {
        console.warn('Erro ao carregar lista de inventário:', e);
    }
};

window.handleSaveInventoryItem = async function(event) {
    if (event) event.preventDefault();

    const btn = document.getElementById('btn-save-item');
    if (!btn) return;

    const payload = {
        patrimonio: document.getElementById('item-patrimonio')?.value.trim() || '',
        nome: document.getElementById('item-nome')?.value.trim() || '',
        localizacao: document.getElementById('item-localizacao')?.value.trim() || '',
        status: document.getElementById('item-status')?.value || 'Ativo'
    };

    const originalText = btn.innerHTML;
    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Salvando...';

    try {
        const response = await fetch('/api/inventario', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken()
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(`Erro ${response.status}: Não foi possível salvar o ativo.`);
        }

        const savedItem = await response.json().catch(() => payload);

        addInventoryRowToTable(savedItem, false); // Insere no topo da tabela
        showToast('Ativo cadastrado com sucesso!', 'success');
        document.getElementById('form-inventory-item')?.reset();
        closeModal('modal-item');

    } catch (error) {
        console.error('Erro ao salvar item no inventário:', error);
        showToast(error.message || 'Falha na comunicação com o servidor.', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
};

window.handleImportInventory = async function(event) {
    const input = event?.target || document.getElementById('file-inventario') || document.getElementById('inventory-import-input');
    const arquivo = input?.files?.[0];
    if (!arquivo) return;

    const formData = new FormData();
    formData.append('file', arquivo);

    showToast('Importando planilha...', 'info');

    try {
        const response = await fetch('/api/inventario/importar', {
            method: 'POST',
            credentials: 'include',
            headers: { 'X-XSRF-TOKEN': getCsrfToken() },
            body: formData
        });

        const json = await response.json().catch(() => null);

        if (!response.ok || (json && json.sucesso === false)) {
            const erroMsg = json?.mensagem || `Erro ${response.status} ao importar a planilha.`;
            throw new Error(erroMsg);
        }

        const mensagemSucesso = json?.mensagem || 'Planilha importada com sucesso!';
        showToast(mensagemSucesso, 'success');

        await loadInventoryList();

    } catch (error) {
        console.error('Erro ao importar planilha de inventário:', error);
        showToast(error.message || 'Falha ao importar a planilha.', 'error');
    } finally {
        if (input) input.value = '';
    }
};

window.deleteInventoryItem = async function(id) {
    if (!id || id === 'undefined') {
        showToast('Identificador do item inválido.', 'error');
        return;
    }

    if (!confirm(`Deseja realmente remover o ativo? (ID/Patrimônio: ${id})`)) {
        return;
    }

    try {
        const response = await fetch(`/api/inventario/${id}`, {
            method: 'DELETE',
            credentials: 'include',
            headers: { 'X-XSRF-TOKEN': getCsrfToken() }
        });

        if (!response.ok) {
            throw new Error(`Erro ${response.status}: Não foi possível remover o ativo.`);
        }

        showToast('Ativo removido com sucesso!', 'success');
        await loadInventoryList();

    } catch (error) {
        console.error('Erro ao excluir item do inventário:', error);
        showToast(error.message || 'Falha na comunicação com o servidor.', 'error');
    }
};

window.editItem = function(patrimonio) {
    if (!patrimonio) return;
    openModal('modal-item');
    const inputPatrimonio = document.getElementById('item-patrimonio');
    if (inputPatrimonio) inputPatrimonio.value = patrimonio;
};

function addInventoryRowToTable(item, append = false) {
    const tbody = document.getElementById('inventory-table-body');
    if (!tbody) return;

    const emptyRow = document.getElementById('empty-inventory-row');
    if (emptyRow) emptyRow.remove();

    const status = item.status || 'Ativo';
    const badgeClass = status.toUpperCase() === 'ATIVO' ? 'online' : 'danger';
    const targetId = item.id || item.patrimonio;

    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td><strong>${item.patrimonio || ''}</strong></td>
        <td>${item.nome || item.equipamento || ''}</td>
        <td>${item.localizacao || item.unidade || ''}</td>
        <td><span class="badge ${badgeClass}">${status}</span></td>
        <td>
            <button class="action-btn edit" onclick="editItem('${item.patrimonio}')" title="Editar"><i class="fas fa-edit"></i></button>
            <button class="action-btn delete" onclick="deleteInventoryItem('${targetId}')" title="Excluir"><i class="fas fa-trash"></i></button>
        </td>
    `;

    if (append) {
        tbody.appendChild(tr);
    } else {
        tbody.prepend(tr);
    }
}

window.filterInventory = function() {
    const searchInput = document.getElementById('inventory-search');
    if (!searchInput) return;
    const term = searchInput.value.toLowerCase();
    const rows = document.querySelectorAll('#inventory-table-body tr');

    rows.forEach(row => {
        if (row.id === 'empty-inventory-row') return;
        const text = row.innerText.toLowerCase();
        row.style.display = text.includes(term) ? '' : 'none';
    });
};

// --- 6. UTILITÁRIOS E HELPERS ---
window.openModal = function(modalId) {
    const modal = document.getElementById(modalId);
    const overlay = document.getElementById('overlay');
    if (modal && overlay) {
        modal.classList.add('active');
        overlay.classList.add('active');
    }
};

window.closeModal = function(modalId) {
    const modal = document.getElementById(modalId);
    const overlay = document.getElementById('overlay');
    if (modal && overlay) {
        modal.classList.remove('active');
        overlay.classList.remove('active');
    }
};

document.getElementById('overlay')?.addEventListener('click', () => {
    document.querySelectorAll('.modal.active').forEach(modal => {
        closeModal(modal.id);
    });
});

window.showToast = function(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerText = message;

    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
};

function getCsrfToken() {
    const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : '';
}