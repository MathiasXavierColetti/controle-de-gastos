# ===================================================================
# ETAPA 1: Build (Compilação da Aplicação)
# ===================================================================
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app

# 1. Copia apenas o pom.xml para aproveitar o cache do Docker
COPY pom.xml .

# 2. Baixa as dependências em uma camada isolada (Cache Layer)
RUN mvn dependency:go-offline -B

# 3. Copia o código-fonte e realiza o build do JAR
COPY src ./src
RUN mvn clean package -DskipTests

# ===================================================================
# ETAPA 2: Runtime (Imagem enxuta para execução)
# ===================================================================
FROM eclipse-temurin:25-jre
WORKDIR /app

# Cria um usuário não-root para melhor segurança do contêiner
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Copia o artefato .jar gerado na etapa de build
COPY --from=build /app/target/*.jar app.jar

# Informa a porta padrão
EXPOSE 8080

# Inicia a aplicação otimizando o consumo de memória no contêiner
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]