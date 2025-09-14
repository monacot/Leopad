# Multi-stage build for React/Vite frontend

# Stage 1: Development dependencies and build
FROM node:18-alpine AS development
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Expose development port
EXPOSE 5173

# Development command
CMD ["npm", "run", "dev"]

# Stage 2: Build for production
FROM node:18-alpine AS build
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies (including dev dependencies for build)
RUN npm install

# Copy source code
COPY . .

# Build the application
RUN npm run build

# Stage 3: Production server with Nginx
FROM nginx:alpine AS production

# Copy built assets from build stage
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Expose port 80
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]