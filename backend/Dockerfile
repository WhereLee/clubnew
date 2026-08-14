# ============ 构建阶段 ============
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
# 先拷贝 pom 预下载依赖，利用 Docker 层缓存加速重复构建
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn package -DskipTests -B -q

# ============ 运行阶段 ============
FROM eclipse-temurin:17-jre
WORKDIR /app
# 非 root 用户运行（企业安全基线）
RUN groupadd -r clubflow && useradd -r -g clubflow clubflow && mkdir -p /app/logs && chown -R clubflow:clubflow /app
COPY --from=build /app/target/*.jar app.jar
USER clubflow
EXPOSE 8081
# 生产 profile 由 compose 传入 SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
