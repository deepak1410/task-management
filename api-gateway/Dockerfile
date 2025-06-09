FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY target/api-gateway-0.0.1-SNAPSHOT.jar app.jar
COPY entrypoint.sh /app/entrypoint.sh

# Optional: Add metadata to image
LABEL maintainer="deepak.tech.hub.300@gmail.com"

# Make the script executable
RUN chmod +x /app/entrypoint.sh

ENTRYPOINT ["/app/entrypoint.sh"]
