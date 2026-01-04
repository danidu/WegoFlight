# Docker Setup Guide

This guide explains how to build and run the WegoFlight API using Docker.

## Prerequisites

- Docker Engine 20.10+
- Docker Compose 2.0+

## Quick Start

### Build and Run with Docker Compose

```bash
# Build and start the service
docker-compose up --build

# Run in detached mode
docker-compose up -d --build

# View logs
docker-compose logs -f

# Stop the service
docker-compose down
```

### Build and Run with Docker

```bash
# Build the image
docker build -t wego-flight-api:latest .

# Run the container
docker run -d \
  --name wego-flight-api \
  -p 8080:8080 \
  wego-flight-api:latest

# View logs
docker logs -f wego-flight-api

# Stop the container
docker stop wego-flight-api
docker rm wego-flight-api
```

## Dockerfile Details

### Multi-Stage Build

The Dockerfile uses a multi-stage build for optimization:

1. **Build Stage**: Uses Maven to compile and package the application
   - Base image: `maven:3.9.6-eclipse-temurin-21`
   - Downloads dependencies (cached layer)
   - Builds the JAR file

2. **Runtime Stage**: Runs the application
   - Base image: `eclipse-temurin:21-jre-alpine` (smaller, JRE only)
   - Copies the JAR from build stage
   - Runs as non-root user for security

### Security Features

- **Non-root user**: Application runs as `spring` user (not root)
- **Minimal base image**: Alpine Linux for smaller attack surface
- **Health check**: Built-in health monitoring

### Image Size Optimization

- Multi-stage build reduces final image size
- Alpine Linux base image (~150MB vs ~500MB for full JDK)
- Only JRE included (not full JDK)

## Docker Compose Configuration

### Services

- **wego-flight-api**: Main application service

### Environment Variables

All configuration can be overridden via environment variables:

```yaml
# Rate Limiting
RATE_LIMIT_FLIGHTS_SEARCH_CAPACITY=10
RATE_LIMIT_FLIGHTS_SEARCH_REFILL_TOKENS=10
RATE_LIMIT_FLIGHTS_SEARCH_REFILL_DURATION=60

# Circuit Breaker
RESILIENCE4J_CIRCUITBREAKER_INSTANCES_FLIGHTPROVIDER_WAITDURATIONINOPENSTATE=30s
RESILIENCE4J_CIRCUITBREAKER_INSTANCES_FLIGHTPROVIDER_FAILURERATETHRESHOLD=50

# Aggregator APIs
AGGREGATOR_AMADEUS_BASE_URL=https://api.amadeus.com
AGGREGATOR_SABRE_BASE_URL=https://api.sabre.com
AGGREGATOR_TRAVELPORT_BASE_URL=https://api.travelport.com
```

### Health Check

The service includes a health check that:
- Checks every 30 seconds
- Times out after 3 seconds
- Allows 40 seconds for startup
- Retries 3 times before marking as unhealthy
- Uses curl to check if the Swagger UI is accessible (indicates service is up)

### Networking

- Creates a bridge network: `wego-network`
- Service is accessible on port 8080

## Customization

### Override Configuration

Create a `docker-compose.override.yml` file:

```yaml
version: '3.8'

services:
  wego-flight-api:
    environment:
      - RATE_LIMIT_FLIGHTS_SEARCH_CAPACITY=20
      - SPRING_PROFILES_ACTIVE=production
    ports:
      - "9090:8080"  # Change port mapping
```

### Build Arguments

Modify the Dockerfile to accept build arguments:

```dockerfile
ARG MAVEN_VERSION=3.9.6
ARG JAVA_VERSION=21

FROM maven:${MAVEN_VERSION}-eclipse-temurin-${JAVA_VERSION} AS build
```

## Troubleshooting

### Container Won't Start

1. Check logs:
   ```bash
   docker-compose logs wego-flight-api
   ```

2. Verify port availability:
   ```bash
   netstat -an | grep 8080
   ```

3. Check health status:
   ```bash
   docker-compose ps
   ```

### Build Fails

1. Clear Docker cache:
   ```bash
   docker builder prune
   ```

2. Rebuild without cache:
   ```bash
   docker-compose build --no-cache
   ```

### Performance Issues

1. Increase memory limit:
   ```yaml
   services:
     wego-flight-api:
       deploy:
         resources:
           limits:
             memory: 1G
           reservations:
             memory: 512M
   ```

2. Adjust JVM options:
   ```yaml
   environment:
     - JAVA_OPTS=-Xmx512m -Xms256m
   ```

## Production Considerations

### Security

1. **Use secrets management**:
   ```yaml
   secrets:
     api_key:
       file: ./secrets/api_key.txt
   ```

2. **Scan images for vulnerabilities**:
   ```bash
   docker scan wego-flight-api:latest
   ```

3. **Use specific image tags** (not `latest`):
   ```dockerfile
   FROM eclipse-temurin:21-jre-alpine
   ```

### Monitoring

1. **Add logging driver**:
   ```yaml
   logging:
     driver: "json-file"
     options:
       max-size: "10m"
       max-file: "3"
   ```

2. **Expose metrics endpoint** (if using Prometheus)

### Scaling

Scale horizontally with Docker Compose:

```bash
docker-compose up -d --scale wego-flight-api=3
```

Note: For production, use an orchestration platform like Kubernetes.

## CI/CD Integration

### GitHub Actions Example

```yaml
name: Build and Push Docker Image

on:
  push:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Build Docker image
        run: docker build -t wego-flight-api:${{ github.sha }} .
      - name: Push to registry
        run: docker push wego-flight-api:${{ github.sha }}
```

## Best Practices

1. ✅ **Multi-stage builds**: Reduces final image size
2. ✅ **Non-root user**: Improves security
3. ✅ **Health checks**: Enables proper orchestration
4. ✅ **.dockerignore**: Excludes unnecessary files
5. ✅ **Alpine base**: Smaller image size
6. ✅ **Environment variables**: Configurable without rebuild

## Additional Resources

- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

