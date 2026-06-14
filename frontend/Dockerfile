# Build stage
FROM node:22-alpine AS build-stage
RUN npm install -g pnpm
WORKDIR /app
COPY package*.json pnpm-lock.yaml* ./
RUN pnpm install
COPY . .
RUN pnpm run build

# Production stage
FROM nginx:stable-alpine AS production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
