window.onload = function() {
    const btnGenerate = document.getElementById('btnGenerate');
    const resultArea = document.getElementById('resultArea');
    const loader = document.getElementById('loader');
    const previewText = document.getElementById('document-preview');

    btnGenerate.onclick = async function() {
        // 1. Coleta de dados (IDs atualizados conforme o novo HTML)
        const nome = document.getElementById('userName').value;
        const info = document.getElementById('equipo').value;
        const dataInicioRaw = document.getElementById('dataInicio').value;
        const dataTerminoRaw = document.getElementById('dataTermino').value;
        const tipoDoc = document.getElementById('tipoDoc').value;

        // Validação básica
        if (!nome || !info || !dataInicioRaw || !dataTerminoRaw) {
            alert("Por favor, preencha todos os campos.");
            return;
        }

        // Função para formatar YYYY-MM-DD (HTML) para DD/MM/YYYY (Brasil/Word)
        const formatarData = (data) => data.split('-').reverse().join('/');

        // 2. Preparação da UI
        btnGenerate.disabled = true;
        btnGenerate.innerText = "Processando...";
        loader.classList.remove('hidden');
        resultArea.classList.add('hidden');

        const payload = {
            nomeColaborador: nome,
            info: info,
            tipo: tipoDoc,
            dataInicio: formatarData(dataInicioRaw),
            dataTermino: formatarData(dataTerminoRaw)
        };

        try {
            console.log("Enviando dados para o servidor Debian...");
            const response = await fetch('/docs/gerar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                console.log("Documento gerado! Iniciando download...");
                
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                
                const a = document.createElement('a');
                a.style.display = 'none';
                a.href = url;
                // Nome do arquivo dinâmico baseado no tipo e nome do colaborador
                a.download = `${tipoDoc.replace(/\s+/g, '_')}_${nome.replace(/\s+/g, '_')}.docx`;
                document.body.appendChild(a);
                a.click();
                
                setTimeout(() => {
                    window.URL.revokeObjectURL(url);
                    a.remove();
                }, 100);

                // Feedback visual de sucesso
                resultArea.classList.remove('hidden');
                previewText.innerText = `Sucesso! O download do ${tipoDoc} para ${nome} foi iniciado.`;
            } else {    
                console.error("Erro no servidor:", response.status);
                alert("O servidor encontrou um problema ao gerar o DOCX.");    
            }
        } catch (error) {
            console.error("Erro na requisição:", error);
            alert("Não foi possível conectar ao backend Java. Verifique se o bootRun está ativo.");
        } finally {
            // Restaura o estado original da UI independente de sucesso ou erro
            btnGenerate.disabled = false;
            btnGenerate.innerText = "Gerar Termo";
            loader.classList.add('hidden');
        }
    };
};