FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:1.29-alpine

ENV ANGULAR_API_BASE_URL=http://localhost:8080/api

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/srm-credit-engine-frontend/browser /usr/share/nginx/html
COPY docker-entrypoint.d/40-runtime-config.sh /docker-entrypoint.d/40-runtime-config.sh

RUN chmod +x /docker-entrypoint.d/40-runtime-config.sh

EXPOSE 80
