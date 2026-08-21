# ===== Estágio 1: Build (compila o projeto) =====
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia primeiro os arquivos de dependência (cache inteligente)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Baixa as dependências (fica em cache se o pom não mudar)
RUN mvn dependency:go-offline -B

# Copia o código-fonte e compila, gerando o .jar
COPY src/ src/
RUN mvn clean package -DskipTests

# ===== Estágio 2: Runtime (só roda o .jar) =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia só o .jar pronto do estágio de build
COPY --from=build /app/target/*.jar app.jar

# A porta que a aplicação usa
EXPOSE 8081

# Comando que roda quando o contêiner inicia
ENTRYPOINT ["java", "-jar", "app.jar"];