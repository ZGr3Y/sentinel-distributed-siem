# ============================================
# sentinel-dashboard — Multi-Stage Dockerfile
# ============================================
# Build context: sentinel-dashboard/ directory
# Usage: docker build -f sentinel-dashboard/Dockerfile sentinel-dashboard/

# --- Stage 1: Build ---
# Full Node.js image to compile TypeScript/React into static assets.
FROM node:20-alpine AS builder

WORKDIR /app

# Copy dependency manifests first (leverages Docker layer caching)
COPY package.json package-lock.json ./

# Deterministic install with increased network resilience (ETIMEDOUT fix for Docker)
RUN --mount=type=cache,target=/root/.npm \
    npm config set registry http://registry.npmjs.org/ && \
    npm config set fetch-retries 5 && \
    npm ci --audit=false --fund=false

# Copy source files
COPY . .

# Vite build: TypeScript compilation + bundling
# Output: /app/dist/ (HTML/JS/CSS)
RUN npm run build

# --- Stage 2: Runtime ---
# Minimal nginx image to serve static files.
# No Node.js runtime in the final image.
FROM nginx:alpine

# Security: create non-root user (POLP)
RUN addgroup -S appgroup && adduser -S USER -G appgroup

# Adjust nginx directories for non-root execution
RUN chown -R USER:appgroup /var/cache/nginx /var/log/nginx /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && chown USER:appgroup /var/run/nginx.pid

# Custom nginx config for SPA routing
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy compiled static assets from builder
COPY --from=builder /app/dist /usr/share/nginx/html

USER USER

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
