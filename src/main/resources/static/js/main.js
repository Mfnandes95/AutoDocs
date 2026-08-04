/**
 * main.js - Corrigido para leitura de CSRF via Meta Tag e Download de Arquivo (Blob)
 */

// Captura o Token CSRF diretamente das Meta Tags injetadas pelo Thymeleaf
function getCsrfToken() {
    const metaToken = document.querySelector("meta[name='_csrf']")?.getAttribute("content");
    if (metaToken && metaToken !== "") return metaToken;

    // Fallback: Tenta obter do Cookie
    const value = `; ${document.cookie}`;
    const parts = value.split(`; XSRF-TOKEN=`);
    if (parts.length === 2) return parts.pop().split(';').shift();

    return '';
}

function getCsrfHeader() {
    return document.querySelector("meta[name='_csrf_header']")?.getAttribute("content") || 'X-XSRF-TOKEN';
}

function toast(msg, tipo = 'ok') {
    const t = document.getElementById('toast');
    if (!t) {
        alert(msg);
        return;
    }
    t.className = `toast ${tipo} show`;
    t.innerHTML = (tipo === 'ok' ? '✅' : '❌') + ' ' + msg;
    setTimeout(() => t.className = t.className.replace('show', ''), 3500);
}

// Geração Dinâmica de Documentos (POST com FormData, CSRF e Download Automático)
async function gerarDinamico() {
    const fileInput = document.getElementById('fileInput');
    const file = fileInput?.files[0];
   
    if (!file) return toast('Selecione o arquivo .docx', 'error');

    const dados = {
        nomeColaborador: document.getElementById('nome')?.value || "",
        patrimonio: document.getElementById('patrimonioInput')?.value || "0000",
        unidade: document.getElementById('unidade')?.value || "",
        tipo: document.getElementById('tipo')?.value || "",
        info: document.getElementById('info')?.value || "",
        dataInicio: document.getElementById('dataInicio')?.value || "",
        dataTermino: document.getElementById('dataTermino')?.value || ""
    };

    if (!dados.dataInicio || !dados.dataTermino) {
        return toast('As datas de início e término são obrigatórias!', 'error');
    }

    const btn = document.getElementById('btnGerar');
    const spinner = document.getElementById('spinnerGerar');
    if (btn) btn.disabled = true;
    if (spinner) spinner.style.display = 'inline-block';

    const formData = new FormData();
    formData.append('file', file);
    formData.append('dados', new Blob([JSON.stringify(dados)], { type: "application/json" }));

    try {
        const token = getCsrfToken();
        const headerName = getCsrfHeader();

        const headers = {};
        if (token) {
            headers[headerName] = token;
        }

        const response = await fetch('/api/docs/gerar-dinamico', { 
            method: 'POST', 
            headers: headers,
            body: formData 
        });

        // Trata respostas de erro (403, 400, 500)
        if (!response.ok) {
            let errorMsg = `Erro (${response.status}): `;
            try {
                const errJson = await response.json();
                errorMsg += errJson.mensagem || errJson.message || 'Falha na requisição';
            } catch (e) {
                errorMsg += 'Acesso negado ou permissão insuficiente.';
            }
            throw new Error(errorMsg);
        }

        // O backend retorna um binário (.docx). Faz o download via Blob
        const blob = await response.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = 'Termo_Gerado.docx';
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(url);
        a.remove();

        toast('Documento gerado e baixado com sucesso!', 'ok');
    } catch (error) {
        console.error("Erro na geração:", error);
        toast(error.message, 'error');
    } finally {
        if (btn) btn.disabled = false;
        if (spinner) spinner.style.display = 'none';
    }
}

// Listagem de Termos
async function buscarTermos() {
    try {
        const response = await fetch('/api/docs/listar-todos');
        const result = await response.json();
        
        if (!result.sucesso) throw new Error(result.mensagem);
        return result.dados;
    } catch (e) {
        toast('Erro ao buscar lista: ' + e.message, 'error');
        return [];
    }
}