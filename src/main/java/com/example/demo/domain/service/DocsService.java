package com.example.demo.domain.service;

import com.example.demo.domain.dto.DadosTermoDTO;
import com.example.demo.domain.dto.DashboardDTO;
import com.example.demo.domain.dto.EstatisticasDTO;
import com.example.demo.domain.dto.TermoRequestDTO;
import com.example.demo.domain.model.DadosTermo;
import com.example.demo.domain.model.TermoEntity;
import com.example.demo.domain.ports.in.GerarDocumentoUseCase;
import com.example.demo.domain.ports.out.ArmazemPort;
import com.example.demo.domain.ports.out.DocsGerar;
import com.example.demo.infrastructure.repository.TermoRepository;
import com.example.demo.application.util.SanitizerUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
            TermoEntity termo = new TermoEntity();
            termo.setNomeColaborador(SanitizerUtils.sanitizar(dto.getNomeColaborador()));
            termo.setInfo(    SanitizerUtils.sanitizar(nvl(dto.getInfo())));
            termo.setTipo(    SanitizerUtils.sanitizar(nvl(dto.getTipo())));
            termo.setUnidade( SanitizerUtils.sanitizar(nvl(dto.getUnidade())));

            termo.setDataInicio(  dto.getDataInicio().atStartOfDay());
            termo.setDataTermino( dto.getDataTermino().atStartOfDay());
            termo.setDataCriacao( LocalDateTime.now());

            String equipamentosConcatenados = "";

            if (dto.getItens() != null && !dto.getItens().isEmpty()) {
                // Extrai os patrimônios da lista
                String patrimoniosConcatenados = dto.getItens().stream()
                        .map(item -> SanitizerUtils.sanitizar(item.getPatrimonio()))
                        .collect(Collectors.joining("; "));
                termo.setPatrimonio(patrimoniosConcatenados);

                // Extrai os equipamentos da lista
                equipamentosConcatenados = dto.getItens().stream()
                        .map(item -> SanitizerUtils.sanitizar(nvl(item.getEquipamento())))
                        .collect(Collectors.joining("; "));
            } else {
                // Caso seja enviado um patrimônio e equipamento único (fora de lista)
                termo.setPatrimonio(SanitizerUtils.sanitizar(nvl(dto.getPatrimonio())));
                
                // Descomente a linha abaixo se o seu TermoRequestDTO também possuir getEquipamento() individual:
                // equipamentosConcatenados = SanitizerUtils.sanitizar(nvl(dto.getEquipamento()));
            }

            reposit.save(termo);

            DadosTermo dados = new DadosTermo();
            dados.setNomeColaborador(termo.getNomeColaborador());
            dados.setTipo(           termo.getTipo());
            dados.setInfo(           termo.getInfo());
            dados.setUnidade(        termo.getUnidade());
            dados.setPatrimonio(     termo.getPatrimonio());
            dados.setEquipamento(    equipamentosConcatenados); // <-- Atribuído corretamente aqui
            dados.setDataInicio(     termo.getDataInicio());
            dados.setDataTermino(    termo.getDataTermino());
            dados.setDataCriacao(    termo.getDataCriacao());

            if (dto.getItens() != null && !dto.getItens().isEmpty()) {
                List<DadosTermo.ItemTermo> itens = dto.getItens().stream()
                        .map(item -> new DadosTermo.ItemTermo(
                                SanitizerUtils.sanitizar(item.getPatrimonio()),
                                SanitizerUtils.sanitizar(nvl(item.getEquipamento()))
                        ))
                        .collect(Collectors.toList());
                dados.setItensLista(itens);
            }

            return geradorPort.gerar(dados, file.getInputStream());

        } catch (Exception e) {
            throw new RuntimeException("Erro ao processar termo: " + e.getMessage(), e);
        }
    }

    public List<DadosTermoDTO> listarTodosOsTermos() {
        return reposit.findAll().stream().map(termo -> {
            DadosTermoDTO dto = new DadosTermoDTO();
            dto.setNomeColaborador(termo.getNomeColaborador());
            dto.setDataInicio( termo.getDataInicio()  != null ? termo.getDataInicio().toLocalDate().toString()  : null);
            dto.setDataTermino(termo.getDataTermino() != null ? termo.getDataTermino().toLocalDate().toString() : null);
            dto.setPatrimonio( termo.getPatrimonio());
            dto.setUnidade(    termo.getUnidade());
            dto.setTipo(       termo.getTipo());
            dto.setInfo(       termo.getInfo());
            return dto;
        }).collect(Collectors.toList());
    }

    public EstatisticasDTO obterEstatisticas() {
        long total = reposit.count();
        Map<String, Long> porTipo = calcularPorTipo();
        return EstatisticasDTO.builder()
                .totalTermosGerados(total)
                .termosGeradosMesAtual(0L)
                .tempoMedioProcessamentoMs(125.5)
                .errosDeGeracaoMesAtual(0)
                .termosPorCategoria(porTipo)
                .build();
    }

    public DashboardDTO obterDashboard() {
        EstatisticasDTO estatisticas = obterEstatisticas();
        List<DadosTermoDTO> todos = listarTodosOsTermos();
        List<DadosTermoDTO> recentes = todos.size() > 5 ? todos.subList(0, 5) : todos;

        List<TermoEntity> entidades = reposit.findAll();

        Map<String, Long> porUnidade = entidades.stream()
                .collect(Collectors.groupingBy(
                        t -> (t.getUnidade() == null || t.getUnidade().isBlank()) ? "Não informado" : t.getUnidade(),
                        Collectors.counting()));

        LocalDateTime agora = LocalDateTime.now();
        LocalDateTime limite = agora.plusDays(7);
        long aVencer = entidades.stream()
                .filter(t -> t.getDataTermino() != null
                        && !t.getDataTermino().isBefore(agora)
                        && !t.getDataTermino().isAfter(limite))
                .count();

        return DashboardDTO.builder()
                .estatisticasGerais(estatisticas)
                .atividadesRecentes(recentes)
                .porUnidade(porUnidade)
                .porTipo(estatisticas.getTermosPorCategoria())
                .totalAVencer(aVencer)
                .build();
    }

    private Map<String, Long> calcularPorTipo() {
        return reposit.findAll().stream()
                .collect(Collectors.groupingBy(
                        t -> (t.getTipo() == null || t.getTipo().isBlank()) ? "Não informado" : t.getTipo(),
                        Collectors.counting()));
    }

    @Override public String executar(DadosTermo dadosDTO)                          { return null; }
    @Override public byte[] gerarArquivoBinario(DadosTermo dadosDTO)               { return null; }
    @Override public byte[] gerarArquivoDinamico(MultipartFile file, DadosTermo d) { return null; }

    private String nvl(String valor) { return valor != null ? valor : ""; }
}