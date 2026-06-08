# Estágio 1: Compilação (Build)
FROM gradle:8-jdk21 AS build
WORKDIR /app

# Copia todos os ficheiros do repositório para dentro do contêiner
COPY . .

# Executa o build do Gradle para gerar o arquivo .jar (ignora os testes para poupar tempo)
RUN gradle build -x test --no-daemon

# Estágio 2: Execução (Runtime)
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Copia o .jar gerado no estágio anterior para a imagem final limpa
COPY --from=build /app/build/libs/*.jar app.jar

# Indica a porta que o Spring Boot vai usar
EXPOSE 8080

# Comando definitivo que inicia a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]