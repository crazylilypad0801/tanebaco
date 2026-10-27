version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/inventory_db
      - SPRING_DATASOURCE_USERNAME=inventory_user
      - SPRING_DATASOURCE_PASSWORD=inventory_pass
      - SPRING_JPA_HIBERNATE_DDL_AUTO=update
    depends_on:
      db:
        condition: service_healthy

  db:
    image: postgres:15-alpine
    container_name: inventory_postgres
    environment:
      - POSTGRES_DB=inventory_db
      - POSTGRES_USER=inventory_user
      - POSTGRES_PASSWORD=inventory_pass
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U inventory_user -d inventory_db"]
      interval: 5s
      timeout: 5s
      retries: 5

volumes:
  postgres_data: