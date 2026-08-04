package com.example.demo.domain.service;

import com.example.demo.domain.dto.DadosTermoDTO;
import com.example.demo.domain.dto.DashboardDTO; // ADICIONADO
import com.example.demo.domain.dto.EstatisticasDTO; // ADICIONADO
import com.example.demo.domain.dto.TermoRequestDTO;
import com.example.demo.domain.model.DadosTermo;
import com.example.demo.domain.model.TermoEntity;
import com.example.demo.domain.ports.out.DocsGerar;
import com.example.demo.domain.ports.out.ArmazemPort;
import com.example.demo.domain.ports.in.GerarDocumentoUseCase;
import com.example.demo.infrastructure.repository.TermoRepository;
import com.example.demo.application.util.SanitizerUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.stream.Collectors;
import java.util.List;
import java.util.Map;

@Service
public class DocsService implements GerarDocumentoUseCase {

    private final DocsGerar geradorPort;
    private final ArmazemPort armazenamentoPort;
    private final TermoRepository reposit;

    public DocsService(DocsGerar geradorPort, ArmazemPort armazenamentoPort, TermoRepository repository) {
        this.geradorPort = geradorPort;
        this.armazenamentoPort = armazenamentoPort;
        this.reposit = repository;
    }

    @Override
    public byte[] processarGeracao(MultipartFile file, TermoRequestDTO dto) {
        try {
            // 1. Criar e Popular a Entidade
            TermoEntity termo = new TermoEntity();
            termo.setNomeColaborador(SanitizerUtils.sanitizar(dto.getNomeColaborador()));
            termo.setInfo(SanitizerUtils.sanitizar(dto.getInfo()));
            termo.setPatrimonio(SanitizerUtils.sanitizar(dto.getPatrimonio()));
            termo.setUnidade(SanitizerUtils.sanitizar(dto.getUnidade()));
            termo.setTipo(SanitizerUtils.sanitizar(dto.getTipo()));

            // 2. Mapeamento de Datas com Proteção Contra Nulo
            if (dto.getDataInicio() != null) {
                termo.setDataInicio(dto.getDataInicio().atStartOfDay());
            } else {
                throw new RuntimeException("Data de início é obrigatória.");
            }

            if (dto.getDataTermino() != null) {
                termo.setDataTermino(dto.getDataTermino().atStartOfDay());
            } else {
                throw new RuntimeException("Data de término é obrigatória.");
            }
            
            // 3. Persistência
            reposit.save(termo);

            // 4. Preparar dados para o POI-TL
            DadosTermo dados = new DadosTermo();
            dados.setNomeColaborador(termo.getNomeColaborador());
            
            return geradorPort.gerar(dados, file.getInputStream());
            
        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar termo: " + e.getMessage(), e);
        }
    }

    public List<DadosTermoDTO> listarTodosOsTermos() {
        return reposit.findAll().stream().map(termo -> {
            DadosTermoDTO dto = new DadosTermoDTO();
            dto.setNomeColaborador(termo.getNomeColaborador());
            dto.setDataInicio(termo.getDataInicio() != null ? termo.getDataInicio().toLocalDate().toString() : null);
            dto.setDataTermino(termo.getDataTermino() != null ? termo.getDataTermino().toLocalDate().toString() : null);
            dto.setPatrimonio(termo.getPatrimonio());
            dto.setUnidade(termo.getUnidade());
            dto.setTipo(termo.getTipo());
            dto.setInfo(termo.getInfo());
            return dto;
        }).collect(Collectors.toList());
    }

    // =======================================================
    // NOVOS MÉTODOS ADICIONADOS PARA RESOLVER O ERRO
    // =======================================================

    public EstatisticasDTO obterEstatisticas() {
        // Busca a contagem total de registros no banco de dados usando o repositório
        long total = reposit.count();

        // Constrói o DTO de estatísticas. 
        // Os demais valores estão estáticos para a compilação.
        // No futuro, você pode criar consultas (Queries) específicas no TermoRepository para preenchê-los de verdade.
        return EstatisticasDTO.builder()
                .totalTermosGerados(total)
                .termosGeradosMesAtual(0L) // TODO: Implementar query por mês no repositório
                .tempoMedioProcessamentoMs(125.5) // Exemplo fictício
                .errosDeGeracaoMesAtual(0)
                .termosPorCategoria(Map.of("Equipamentos", total)) // Exemplo genérico
                .build();
    }

    public DashboardDTO obterDashboard() {
        EstatisticasDTO estatisticas = obterEstatisticas();
        
        // Pega todos os termos (para simplificar agora) e limita para pegar apenas os últimos 5
        // (Em um ambiente de produção, o ideal é usar PageRequest no repositório)
        List<DadosTermoDTO> todosOsTermos = listarTodosOsTermos();
        List<DadosTermoDTO> atividadesRecentes = todosOsTermos.size() > 5 
                ? todosOsTermos.subList(0, 5) 
                : todosOsTermos;

        return DashboardDTO.builder()
                .estatisticasGerais(estatisticas)
                .atividadesRecentes(atividadesRecentes)
                .build();
    }

    // =======================================================

    @Override
    public String executar(DadosTermo dadosDTO) { return null; } 
    @Override
    public byte[] gerarArquivoBinario(DadosTermo dadosDTO) { return null; }
    @Override
    public byte[] gerarArquivoDinamico(MultipartFile file, DadosTermo dados) { return null; }
}