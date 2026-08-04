document.addEventListener("DOMContentLoaded", () => {
    const userRole = localStorage.getItem("user_role");

    if (userRole === "GESTOR") {
        const btnNovo = document.getElementById("btn-novo-item");
        if (btnNovo) btnNovo.style.display = "block";

        document.querySelectorAll(".th-acoes-inv").forEach(th => {
            th.style.display = "table-cell";
        });
    }

    carregarModuloInventario(userRole);
});

async function carregarModuloInventario(role) {
    const tbodyDell   = document.getElementById("tabela-dell-body");
    const tbodyHp     = document.getElementById("tabela-hp-body");
    const tbodyOutros = document.getElementById("tabela-outros-body");

    try {
        const response = await fetch('/equipamentos', {
            method: 'GET',
            credentials: 'same-origin'
        });

        if (!response.ok) {
            throw new Error(`Erro HTTP: ${response.status}`);
        }

        const ativos = await response.json();

        if (tbodyDell)   tbodyDell.innerHTML   = "";
        if (tbodyHp)     tbodyHp.innerHTML     = "";
        if (tbodyOutros) tbodyOutros.innerHTML = "";

        if (Array.isArray(ativos)) {
            ativos.forEach(item => {
                const tr = document.createElement("tr");
                const nome       = item.nome       || "SEM NOME";
                const patrimonio = item.patrimonio || "SEM PATRIMÔNIO";
                const status     = item.status     || "Indefinido";

                let classeSit = "sit-disponivel";
                if (status.toUpperCase() === "INATIVO")   classeSit = "sit-manutencao";
                if (status.toUpperCase() === "AVARIADO")  classeSit = "sit-alocado";

                let colunasHtml = `
                    <td class="txt-mono-primary">${nome}</td>
                    <td class="txt-mono-muted">${patrimonio}</td>
                    <td><span class="badge-sit ${classeSit}">${status}</span></td>
                `;

                if (role === "GESTOR") {
                    colunasHtml += `
                        <td style="text-align:right;">
                            <button class="btn-table-action" title="Editar" onclick="editarAtivo(${item.id})">✏️</button>
                            <button class="btn-table-action" title="Excluir" onclick="excluirAtivo(${item.id})">🗑️</button>
                        </td>`;
                } else {
                    colunasHtml += `<td></td>`;
                }

                tr.innerHTML = colunasHtml;

                const nomeUpper = nome.toUpperCase();
                if (nomeUpper.includes("DELL")) {
                    tbodyDell?.appendChild(tr);
                } else if (nomeUpper.includes("HP")) {
                    tbodyHp?.appendChild(tr);
                } else {
                    tbodyOutros?.appendChild(tr);
                }
            });
        }

    } catch (error) {
        console.error("[INVENTÁRIO] Erro ao carregar dados:", error);
        mostrarToast("Erro ao carregar lista de inventário.", "err");
    } finally {
        // Preenche com a mensagem de "Nenhum ativo registrado" caso as tabelas estejam vazias
        verificarTabelaVazia(tbodyDell, 4);
        verificarTabelaVazia(tbodyHp, 4);
        verificarTabelaVazia(tbodyOutros, 4);

        // Oculta indicador de carregamento
        const spinner = document.getElementById("loading") || document.querySelector(".loading-spinner");
        if (spinner) spinner.style.display = "none";
    }
}

function verificarTabelaVazia(tbody, colspan) {
    if (tbody && tbody.children.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="${colspan}" style="text-align:center;color:var(--muted, #888);padding:16px;font-size:12px;">
                    Nenhum ativo registrado nesta categoria.
                </td>
            </tr>`;
    }
}

window.abrirModalCadastro = function () {
    document.getElementById("modalCadastro")?.classList.add("active");
};

window.fecharModalCadastro = function () {
    document.getElementById("modalCadastro")?.classList.remove("active");
    document.getElementById("formCadastroAtivo")?.reset();
};

// POST - Salvar com CSRF Token
window.salvarNovoAtivo = async function (event) {
    event.preventDefault();

    const nome       = document.getElementById("nomeEquipamento")?.value.trim();
    const patrimonio = document.getElementById("patrimonio")?.value.trim();
    const status     = document.getElementById("situacao")?.value;

    if (!nome) {
        mostrarToast("Informe o nome do equipamento.", "err");
        return;
    }

    const novoAtivo = { nome, patrimonio, tipo: "Hardware", status };

    try {
        const response = await fetch('/equipamentos', {
            method: 'POST',
            headers: { 
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCookie('XSRF-TOKEN')
            },
            credentials: 'same-origin',
            body: JSON.stringify(novoAtivo)
        });

        if (response.ok) {
            fecharModalCadastro();
            await carregarModuloInventario(localStorage.getItem("user_role"));
            mostrarToast("Ativo gravado com sucesso!");
        } else {
            mostrarToast("Erro ao salvar o item.", "err");
        }
    } catch (error) {
        mostrarToast("Erro de conexão.", "err");
    }
};

// PUT - Editar com CSRF Token
window.editarAtivo = async function (id) {
    const nome       = prompt("Novo nome do equipamento:");
    const patrimonio = prompt("Novo patrimônio:");
    const status     = prompt("Novo status (ATIVO / INATIVO / AVARIADO):");

    if (!nome && !patrimonio && !status) return;

    try {
        const response = await fetch(`/equipamentos/${id}`, {
            method: 'PUT',
            headers: { 
                'Content-Type': 'application/json',
                'X-XSRF-TOKEN': getCookie('XSRF-TOKEN')
            },
            credentials: 'same-origin',
            body: JSON.stringify({ nome, patrimonio, status })
        });

        if (response.ok) {
            await carregarModuloInventario(localStorage.getItem("user_role"));
            mostrarToast("Ativo atualizado!");
        } else {
            mostrarToast("Erro ao atualizar.", "err");
        }
    } catch (error) {
        mostrarToast("Erro de conexão.", "err");
    }
};

// DELETE - Excluir com CSRF Token
window.excluirAtivo = async function (id) {
    if (!confirm("Excluir este equipamento?")) return;

    try {
        const response = await fetch(`/equipamentos/${id}`, {
            method: 'DELETE',
            headers: {
                'X-XSRF-TOKEN': getCookie('XSRF-TOKEN')
            },
            credentials: 'same-origin'
        });

        if (response.ok) {
            await carregarModuloInventario(localStorage.getItem("user_role"));
            mostrarToast("Ativo excluído.");
        } else {
            mostrarToast("Erro ao excluir.", "err");
        }
    } catch (error) {
        mostrarToast("Erro de conexão.", "err");
    }
};

function mostrarToast(msg, tipo = "ok") {
    const toast = document.getElementById("toast");
    if (!toast) return;
    toast.textContent = msg;
    toast.className = `toast show ${tipo}`;
    setTimeout(() => { toast.className = "toast"; }, 3000);
}

function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return '';
}