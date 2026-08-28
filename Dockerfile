# =========================
# BUILD
# =========================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copia o pom
COPY pom.xml .

# Baixa as dependências
RUN mvn dependency:go-offline -B

# Copia o código fonte
COPY src ./src

# Compila e gera o JAR
RUN mvn clean package -DskipTests

# Apenas para confirmar no log do Railway
RUN ls -lah /app/target


# =========================
# EXECUÇÃO
# =========================
FROM eclipse-temurin:21-jre-alpine

# Criar um usuário não-root para rodar a aplicação
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app

# Ajustar a permissão do diretório para o novo usuário
RUN chown -R spring:spring /app
USER spring:spring

# Copia o JAR do estágio de build
COPY --from=build /app/target/*.jar app.jar


# Define limites básicos de memória para a JVM (ajuste conforme necessário)
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

# Utiliza os JAVA_OPTS na execução
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]