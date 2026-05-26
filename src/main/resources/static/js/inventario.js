document.addEventListener("DOMContentLoaded", () => {
    const userRole = localStorage.getItem("user_role");
    if (!userRole) return;

    // Se for GESTOR, libera os controles de escrita
    if (userRole === "GESTOR") {
        const btnNovo = document.getElementById("btn-novo-item");
        if (btnNovo) btnNovo.style.display = "block";
        
        document.querySelectorAll(".th-acoes-inv").forEach(th => {
            th.style.display = "table-cell";
        });
    }

    carregarModuloInventario(userRole);
});

// GET - Busca e renderiza filtrando por Dell/HP/Outros
async function carregarModuloInventario(role) {
    try {
        const response = await fetch('/docs/listar-todos', {
            method: 'GET',
            credentials: 'same-origin'
        });

        if (!response.ok) return;
        const ativos = await response.json();
        
        const tbodyDell = document.getElementById("tabela-dell-body");
        const tbodyHp = document.getElementById("tabela-hp-body");
        const tbodyOutros = document.getElementById("tabela-outros-body");

        if(tbodyDell) tbodyDell.innerHTML = "";
        if(tbodyHp) tbodyHp.innerHTML = "";
        if(tbodyOutros) tbodyOutros.innerHTML = "";

        ativos.forEach(item => {
            const tr = document.createElement("tr");
            let classeSit = "sit-disponivel";
            if (item.situacao === "Manutenção") classeSit = "sit-manutencao";
            if (item.situacao === "Alocado")    classeSit = "sit-alocado";

            let colunasHtml = `
                <td class="txt-mono-primary">${item.nomeEquipamento}</td>
                <td class="txt-mono-muted">${item.patrimonio}</td>
                <td><span class="badge-sit ${classeSit}">${item.situacao}</span></td>
            `;

            if (role === "GESTOR") {
                colunasHtml += `
                    <td style="text-align: right;">
                        <button class="btn-table-action" title="Editar" onclick="editarAtivo(${item.id})">✏️</button>
                        <button class="btn-table-action" title="Excluir" onclick="excluirAtivo(${item.id})">🗑️</button>
                    </td>
                `;
            } else {
                colunasHtml += `<td></td>`;
            }
            tr.innerHTML = colunasHtml;

            const nomeUpper = item.nomeEquipamento.toUpperCase();
            if (nomeUpper.includes("NOTEBOOK") || nomeUpper.includes("-NT-")) {
                if (nomeUpper.includes("DELL")) {
                    tbodyDell.appendChild(tr);
                } else if (nomeUpper.includes("HP")) {
                    tbodyHp.appendChild(tr);
                } else {
                    tbodyOutros.appendChild(tr);
                }
            } else {
                tbodyOutros.appendChild(tr); // Tablets, Celulares, HDs, Desktops...
            }
        });

        verificarTabelaVazia(tbodyDell, 4);
        verificarTabelaVazia(tbodyHp, 4);
        verificarTabelaVazia(tbodyOutros, 4);

    } catch (error) {
        console.error("[INVENTÁRIO] Erro ao carregar dados:", error);
    }
}

function verificarTabelaVazia(tbody, colspan) {
    if (tbody && tbody.children.length === 0) {
        tbody.innerHTML = `<tr><td colspan="${colspan}" style="text-align:center; color:var(--muted); padding:16px; font-size:12px;">Nenhum ativo registrado nesta categoria.</td></tr>`;
    }
}

// ── CONTROLE DA MODAL DE INSERÇÃO ──
window.abrirModalCadastro = function() {
    document.getElementById("modalCadastro").classList.add("active");
};

window.fecharModalCadastro = function() {
    document.getElementById("modalCadastro").classList.remove("active");
    document.getElementById("formCadastroAtivo").reset();
};

// POST - Envia o novo hardware para o backend controller do Spring
window.salvarNovoAtivo = async function(event) {
    event.preventDefault();
    
    const nomeEquipamento = document.getElementById("nomeEquipamento").value;
    const patrimonio = document.getElementById("patrimonio").value;
    const situacao = document.getElementById("situacao").value;

    const novoAtivo = { nomeEquipamento, patrimonio, situacao };

    try {
        const response = await fetch('/docs/cadastrar', { // Ajuste para o endpoint correto do seu Controller
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(novoAtivo),
            credentials: 'same-origin'
        });

        if (response.ok) {
            fecharModalCadastro();
            // Recarrega o módulo do inventário e força o script global do dashboard a atualizar as somas/cards
            carregarModuloInventario(localStorage.getItem("user_role"));
            if (typeof carregarDados === "function") carregarDados(); 
            mostrarToast("Ativo gravado com sucesso!");
        } else {
            alert("Erro ao salvar o item. Verifique os dados.");
        }
    } catch (error) {
        console.error("Erro na requisição POST:", error);
    }
};

function mostrarToast(msg) {
    const toast = document.getElementById("toast");
    if(!toast) return;
    toast.textContent = msg;
    toast.className = "toast show ok";
    setTimeout(() => { toast.className = "toast"; }, 3000);
}