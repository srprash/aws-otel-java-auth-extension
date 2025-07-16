package org.example.myspringbootapp;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @Autowired
    private OpenTelemetry openTelemetry;

    @GetMapping("/hello")
    public String sayHello() {
        // Get the tracer
        Tracer tracer = openTelemetry.getTracer("org.example.myspringbootapp");
        
        // Create a span
        Span span = tracer.spanBuilder("hello-operation")
                .setAttribute("http.method", "GET")
                .setAttribute("http.route", "/hello")
                .setAttribute("custom.attribute", "manual-instrumentation")
                .startSpan();
        
        try (var scope = span.makeCurrent()) {
            // Add some span events
            span.addEvent("Starting hello operation");
            
            // Simulate some work
            try {
                Thread.sleep(100); // Simulate processing time
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            span.addEvent("Hello operation completed");
            
            return "Hello from Spring Boot with manual OpenTelemetry instrumentation!";
        } finally {
            span.end();
        }
    }
} 