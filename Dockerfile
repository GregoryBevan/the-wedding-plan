FROM node:24-alpine AS frontend-build
WORKDIR /workspace/frontend
ENV VITE_ROUTER_BASE=/backoffice/
ENV VITE_API_BASE_URL=
COPY frontend/package.json frontend/pnpm-lock.yaml frontend/pnpm-workspace.yaml ./
RUN corepack enable
RUN pnpm install --frozen-lockfile
COPY frontend/ ./
RUN pnpm run build

FROM eclipse-temurin:25-jdk-alpine AS backend-build
WORKDIR /workspace/backend
COPY backend/ ./
COPY --from=frontend-build /workspace/frontend/dist/ ./src/main/resources/static/
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar --no-daemon

FROM eclipse-temurin:25-jre-alpine AS runtime
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
COPY --from=backend-build /workspace/backend/build/libs/*-SNAPSHOT.jar /app/app.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]


