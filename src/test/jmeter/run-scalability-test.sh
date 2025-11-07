#!/bin/bash

# Exit on any error
set -e

# Function to check if the application is running
check_application() {
    if ! curl -s "http://localhost:8080/actuator/health" > /dev/null; then
        echo "Error: Application is not running or not accessible"
        exit 1
    fi
}

# Function to cleanup JMeter processes on script exit
cleanup() {
    echo "Cleaning up JMeter processes..."
    pkill -f jmeter || true
}

# Create results directory if it doesn't exist
RESULTS_DIR="src/test/jmeter/scalability-results"
mkdir -p "$RESULTS_DIR"

# Get timestamp for unique report directory
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
TEST_DIR="$RESULTS_DIR/test_$TIMESTAMP"
mkdir -p "$TEST_DIR"

# Array of concurrent user counts for step-load testing
USERS=(10 25 50 100 200 500)
DURATION=300  # Duration for each step in seconds
RAMP_UP=30    # Ramp-up time for each step in seconds

# Set trap for cleanup
trap cleanup EXIT

# Check if application is running
check_application

# Function to collect system metrics
collect_metrics() {
    local users=$1
    local timestamp=$(date +"%Y-%m-%d %H:%M:%S")
    echo "Collecting metrics for $users users at $timestamp"
    
    # Create metrics directory for this test
    local metrics_dir="$TEST_DIR/metrics_${users}users"
    mkdir -p "$metrics_dir"
    
    # Collect various metrics
    curl -s "http://localhost:8080/actuator/metrics" > "$metrics_dir/all_metrics.json"
    curl -s "http://localhost:8080/actuator/metrics/system.active.requests" > "$metrics_dir/active_requests.json"
    curl -s "http://localhost:8080/actuator/metrics/system.response.time" > "$metrics_dir/response_time.json"
    curl -s "http://localhost:8080/actuator/metrics/jvm.memory.used" > "$metrics_dir/memory_used.json"
    curl -s "http://localhost:8080/actuator/metrics/system.cpu.usage" > "$metrics_dir/cpu_usage.json"
    curl -s "http://localhost:8080/actuator/metrics/hikaricp.connections.active" > "$metrics_dir/db_connections.json"
    
    # Collect system metrics
    top -l 1 > "$metrics_dir/system_top.txt"
    vmstat 1 5 > "$metrics_dir/vmstat.txt"
    
    echo "Metrics collection completed for $users users"
}

# Create summary file
SUMMARY_FILE="$TEST_DIR/test_summary.md"
echo "# Scalability Test Summary - $(date)" > "$SUMMARY_FILE"
echo "## Test Configuration" >> "$SUMMARY_FILE"
echo "- Duration per step: ${DURATION}s" >> "$SUMMARY_FILE"
echo "- Ramp-up time: ${RAMP_UP}s" >> "$SUMMARY_FILE"
echo "- User steps: ${USERS[*]}" >> "$SUMMARY_FILE"
echo "" >> "$SUMMARY_FILE"

for users in "${USERS[@]}"; do
    echo "Running test with $users concurrent users..."
    echo "## Test Run - $users Users" >> "$SUMMARY_FILE"
    
    # Update JMeter properties for current test
    sed -i '' "s/<stringProp name=\"ThreadGroup.num_threads\">[0-9]*<\/stringProp>/<stringProp name=\"ThreadGroup.num_threads\">$users<\/stringProp>/" \
        src/test/jmeter/load-test-plan.jmx
    
    # Run JMeter test with error handling
    if ! jmeter -n \
        -t src/test/jmeter/load-test-plan.jmx \
        -l "$TEST_DIR/results_${users}users.jtl" \
        -e -o "$TEST_DIR/dashboard_${users}users" \
        -j "$TEST_DIR/jmeter_${users}users.log"; then
        echo "Warning: JMeter test failed for $users users" | tee -a "$SUMMARY_FILE"
        continue
    fi
    
    # Collect metrics
    collect_metrics "$users"
    
    # Extract and log key metrics
    echo "### Performance Metrics" >> "$SUMMARY_FILE"
    echo "\`\`\`" >> "$SUMMARY_FILE"
    jmeter -n -j /dev/null -l "$TEST_DIR/results_${users}users.jtl" -e -g "$TEST_DIR/results_${users}users.jtl" 2>/dev/null | grep -E "summary|Error" >> "$SUMMARY_FILE"
    echo "\`\`\`" >> "$SUMMARY_FILE"
    echo "" >> "$SUMMARY_FILE"
    
    # Allow system to stabilize
    echo "Waiting for system to stabilize..."
    sleep 30
done

# Generate final summary and analysis
echo "## Final Analysis" >> "$SUMMARY_FILE"
echo "### Response Time Trend" >> "$SUMMARY_FILE"
echo "\`\`\`" >> "$SUMMARY_FILE"
echo "Users | Avg Response Time (ms) | 90th Percentile | Error Rate %" >> "$SUMMARY_FILE"
echo "------|---------------------|----------------|-------------" >> "$SUMMARY_FILE"

for users in "${USERS[@]}"; do
    if [ -f "$TEST_DIR/metrics_${users}users/response_time.json" ]; then
        avg_rt=$(jq -r '.measurements[] | select(.statistic=="MEAN") | .value' "$TEST_DIR/metrics_${users}users/response_time.json")
        p90_rt=$(jq -r '.measurements[] | select(.statistic=="p90") | .value' "$TEST_DIR/metrics_${users}users/response_time.json")
        errors=$(jq -r '.measurements[] | select(.statistic=="COUNT") | .value' "$TEST_DIR/metrics_${users}users/active_requests.json")
        
        # Convert to milliseconds and format
        avg_rt_ms=$(printf "%.2f" $(echo "$avg_rt * 1000" | bc))
        p90_rt_ms=$(printf "%.2f" $(echo "$p90_rt * 1000" | bc))
        
        echo "$users | $avg_rt_ms | $p90_rt_ms | ${errors:-N/A}" >> "$SUMMARY_FILE"
    else
        echo "$users | N/A | N/A | N/A" >> "$SUMMARY_FILE"
    fi
done
echo "\`\`\`" >> "$SUMMARY_FILE"

# Add system resource utilization summary
echo "### System Resource Utilization" >> "$SUMMARY_FILE"
echo "\`\`\`" >> "$SUMMARY_FILE"
echo "Users | CPU Usage % | Memory Used % | Active DB Connections" >> "$SUMMARY_FILE"
echo "------|------------|---------------|--------------------" >> "$SUMMARY_FILE"

for users in "${USERS[@]}"; do
    if [ -f "$TEST_DIR/metrics_${users}users/cpu_usage.json" ]; then
        cpu=$(jq -r '.measurements[] | select(.statistic=="VALUE") | .value' "$TEST_DIR/metrics_${users}users/cpu_usage.json")
        mem=$(jq -r '.measurements[] | select(.statistic=="VALUE") | .value' "$TEST_DIR/metrics_${users}users/memory_used.json")
        db_conn=$(jq -r '.measurements[] | select(.statistic=="VALUE") | .value' "$TEST_DIR/metrics_${users}users/db_connections.json")
        
        cpu_pct=$(printf "%.1f" $(echo "$cpu * 100" | bc))
        mem_pct=$(printf "%.1f" $(echo "$mem * 100" | bc))
        
        echo "$users | $cpu_pct | $mem_pct | ${db_conn:-N/A}" >> "$SUMMARY_FILE"
    else
        echo "$users | N/A | N/A | N/A" >> "$SUMMARY_FILE"
    fi
done
echo "\`\`\`" >> "$SUMMARY_FILE"

echo "Scalability testing completed. Results are in $TEST_DIR"
echo "Summary report available at $SUMMARY_FILE"

# Open the summary report if on macOS
if [[ "$OSTYPE" == "darwin"* ]]; then
    open "$SUMMARY_FILE"
fi