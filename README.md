````markdown
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

## Running the application with different properties or profiles

You can start the Spring Boot application while pointing it at an alternate properties file or selecting a profile.

- Use a different properties file (file system path):

```bash
# run with a specific properties file (use full/relative path)
./mvnw -Dspring-boot.run.arguments="--spring.config.location=file:./config/application-local.properties" spring-boot:run
```

- Use Spring profiles (recommended if you provide profile-specific files like `application-local.properties`):

```bash
# using environment variable
SPRING_PROFILES_ACTIVE=local ./mvnw spring-boot:run

# or with the maven plugin property
./mvnw -Dspring-boot.run.profiles=local spring-boot:run
```

- Run a built jar with a specific properties file or profile:

```bash
# after packaging
java -jar target/system-design-experiment-0.0.1-SNAPSHOT.jar --spring.config.location=file:./config/application-local.properties
# or
java -jar target/system-design-experiment-0.0.1-SNAPSHOT.jar --spring.profiles.active=local
```

Notes:
- `--spring.config.location` completely replaces the default search locations unless you use `spring.config.additional-location`.
- Profile-based files like `application-local.properties` should live on the classpath or be referenced explicitly.

## Running JMeter tests and viewing results

This repository includes a JMeter test plan at `src/test/jmeter/load-test-plan.jmx` and a helper script `src/test/jmeter/run-scalability-test.sh` that:

- updates the ThreadGroup size in the JMX (for step tests)
- runs JMeter in non-GUI mode
- collects actuator metrics from the running app
- produces a Markdown summary `test_summary.md` and an HTML dashboard

1) Prerequisites

- JMeter installed and on your PATH (>= 5.x). If you don't have it, install via Homebrew or download from the Apache JMeter site.
- `jq` installed (used by the script to parse actuator JSON). Install with `brew install jq` on macOS.
- (Optional) `vmstat` — macOS provides `vm_stat` instead of `vmstat`; the script will try to use `vmstat` and may fall back to `top`/`vm_stat` where available.

2) Run the tests

Make the script executable and run it (it will check the app health and then run step-load tests):

```bash
chmod +x src/test/jmeter/run-scalability-test.sh
# ensure your app is running (previous "Quickstart" shows how)
./src/test/jmeter/run-scalability-test.sh
```

You can also run JMeter directly against the test plan (non-GUI):

```bash
# run jmeter in non-gui mode and generate a dashboard
jmeter -n -t src/test/jmeter/load-test-plan.jmx -l results.jtl -e -o target/jmeter-report
```

3) Viewing results

- If you used the provided script it writes a summary file and dashboard into `src/test/jmeter/scalability-results/test_<TIMESTAMP>/`.
  - The Markdown summary is: `src/test/jmeter/scalability-results/test_<TIMESTAMP>/test_summary.md`
  - The JMeter HTML dashboard (if generated) will be under the `dashboard_*/` folder inside that test directory.

- To open the Markdown summary on macOS:

```bash
open src/test/jmeter/scalability-results/test_<TIMESTAMP>/test_summary.md
```

- To open the JMeter HTML dashboard in your browser:

```bash
open src/test/jmeter/scalability-results/test_<TIMESTAMP>/dashboard_*/index.html
```

4) Troubleshooting

- If the script reports `vmstat: command not found` on macOS, install `vmstat` or let the script fall back; I can also update the script to prefer `vm_stat` on macOS.
- If JMeter fails due to URL encoding or test-plan parameters, confirm variables in `load-test-plan.jmx` and any referenced CSV files.

````
