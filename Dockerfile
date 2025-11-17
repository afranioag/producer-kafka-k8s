# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copiar pom.xml e baixar dependências (cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código e buildar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Criar usuário não-root
RUN addgroup -g 1001 spring && \
    adduser -D -u 1001 -G spring spring

# Copiar JAR do stage anterior
COPY --from=build /app/target/*.jar app.jar

# Mudar ownership
RUN chown -R spring:spring /app

# Usar usuário não-root
USER spring

# Expor porta
EXPOSE 8081

# Variável de ambiente para profile
ENV SPRING_PROFILES_ACTIVE=prod

# Executar aplicação
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]