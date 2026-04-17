window.onload = function() {
    const btnGenerate = document.getElementById('btnGenerate');
    const resultArea = document.getElementById('resultArea');
    const loader = document.getElementById('loader');
    const previewText = document.getElementById('document-preview');

    btnGenerate.onclick = async function() {
        const nome = document.getElementById('userName').value;
        const info = document.getElementById('equipo').value;

        if (!nome || !info) {
            alert("Por favor, preencha todos os campos.");
            return;
        }

        // UI - Iniciando
        btnGenerate.disabled = true;
        btnGenerate.innerText = "Processando...";
        loader.classList.remove('hidden');
        resultArea.classList.add('hidden');

        const payload = {
            nomeColaborador: nome,
            info: info,
            tipo: "Termo de Responsabilidade",
            dataInicio: new Date().toLocaleDateString('pt-BR')
        };

        try {
            const response = await fetch('/docs/gerar', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (response.ok) {
                const blob = await response.blob();
                const url = window.URL.createObjectURL(blob);
                
                const a = document.createElement('a');
                a.style.display = 'none';
                a.href = url;
                a.download = `Termo_${nome.replace(/\s+/g, '_')}.docx`;
                document.body.appendChild(a);
                a.click();
                
                // Limpeza da URL de memória
                setTimeout(() => {
                    window.URL.revokeObjectURL(url);
                    a.remove();
                }, 100);

                // UI - Sucesso
                resultArea.classList.remove('hidden');
                previewText.innerText = "Documento gerado com sucesso para: " + nome;
            } else {    
                alert("Ocorreu um erro no servidor ao gerar o documento.");    
            }
        } catch (error) {
            console.error("Erro na requisição:", error);
            alert("Não foi possível conectar ao servidor. Verifique se o Spring Boot está rodando.");
        } finally {
            // UI - Reset Final (Sempre executa, dando erro ou não)
            btnGenerate.disabled = false;
            btnGenerate.innerText = "Gerar Termo";
            loader.classList.add('hidden');
        }
    };
};