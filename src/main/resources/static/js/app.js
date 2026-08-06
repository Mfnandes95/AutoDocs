/**
 * AutoDocs - Frontend Architecture
 * Gerenciador de Estado das Views, Dashboards/Gráficos, Inventário e Termos.
 */

let officeChartInstance = null;
let equipmentChartInstance = null;

// --- 1. NAVEGAÇÃO SPA E MENU MOBILE ---
document.addEventListener('DOMContentLoaded', () => {
    const navItems = document.querySelectorAll('.nav-item');
    const appSections = document.querySelectorAll('.app-section');
    const sidebar = document.getElementById('sidebar');
    const menuToggle = document.getElementById('menu-toggle');
    const closeMenu = document.getElementById('close-menu');

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

            if (targetId === 'section-dashboard') {
                loadDashboardCharts();
            }

            if (window.innerWidth <= 768 && sidebar) {
                sidebar.classList.remove('active');
            }
        });
    });

    menuToggle?.addEventListener('click', () => sidebar?.classList.add('active'));
    closeMenu?.addEventListener('click', () => sidebar?.classList.remove('active'));

    if (document.getElementById('view-app') && !document.getElementById('view-app').classList.contains('d-none')) {
        loadDashboardCharts();
    }
});

// --- 2. CONTROLE DE AUTENTICAÇÃO (LOGIN / REGISTRO / LOGOUT) ---
function switchAuthView(view) {
    const boxLogin = document.getElementById('box-login');
    const boxRegister = document.getElementById('box-register');

    if (view === 'register') {
        boxLogin?.classList.add('d-none');
        boxRegister?.classList.remove('d-none');
    } else {
        boxRegister?.classList.add('d-none');
        boxLogin?.classList.remove('d-none');
    }
}

// Submissão do Formulário de Login com captura tratada (.trim)
document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('form-login');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();

            const btn = loginForm.querySelector('button[type="submit"]');
            const originalText = btn ? btn.innerHTML : '';

            if (btn) {
                btn.disabled = true;
                btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Entrando...';
            }

            // Captura os elementos de input suportando diferentes convenções de ID/Name
            const emailInput = loginForm.querySelector('input[name="email"], input[name="username"], #login-email, #email');
            const passwordInput = loginForm.querySelector('input[name="password"], input[name="senha"], #login-password, #senha');

            // O e-mail pode ser trimado com segurança (o backend normaliza
            // e-mail da mesma forma no cadastro/login). A SENHA não deve ser
            // trimada aqui: o cadastro não trima a senha antes de gerar o
            // hash, então trimar só no login criaria uma senha "diferente"
            // da que foi de fato cadastrada sempre que ela tiver espaço
            // nas pontas — e o login falharia mesmo com a senha "correta".
            const emailValue = emailInput ? emailInput.value.trim() : '';
            const passwordValue = passwordInput ? passwordInput.value : '';

            // Monta os parâmetros exatamente com as chaves "email" e "password" configuradas no Spring Security
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
                    showToast('Login realizado com sucesso!', 'success');
                } else {
                    // O failureHandler do SecurityConfig devolve um JSON com a
                    // mensagem real do erro (e-mail não encontrado, senha
                    // incorreta, etc). Mostrar essa mensagem em vez de um
                    // texto fixo evita mascarar a causa real (ex: 403 de CSRF
                    // vs 401 de credencial inválida vs 500 do servidor).
                    let mensagem = 'Usuário ou senha inválidos.';
                    try {
                        const corpo = await response.json();
                        if (corpo?.mensagem) {
                            mensagem = response.status === 401
                                ? 'Usuário ou senha inválidos.'
                                : corpo.mensagem;
                        }
                    } catch (_) {
                        // resposta não era JSON (ex: erro 500 genérico) — mantém mensagem padrão
                    }
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
        });
    }
});

function executeLogout() {
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
}

// --- 3. RELATÓRIOS E GRÁFICOS (CHART.JS) ---
async function loadDashboardCharts() {
    if (typeof Chart === 'undefined') return;

    Chart.defaults.color = '#9ca3af';
    Chart.defaults.font.family = "'Inter', system-ui, sans-serif";

    let officeLabels = [];
    let officeValues = [];
    let equipmentLabels = [];
    let equipmentValues = [];

    try {
        const response = await fetch('/api/dashboard/metrics', {
            method: 'GET',
            credentials: 'include',
            headers: {
                'X-XSRF-TOKEN': getCsrfToken()
            }
        });
        if (response.ok) {
            const data = await response.json();
            officeLabels = data.offices?.map(o => o.nome) || [];
            officeValues = data.offices?.map(o => o.qtd) || [];
            equipmentLabels = data.equipments?.map(e => e.categoria) || [];
            equipmentValues = data.equipments?.map(e => e.qtd) || [];

            const totalBorrowed = document.getElementById('kpi-total-borrowed');
            const totalOffices = document.getElementById('kpi-total-offices');
            const totalTypes = document.getElementById('kpi-total-types');
            const expiringSoon = document.getElementById('kpi-expiring-soon');

            if (totalBorrowed) totalBorrowed.innerText = data.totalEmprestimos || 0;
            if (totalOffices) totalOffices.innerText = data.totalEscritorios || 0;
            if (totalTypes) totalTypes.innerText = data.totalTipos || 0;
            if (expiringSoon) expiringSoon.innerText = data.aVencer || 0;
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
                plugins: {
                    legend: { position: 'bottom', labels: { padding: 15, usePointStyle: true } }
                },
                cutout: '68%'
            }
        });
    }
}

// --- 4. GERADOR DE TERMOS DE RESPONSABILIDADE ---
function addTermItemRow() {
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
}

function removeTermItemRow(button) {
    const rows = document.querySelectorAll('.term-item-row');
    if (rows.length > 1) {
        button.closest('.term-item-row').remove();
        validateTermForm();
    } else {
        showToast('O termo deve conter ao menos 1 item.', 'info');
    }
}

function validateTermForm() {
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
        if (!patrimonio) {
            hasValidItems = false;
        }
    });

    const isDatesValid = dateStart && dateEnd && new Date(dateEnd) >= new Date(dateStart);
    btn.disabled = !(user && isDatesValid && hasValidItems && hasFile);
}

async function handleGenerateTerm(event) {
    if (event) event.preventDefault();

    const btn = document.getElementById('btn-generate-doc');
    const originalText = btn ? btn.innerHTML : '';

    const fileInput = document.getElementById('term-template');
    if (!fileInput || !fileInput.files[0]) {
        showToast('Selecione um arquivo modelo (.docx)', 'error');
        return;
    }

    if (btn) {
        btn.disabled = true;
        btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Gerando...';
    }

    const itemRows = document.querySelectorAll('.term-item-row');
    const itens = [];
    itemRows.forEach(row => {
        const patrimonio  = row.querySelector('.item-patrimonio')?.value.trim();
        const equipamento = row.querySelector('.item-equipamento')?.value.trim() || '';
        if (patrimonio) itens.push({ patrimonio, equipamento });
    });

    const payload = {
        nomeColaborador: document.getElementById('term-user')?.value.trim()       || '',
        dataInicio:      document.getElementById('term-date-start')?.value         || '',
        dataTermino:     document.getElementById('term-date-end')?.value           || '',
        unidade:         document.getElementById('term-unidade')?.value?.trim()    || '',
        tipo:            document.getElementById('term-tipo')?.value?.trim()       || '',
        info:            document.getElementById('term-info')?.value?.trim()       || '',
        itens: itens
    };

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    formData.append('dto', new Blob([JSON.stringify(payload)], { type: 'application/json' }));

    try {
        const response = await fetch('/api/termos/gerar', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'X-XSRF-TOKEN': getCsrfToken()
            },
            body: formData
        });

        const contentType = response.headers.get('content-type') || '';

        // SE O SERVIDOR RETORNAR HTML OU REDIRECIONAR (SESSÃO INVÁLIDA)
        if (response.redirected || contentType.includes('text/html')) {
            showToast('Sessão expirada ou não autorizada. Faça login novamente.', 'error');
            document.getElementById('view-app')?.classList.add('d-none');
            document.getElementById('view-auth')?.classList.remove('d-none');
            return;
        }

        if (!response.ok) {
            let errorMsg = 'Falha ao processar o documento.';
            try {
                const errorJson = await response.json();
                errorMsg = errorJson.mensagem || errorJson.message || errorMsg;
            } catch (_) {
                const rawText = await response.text();
                if (rawText) errorMsg = rawText;
            }
            throw new Error(errorMsg);
        }

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

    } catch (error) {
        console.error('Erro ao gerar termo:', error);
        showToast(error.message, 'error');
    } finally {
        if (btn) {
            btn.disabled = false;
            btn.innerHTML = originalText;
        }
    }
}

// --- 5. CADASTRO DE INVENTÁRIO ---
async function handleSaveInventoryItem(event) {
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

        const savedItem = response.headers.get('content-type')?.includes('application/json')
            ? await response.json()
            : payload;

        addInventoryRowToTable(savedItem);

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
}

function addInventoryRowToTable(item) {
    const tbody = document.getElementById('inventory-table-body');
    if (!tbody) return;

    const emptyRow = document.getElementById('empty-inventory-row');
    if (emptyRow) {
        emptyRow.remove();
    }

    const badgeClass = item.status === 'Ativo' ? 'online' : 'danger';
    const tr = document.createElement('tr');
    tr.innerHTML = `
        <td>${item.patrimonio}</td>
        <td>${item.nome}</td>
        <td>${item.localizacao}</td>
        <td><span class="badge ${badgeClass}">${item.status}</span></td>
        <td>
            <button class="action-btn edit" onclick="editItem('${item.patrimonio}')"><i class="fas fa-edit"></i></button>
            <button class="action-btn delete" onclick="openModal('modal-delete-item')"><i class="fas fa-trash"></i></button>
        </td>
    `;
    tbody.prepend(tr);
}

function filterInventory() {
    const searchInput = document.getElementById('inventory-search');
    if (!searchInput) return;
    const term = searchInput.value.toLowerCase();
    const rows = document.querySelectorAll('#inventory-table tbody tr');

    rows.forEach(row => {
        if (row.id === 'empty-inventory-row') return;
        const text = row.innerText.toLowerCase();
        row.style.display = text.includes(term) ? '' : 'none';
    });
}

// --- 6. UTILITÁRIOS ---
function openModal(modalId) {
    const modal = document.getElementById(modalId);
    const overlay = document.getElementById('overlay');
    if (modal && overlay) {
        modal.classList.add('active');
        overlay.classList.add('active');
    }
}

function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    const overlay = document.getElementById('overlay');
    if (modal && overlay) {
        modal.classList.remove('active');
        overlay.classList.remove('active');
    }
}

document.getElementById('overlay')?.addEventListener('click', () => {
    document.querySelectorAll('.modal.active').forEach(modal => {
        closeModal(modal.id);
    });
});

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    if (!container) return;

    const toast = document.createElement('div');
    toast.className = `toast toast-${type}`;
    toast.innerText = message;

    container.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

function getCsrfToken() {
    const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
    return match ? decodeURIComponent(match[1]) : '';
}