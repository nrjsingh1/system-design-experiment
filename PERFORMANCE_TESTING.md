# Performance Testing Guide: Comparing Configuration Profiles

This guide explains how to test and compare the different application property configurations.

## AI Model Configuration Profiles

Each configuration represents how different AI models might approach Spring Boot optimization:

### Performance-Oriented Profiles
1. **baseline** - Current production config with comprehensive metrics
2. **minimal** - Maximum throughput, minimal observability overhead
3. **aggressive** - Optimized for peak load with higher resource usage
4. **balanced** - Good performance with reasonable monitoring
5. **memory-optimized** - Low memory footprint for constrained environments

### AI Model Style Profiles
6. **gpt4** - Conservative, enterprise-ready with comprehensive monitoring
7. **claude** - Thoughtful balance with clear reasoning and trade-offs
8. **gemini** - Experimental and cutting-edge, pushing performance boundaries
9. **copilot** - Pragmatic developer-focused with sensible defaults
10. **chatgpt35** - Simple and straightforward, no-frills approach

## AI Model Comparison Matrix

| Profile | Philosophy | Pool Size | Threads | Metrics | Batching | Best For |
|---------|-----------|-----------|---------|---------|----------|----------|
| gpt4 | Conservative, documented | 25 | 200 | Comprehensive | 25 | Enterprise production |
| claude | Balanced, thoughtful | 35 | 200 | Selective | 30 | General production |
| gemini | Aggressive, experimental | 150 | 500 | Minimal | 100 | Peak performance |
| copilot | Pragmatic, dev-friendly | 30 | 200 | Useful subset | 25 | Developer productivity |
| chatgpt35 | Simple, straightforward | 20 | 200 | Basic | None | Quick setup |

## Performance vs Strategy Comparison

| Profile | Metrics | Pool Size | Thread Pool | Best For |
|---------|---------|-----------|-------------|----------|
| baseline | Full (Prometheus, histograms) | 20 | 200 (default) | Reference/Production |
| minimal | Health only | 50 | 200 (default) | Max throughput |
| aggressive | Selective | 100 | 400 | Peak load scenarios |
| balanced | Key metrics | 40 | 200 | General production |
| memory-optimized | Minimal | 10 | 50 | Resource-constrained |

## How to Run Tests with Different Profiles

### Method 1: Using Spring Profiles

```bash
# Test AI model configurations
SPRING_PROFILES_ACTIVE=gpt4 ./mvnw spring-boot:run
SPRING_PROFILES_ACTIVE=claude ./mvnw spring-boot:run
SPRING_PROFILES_ACTIVE=gemini ./mvnw spring-boot:run
SPRING_PROFILES_ACTIVE=copilot ./mvnw spring-boot:run
SPRING_PROFILES_ACTIVE=chatgpt35 ./mvnw spring-boot:run

# Test strategy-based configurations
SPRING_PROFILES_ACTIVE=minimal ./mvnw spring-boot:run
SPRING_PROFILES_ACTIVE=aggressive ./mvnw spring-boot:run
```

### Method 2: Using Built JAR

```bash
# Build the application first
./mvnw clean package -DskipTests

# Run with AI model profiles
java -jar target/system-design-experiment-0.0.1-SNAPSHOT.jar --spring.profiles.active=gpt4
java -jar target/system-design-experiment-0.0.1-SNAPSHOT.jar --spring.profiles.active=claude
java -jar target/system-design-experiment-0.0.1-SNAPSHOT.jar --spring.profiles.active=gemini
```

## Running Performance Tests

### Step 1: Start the application with a profile

```bash
# Example: Test the minimal configuration
SPRING_PROFILES_ACTIVE=minimal DB_HOST=localhost DB_PORT=5432 DB_NAME=system_design_db DB_USERNAME=postgres DB_PASSWORD=password ./mvnw spring-boot:run
```

### Step 2: Run JMeter tests

```bash
# Make script executable (first time only)
chmod +x src/test/jmeter/run-scalability-test.sh

# Run the scalability test
./src/test/jmeter/run-scalability-test.sh
```

### Step 3: Collect results

Results will be in `src/test/jmeter/scalability-results/test_<TIMESTAMP>/`

- Summary: `test_summary.md`
- Dashboard: `dashboard_*/index.html`
- Metrics: `metrics_*users/`

## Systematic Testing Approach

### 1. Baseline Test (Reference)

```bash
# Terminal 1: Start app
SPRING_PROFILES_ACTIVE=baseline DB_HOST=localhost DB_PORT=5432 DB_NAME=system_design_db DB_USERNAME=postgres DB_PASSWORD=password ./mvnw spring-boot:run

# Terminal 2: Run tests
./src/test/jmeter/run-scalability-test.sh

# Save results
mv src/test/jmeter/scalability-results/test_* src/test/jmeter/scalability-results/baseline-test
```

### 2. Test Each Profile

Repeat for each profile:

```bash
# Stop the app (Ctrl+C in Terminal 1)

# Start with new profile
SPRING_PROFILES_ACTIVE=minimal DB_HOST=localhost DB_PORT=5432 DB_NAME=system_design_db DB_USERNAME=postgres DB_PASSWORD=password ./mvnw spring-boot:run

# Run tests
./src/test/jmeter/run-scalability-test.sh

# Save results with profile name
mv src/test/jmeter/scalability-results/test_* src/test/jmeter/scalability-results/minimal-test
```

### 3. Compare Results

Create a comparison spreadsheet with these metrics from each `test_summary.md`:

#### AI Model Configurations

| Profile | Avg Response Time (10 users) | Avg Response Time (100 users) | Error Rate | CPU Usage | Memory Usage | Philosophy |
|---------|------------------------------|-------------------------------|------------|-----------|--------------|------------|
| gpt4 | | | | | | Conservative |
| claude | | | | | | Balanced |
| gemini | | | | | | Aggressive |
| copilot | | | | | | Pragmatic |
| chatgpt35 | | | | | | Simple |

#### Strategy-Based Configurations

| Profile | Avg Response Time (10 users) | Avg Response Time (100 users) | Error Rate | CPU Usage | Memory Usage |
|---------|------------------------------|-------------------------------|------------|-----------|--------------|
| baseline | | | | | |
| minimal | | | | | |
| aggressive | | | | | |
| balanced | | | | | |
| memory-optimized | | | | | |

## AI Model Configuration Deep Dive

### GPT-4 Style (Conservative Enterprise)
**Characteristics:**
- Comprehensive logging and monitoring
- Connection leak detection enabled
- Access logs for audit trail
- Graceful shutdown with 30s timeout
- Validation enabled on all operations

**Expected Performance:**
- Lower raw throughput due to monitoring overhead
- Better debugging capabilities
- More predictable under varying loads
- Higher memory usage from detailed metrics

**Best Use Case:** Enterprise production where observability and debugging are critical

### Claude Style (Thoughtful Balance)
**Characteristics:**
- Selective metrics (only actionable insights)
- Balanced connection pool sizing
- Security-conscious (error messages hidden)
- Optimized for typical web app workloads

**Expected Performance:**
- Good throughput with useful metrics
- Moderate resource usage
- Stable performance across load ranges
- Clean separation of concerns

**Best Use Case:** General production deployments needing monitoring without excessive overhead

### Gemini Style (Experimental Performance)
**Characteristics:**
- Aggressive batching (batch_size=100)
- Large connection pool (150 connections)
- Massive thread pool (500 threads)
- HTTP/2 enabled
- Statement caching optimizations

**Expected Performance:**
- Highest throughput under heavy load
- May be unstable at very high concurrency
- Higher memory and connection usage
- Fast response times for bulk operations

**Best Use Case:** Peak performance scenarios, load testing upper limits

### Copilot Style (Developer Pragmatic)
**Characteristics:**
- Environment-aware configuration
- Inline documentation and tips
- Leak detection toggle for dev/prod
- Sensible defaults that scale

**Expected Performance:**
- Good balance out-of-the-box
- Easy to tune with inline comments
- Predictable behavior
- Developer-friendly debugging

**Best Use Case:** Active development, teams learning Spring Boot

### ChatGPT-3.5 Style (Simple & Direct)
**Characteristics:**
- Minimal configuration
- No advanced tuning
- Basic metrics only
- Standard Spring Boot defaults

**Expected Performance:**
- Adequate for small-medium loads
- Lower resource usage
- May hit limits sooner under load
- Predictable baseline performance

**Best Use Case:** Prototypes, simple applications, learning projects

## Key Metrics to Compare

1. **Response Time**: Average and 90th percentile at different load levels
2. **Throughput**: Requests per second
3. **Error Rate**: Percentage of failed requests
4. **CPU Usage**: From system metrics
5. **Memory Usage**: Heap and non-heap memory
6. **Connection Pool**: Active and idle connections
7. **Latency Distribution**: From JMeter dashboard histograms

## Expected Outcomes

### Minimal Profile
- **Pros**: Fastest response times, highest throughput
- **Cons**: Limited visibility, harder to debug issues

### Aggressive Profile
- **Pros**: Best performance under heavy load (100+ users)
- **Cons**: Higher memory usage, more database connections

### Balanced Profile
- **Pros**: Good compromise - decent performance with useful metrics
- **Cons**: Some overhead from metrics collection

### Memory-Optimized Profile
- **Pros**: Lowest memory footprint, works in constrained environments
- **Cons**: Lower throughput, may struggle under heavy load

## Advanced Testing

### Test with JMeter directly (custom load patterns)

```bash
# Light load (10 threads, 1 minute)
jmeter -n -t src/test/jmeter/load-test-plan.jmx -Jusers=10 -Jduration=60 -l light-load.jtl -e -o light-dashboard

# Heavy load (200 threads, 5 minutes)
jmeter -n -t src/test/jmeter/load-test-plan.jmx -Jusers=200 -Jduration=300 -l heavy-load.jtl -e -o heavy-dashboard

# Spike test (quick ramp-up)
jmeter -n -t src/test/jmeter/load-test-plan.jmx -Jusers=500 -Jrampup=10 -Jduration=120 -l spike-test.jtl -e -o spike-dashboard
```

### Monitor application metrics during tests

```bash
# Watch metrics in real-time
watch -n 1 'curl -s http://localhost:8080/actuator/metrics/jvm.memory.used | jq'
watch -n 1 'curl -s http://localhost:8080/actuator/metrics/hikaricp.connections.active | jq'
watch -n 1 'curl -s http://localhost:8080/actuator/metrics/system.cpu.usage | jq'
```

## Tips for Accurate Testing

1. **Warm up the JVM**: Run a light load test first, then restart and run the real test
2. **Clean database state**: Reset the database between tests if needed
3. **Consistent environment**: Close other applications, use same machine
4. **Multiple runs**: Run each test 3 times and average the results
5. **Cool down**: Wait 30-60 seconds between tests for system to stabilize

## Analyzing Results

1. Open the HTML dashboard: `open src/test/jmeter/scalability-results/<profile>-test/dashboard_*/index.html`
2. Check response time over time graphs
3. Compare throughput graphs
4. Review error percentages
5. Examine the summary Markdown files side by side

## Recommended Testing Order

1. Start with **baseline** (your current config)
2. Test **minimal** (see max theoretical performance)
3. Test **balanced** (likely production candidate)
4. Test **aggressive** (if you need to handle spikes)
5. Test **memory-optimized** (if deploying to constrained environments)

## Conclusion

After testing all profiles, you should be able to answer:
- Which profile gives the best response times?
- Which profile handles high load best?
- Which profile uses the least resources?
- What's the right balance for your production needs?

Choose the profile that best fits your requirements for throughput, observability, and resource constraints.
