package com.nrjsingh1.system_design_experiment.service;

import io.micrometer.core.instrument.*;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ScalabilityMetricsService {
    private final MeterRegistry meterRegistry;
    private final AtomicInteger activeRequests;
    private final Timer responseTimeDistribution;
    private final Counter requestCounter;

    public ScalabilityMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.activeRequests = meterRegistry.gauge("system.active.requests", 
            new AtomicInteger(0));
        this.responseTimeDistribution = Timer.builder("system.response.time")
            .description("Response time distribution")
            .publishPercentileHistogram()
            .register(meterRegistry);
        this.requestCounter = Counter.builder("system.requests.total")
            .description("Total number of requests")
            .register(meterRegistry);
        
        // Register resource utilization gauges
        Gauge.builder("system.memory.usage", Runtime.getRuntime(), 
            runtime -> getMemoryUsage())
            .description("JVM memory usage percentage")
            .register(meterRegistry);
        
        Gauge.builder("system.thread.saturation", Runtime.getRuntime(),
            runtime -> Thread.activeCount())
            .description("Active thread count")
            .register(meterRegistry);
    }

    public void recordRequestStart() {
        activeRequests.incrementAndGet();
        requestCounter.increment();
    }

    public void recordRequestEnd() {
        activeRequests.decrementAndGet();
    }

    public Timer.Sample startTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopTimer(Timer.Sample sample) {
        sample.stop(responseTimeDistribution);
    }

    private double getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        return ((double) (totalMemory - freeMemory) / totalMemory) * 100.0;
    }
}