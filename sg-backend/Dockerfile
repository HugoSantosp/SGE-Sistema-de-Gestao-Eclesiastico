# ============================================
# Build Stage
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copia pom.xml primeiro para cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia código fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================
# Runtime Stage
# ============================================
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Cria usuário não-root
RUN groupadd -r sge && useradd -r -g sge sge

# Cria diretórios necessários
RUN mkdir -p /app/uploads /app/logs && \
    chown -R sge:sge /app

# Copia o JAR do build stage
COPY --from=build /app/target/*.jar app.jar

# Muda para usuário não-root
USER sge

# Porta padrão
EXPOSE 8080

# Variáveis de ambiente (sobrescrever via docker-compose ou -e)
ENV JAVA_OPTS="-Xms256m -Xmx512m" \
    SPRING_PROFILES_ACTIVE=prod

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
