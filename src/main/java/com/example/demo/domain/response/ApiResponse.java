package com.example.demo.domain.response;

public class ApiResponse<T> {
    private boolean sucesso;
    private String mensagem;
    private T dados;

    // Construtor privado
    private ApiResponse(boolean sucesso, String mensagem, T dados) {
        this.sucesso = sucesso;
        this.mensagem = mensagem;
        this.dados = dados;
    }

    // Métodos estáticos para criar respostas facilmente
    public static <T> ApiResponse<T> ok(T dados) {
        return new ApiResponse<>(true, "Operação realizada com sucesso", dados);
    }

    public static ApiResponse<String> erro(String mensagem) {
        return new ApiResponse<>(false, mensagem, null);
    }

    // Getters obrigatórios para o Jackson (o serializador do Spring) converter para JSON
    public boolean isSucesso() { return sucesso; }
    public String getMensagem() { return mensagem; }
    public T getDados() { return dados; }
}