// ── NAVEGAÇÃO ──
function show(id, el) {
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.getElementById(id).classList.add('active');
    document.querySelectorAll('nav li').forEach(li => li.classList.remove('active'));
    if (el) el.classList.add('active');
    if (id === 'dashboard') atualizarDashboard();
}

// ── TOAST ──
function toast(msg, tipo = 'ok') {
    const t = document.getElementById('toast');
    t.className = `toast ${tipo}`;
    t.innerHTML = (tipo === 'ok' ? '✅' : '❌') + ' ' + msg;
    t.classList.add('show');
    setTimeout(() => t.classList.remove('show'), 3500);
}

// ── SPINNER ──
function setLoading(on) {
    const btn = document.getElementById('btnGerar');
    const sp  = document.getElementById('spinnerGerar');
    const tx  = document.getElementById('btnGerarText');
    btn.disabled       = on;
    sp.style.display   = on ? 'block' : 'none';
    tx.textContent     = on ? 'Processando...' : '⬇ Gerar e Baixar Documento';
}

// ── GERAR DOCUMENTO ──
async function gerarDinamico() {
    const fileInput = document.getElementById('fileInput');
    const file = fileInput.files[0];
   
    if (!file) return toast('Selecione o arquivo .docx', 'error');

    // Coleta os dados do formulário
    const dadosObjeto = {
        nome_colaborador: document.getElementById('nome').value,
        patrimonio: document.getElementById('patrimonioInput').value,
        unidade: document.getElementById('unidade').value,
        info: document.getElementById('info').value,
        tipo: document.getElementById('tipo').value,
        data_inicio: document.getElementById('dataInicio').value,
        data_termino: document.getElementById('dataTermino').value
    };

    const formData = new FormData();
    // A chave "file" deve ser idêntica ao @RequestPart("file") no Java
    formData.append('file', file);
   
    // A chave "dados" deve ser idêntica ao @RequestPart("dados") no Java
    // Criamos um Blob com o tipo application/json para o Spring não se perder
    const jsonBlob = new Blob([JSON.stringify(dadosObjeto)], { type: 'application/json' });
    formData.append('dados', jsonBlob);

    try {
        // IMPORTANTE: Use a barra "/" no início para a URL ser absoluta (localhost:8080/docs/...)
        const response = await fetch('/docs/gerar-dinamico', {
            method: 'POST',
            body: formData
            // DICA: NÃO coloque Headers de Content-Type aqui. O browser faz isso sozinho para Multipart.
        });

        if (response.ok) {
            const blob = await response.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            // Define o nome do arquivo baseado no colaborador
            const nomeLimpo = dadosObjeto.nome_colaborador.replace(/\s+/g, '_');
            a.download = `Termo_${nomeLimpo}.docx`;
            document.body.appendChild(a);
            a.click();
            window.URL.revokeObjectURL(url);
            toast('Documento gerado com sucesso!');
        } else if (response.status === 404) {
            toast('Erro 404: Endpoint não encontrado. Verifique se o Controller está ativo.', 'error');
        } else {
            toast('Erro no servidor ao gerar documento.', 'error');
        }
    } catch (e) {
        toast('Erro de conexão com o servidor.', 'error');
        console.error(e);
    }
}

// ── BUSCAR APARELHO ──
async function buscarAparelho() {
    const patrimonio = document.getElementById('searchPatrimonio').value.trim();
    const div        = document.getElementById('resultadoDaBusca');

    if (!patrimonio) return toast('Digite um número de patrimônio.', 'err');

    div.className = 'result-box visible';
    div.innerHTML = '<span style="color:var(--muted);font-size:13px;">Buscando...</span>';

    try {
        const lista = await (await fetch('/docs/listar-todos')).json();
        const item  = lista.find(t => t.patrimonio === patrimonio);

        if (item) {
            div.className = 'result-box visible ok';
            div.innerHTML = `
                <div class="result-row"><span>Colaborador</span><span>${item.nomeColaborador ?? '—'}</span></div>
                <div class="result-row"><span>Unidade</span><span>${item.unidade ?? '—'}</span></div>
                <div class="result-row"><span>Tipo</span><span>${item.tipo ?? '—'}</span></div>
                <div class="result-row"><span>Equipamento</span><span>${item.info ?? '—'}</span></div>
                <div class="result-row"><span>Status</span><span>${item.statusAparelho ?? '—'}</span></div>`;
        } else {
            div.className = 'result-box visible err';
            div.innerHTML = `<span style="color:var(--danger);font-size:13px;">Patrimônio "${patrimonio}" não encontrado.</span>`;
        }
    } catch {
        div.className = 'result-box visible err';
        div.innerHTML = `<span style="color:var(--danger);font-size:13px;">Erro ao conectar com o servidor.</span>`;
    }
}

// ── DASHBOARD ──
async function atualizarDashboard() {
    const ids = ['countSede','countSantana','countOiapoque','countLaranjal','countTartarugalzinho','countPortoGrande','countTotal','countAvaria'];
    ids.forEach(id => document.getElementById(id).innerText = '...');

    try {
        const lista = await (await fetch('/docs/listar-todos')).json();

        document.getElementById('countSede').innerText             = lista.filter(t => t.unidade === 'Macapá').length;
        document.getElementById('countSantana').innerText          = lista.filter(t => t.unidade === 'Escritório de Santana').length;
        document.getElementById('countOiapoque').innerText         = lista.filter(t => t.unidade === 'Escritório de Oiapoque').length;
        document.getElementById('countLaranjal').innerText         = lista.filter(t => t.unidade === 'Escritório de Laranjal do Jari').length;
        document.getElementById('countTartarugalzinho').innerText  = lista.filter(t => t.unidade === 'Escritório de Tartarugalzinho').length;
        document.getElementById('countPortoGrande').innerText      = lista.filter(t => t.unidade === 'Escritório de Porto Grande').length;
        document.getElementById('countTotal').innerText            = lista.length;
        document.getElementById('countAvaria').innerText           = lista.filter(t => t.statusAparelho === 'AVARIADO').length;
    } catch {
        ids.forEach(id => document.getElementById(id).innerText = '—');
    }
}
