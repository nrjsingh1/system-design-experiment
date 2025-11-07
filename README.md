# system-design-experiment

This project includes a Docker compose configuration to run a local PostgreSQL instance for development.

## Quickstart (Docker)

1. Use the development env file (or copy it to `.env` if you prefer):

```bash
# Edit `.env.dev` directly or copy to `.env` if your tooling expects that
cp .env.dev .env
# Edit .env.dev (or .env) if you want different credentials
```

2. Start the database:

```bash
docker compose up -d
```

3. Verify the DB is healthy:

```bash
docker compose ps
docker logs sde-postgres --tail 50
```

4. Run the Spring Boot app (it uses the same env vars that `application.properties` references):

```bash
# using the project mvnw wrapper
DB_HOST=localhost DB_PORT=5432 DB_NAME=system_design_db DB_USERNAME=postgres DB_PASSWORD=password ./mvnw spring-boot:run
```

5. Tear down when you're done:

```bash
docker compose down -v
```

- Notes:
- `application.properties` is already set to read the `DB_*` env vars. `docker-compose` uses `.env.dev` by default. If you run the app from your IDE, provide the same env vars in your run configuration or copy them into `.env`/`.env.dev`.
- For CI and production, use a managed Postgres service and secure credentials (do not commit `.env` with real secrets).
