package org.fsk.redisasdatabase.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import io.micrometer.core.instrument.Timer;
import org.fsk.redisasdatabase.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.MeterRegistry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/performance")
@RequiredArgsConstructor
@Slf4j
public class PerformanceTestController {

    private final OrderService orderService;
    private final MeterRegistry meterRegistry;

    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> runPerformanceTest(
            @RequestParam(defaultValue = "1000") int requestCount,
            @RequestParam(defaultValue = "10") int concurrentUsers) {
        
        Timer timer = meterRegistry.timer("performance.test");
        
        return timer.record(() -> {
            ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
            CountDownLatch latch = new CountDownLatch(requestCount);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);
            
            long startTime = System.currentTimeMillis();
            
            for (int i = 0; i < requestCount; i++) {
                executor.submit(() -> {
                    try {
                        orderService.demonstrateTransactionIssues();
                        successCount.incrementAndGet();
                        meterRegistry.counter("performance.test.success").increment();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        meterRegistry.counter("performance.test.error").increment();
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            executor.shutdown();
            
            Map<String, Object> result = new HashMap<>();
            result.put("totalRequests", requestCount);
            result.put("successCount", successCount.get());
            result.put("errorCount", errorCount.get());
            result.put("durationMs", duration);
            result.put("requestsPerSecond", (double) requestCount / (duration / 1000.0));
            
            return ResponseEntity.ok(result);
        });
    }
}
