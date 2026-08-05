/**
 * AutoDocs - Frontend Architecture
 * Gerenciador de Estado das Views, Dashboards/Gráficos, Inventário e Termos.
 */

// Instâncias Globais dos Gráficos para Limpeza e Redesenho
let officeChartInstance = null;
let equipmentChartInstance = null;

// --- 1. NAVEGAÇÃO SPA E MENU MOBILE ---
document.addEventListener('DOMContentLoaded', () => {
    const navItems = document.querySelectorAll('.nav-item');
    const appSections = document.querySelectorAll('.app-section');
    const sidebar = document.getElementById('sidebar');
    const menuToggle = document.getElementById('menu-toggle');
    const closeMenu = document.getElementById('close-menu');

    // Alternância de Seções na Barra Lateral
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

            // Se navegou para o Dashboard, renderiza/atualiza os gráficos
            if (targetId === 'section-dashboard') {
                loadDashboardCharts();
            }

            // No mobile, recolhe a barra lateral ao escolher uma opção
            if (window.innerWidth <= 768 && sidebar) {
                sidebar.classList.remove('active');
            }
        });
    });

    // Abrir/Fechar Menu no Mobile
    menuToggle?.addEventListener('click', () => sidebar?.classList.add('active'));
    closeMenu?.addEventListener('click', () => sidebar?.classList.remove('active'));

    // Carrega o estado inicial do Dashboard
    loadDashboardCharts();
});

// --- 2. CONTROLE DE AUTENTICAÇÃO (LOGIN / REGISTRO / LOGOUT) ---
function switchAuthView(view) {
    const boxLogin = document.getElementById('box-login');
    const boxRegister = document.getElementById('box-register');

    if (view === 'register') {
        boxLogin.classList.add('d-none');
        boxRegister.classList.remove('d-none');
    } else {
        boxRegister.classList.add('d-none');
        boxLogin.classList.remove('d-none');
    }
}

document.getElementById('form-login')?.addEventListener('submit', (e) => {
    e.preventDefault();
    document.getElementById('view-auth').classList.add('d-none');
    document.getElementById('view-app').classList.remove('d-none');
    loadDashboardCharts();
});

function executeLogout() {
    closeModal('modal-logout');
    document.getElementById('view-app').classList.add('d-none');
    document.getElementById('view-auth').classList.remove('d-none');
}

// --- 3. RELATÓRIOS E GRÁFICOS (CHART.JS - ESTADO INICIAL VAZIO) ---
async function loadDashboardCharts() {
    if (typeof Chart === 'undefined') return;

    Chart.defaults.color = '#9ca3af';
    Chart.defaults.font.family = "'Inter', system-ui, sans-serif";

    // Estrutura inicial sem dados mocados (pronta para receber resposta REST)
    let officeLabels = [];
    let officeValues = [];
    let equipmentLabels = [];
    let equipmentValues = [];

    try {
        // Exemplo de integração futura com endpoint real de métricas
        const response = await fetch('/api/dashboard/metrics');
        if (response.ok) {
            const data = await response.json();
            officeLabels = data.offices?.map(o => o.nome) || [];
            officeValues = data.offices?.map(o => o.qtd) || [];
            equipmentLabels = data.equipments?.map(e => e.categoria) || [];
            equipmentValues = data.equipments?.map(e => e.qtd) || [];

            document.getElementById('kpi-total-borrowed').innerText = data.totalEmprestimos || 0;
            document.getElementById('kpi-total-offices').innerText = data.totalEscritorios || 0;
            document.getElementById('kpi-total-types').innerText = data.totalTipos || 0;
            document.getElementById('kpi-expiring-soon').innerText = data.aVencer || 0;
        }
    } catch (e) {
        // Fallback para exibição limpa caso a API ainda não esteja respondendo
    }

    // Renderiza Gráfico 1 (Barras)
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

    // Renderiza Gráfico 2 (Doughnut)
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
function validateTermForm() {
    const device = document.getElementById('term-device')?.value.trim();
    const user = document.getElementById('term-user')?.value.trim();
    const dateStart = document.getElementById('term-date-start')?.value;
    const dateEnd = document.getElementById('term-date-end')?.value;
    const btn = document.getElementById('btn-generate-doc');

    if (!btn) return;

    const isDatesValid = dateStart && dateEnd && new Date(dateEnd) >= new Date(dateStart);
    btn.disabled = !(device && user && isDatesValid);
}

async function handleGenerateTerm(event) {
    event.preventDefault();

    const btn = document.getElementById('btn-generate-doc');
    const originalText = btn.innerHTML;

    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Processando documento...';

    const payload = {
        equipamento: document.getElementById('term-device').value.trim(),
        responsavel: document.getElementById('term-user').value.trim(),
        dataInicio: document.getElementById('term-date-start').value,
        dataFim: document.getElementById('term-date-end').value
    };

    try {
        const response = await fetch('/api/termos/gerar', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCsrfToken()
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            throw new Error(`Erro ${response.status}: Falha ao gerar o termo.`);
        }

        const blob = await response.blob();
        const downloadUrl = window.URL.createObjectURL(blob);
        
        const a = document.createElement('a');
        a.href = downloadUrl;
        a.download = `Termo_Responsabilidade_${payload.responsavel.replace(/\s+/g, '_')}.pdf`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        
        window.URL.revokeObjectURL(downloadUrl);

        showToast('Termo gerado e baixado com sucesso!', 'success');
        document.getElementById('form-generate-term').reset();
        validateTermForm();

    } catch (error) {
        console.error('Erro na geração:', error);
        showToast(error.message || 'Erro de comunicação com o servidor.', 'error');
    } finally {
        btn.disabled = false;
        btn.innerHTML = originalText;
    }
}

// --- 5. CADASTRO DE INVENTÁRIO (VIA SPRING REST) ---
async function handleSaveInventoryItem(event) {
    event.preventDefault();

    const btn = document.getElementById('btn-save-item');
    const originalText = btn.innerHTML;

    const payload = {
        patrimonio: document.getElementById('item-patrimonio').value.trim(),
        nome: document.getElementById('item-nome').value.trim(),
        localizacao: document.getElementById('item-localizacao').value.trim(),
        status: document.getElementById('item-status').value
    };

    btn.disabled = true;
    btn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Salvando...';

    try {
        const response = await fetch('/api/inventario', {
            method: 'POST',
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
        document.getElementById('form-inventory-item').reset();
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

    // Remove a mensagem de tabela vazia se existir
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
    const term = document.getElementById('inventory-search').value.toLowerCase();
    const rows = document.querySelectorAll('#inventory-table tbody tr');

    rows.forEach(row => {
        if (row.id === 'empty-inventory-row') return;
        const text = row.innerText.toLowerCase();
        row.style.display = text.includes(term) ? '' : 'none';
    });
}

// --- 6. UTILITÁRIOS: MODAIS, TOASTS & SEGURANÇA ---
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