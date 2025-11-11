# Prompt for AI Models to Generate application.properties

Use this exact prompt with each AI model to get their configuration:

---

## Prompt:

```
Create a production-ready application.properties file for this Spring Boot 3.5.7 application.

Context:
- E-commerce system with Products, Orders, Customers, OrderItems
- PostgreSQL 15 database
- Using HikariCP for connection pooling
- JPA/Hibernate for ORM
- Flyway for database migrations
- Spring Boot Actuator for monitoring
- Need Prometheus metrics export
- JMeter load testing will be performed

Database connection uses environment variables:
- DB_HOST (default: localhost)
- DB_PORT (default: 5432)
- DB_NAME (default: system_design_db)
- DB_USERNAME (default: postgres)
- DB_PASSWORD (default: password)

Requirements:
1. Configure HikariCP connection pool with optimal settings
2. Configure JPA/Hibernate for production use
3. Set up Actuator endpoints and metrics
4. Enable Prometheus metrics export
5. Configure any performance optimizations you recommend
6. Add logging configuration
7. Include any other Spring Boot best practices

Provide ONLY the complete application.properties file content, optimized based on your expertise and recommendations. Add comments explaining your choices.
```

---

## Instructions:

1. Switch to each AI model in VS Code
2. Paste the above prompt
3. Save the response as:
   - `src/main/resources/application-<modelname>.properties`
   
Models to test:
- GPT-4o
- GPT-4
- Claude 3.5 Sonnet
- Gemini 1.5 Pro
- o1-preview (if available)
- o1-mini (if available)

## After collecting all configs:

Run this command to see what you have:
```bash
ls -la src/main/resources/application-*.properties
```

Then test each one with:
```bash
SPRING_PROFILES_ACTIVE=<modelname> ./mvnw spring-boot:run
./src/test/jmeter/run-scalability-test.sh
```
