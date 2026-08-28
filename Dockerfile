# Etapa 1: Compilação do projeto com Maven e Java 25
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Execução da aplicação com uma imagem enxuta de JRE 25
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Porta padrão que a aplicação vai escutar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]