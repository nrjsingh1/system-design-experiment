#!/bin/bash

# Create results directory if it doesn't exist
RESULTS_DIR="src/test/jmeter/results"
mkdir -p "$RESULTS_DIR"

# Get timestamp for unique report directory
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
TEST_DIR="$RESULTS_DIR/test_$TIMESTAMP"
mkdir -p "$TEST_DIR"

# Run JMeter test
jmeter -n \
    -t src/test/jmeter/load-test-plan.jmx \
    -l "$TEST_DIR/results.jtl" \
    -e -o "$TEST_DIR/dashboard" \
    -j "$TEST_DIR/jmeter.log"

echo "Test completed. Results are in $TEST_DIR"
echo "Dashboard report is available at $TEST_DIR/dashboard/index.html"