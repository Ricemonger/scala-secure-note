FROM node:25.2.1-alpine3.23 AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .

RUN npm run build

FROM nginx:1.28.1-alpine3.23-slim AS runtime

RUN apk add --no-cache curl

RUN rm -rf /usr/share/nginx/html/*

COPY --from=builder /app/build /usr/share/nginx/html

COPY nginx.conf /etc/nginx/templates/default.conf.template

RUN touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid /usr/share/nginx/html /var/cache/nginx /var/log/nginx /etc/nginx/conf.d

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT}/ || exit 1

USER nginx

CMD ["nginx", "-g", "daemon off;"]