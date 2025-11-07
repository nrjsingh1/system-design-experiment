package com.nrjsingh1.system_design_experiment.controller;

import com.nrjsingh1.system_design_experiment.model.Order;
import com.nrjsingh1.system_design_experiment.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order", description = "The Order API")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    @Operation(summary = "Get all orders", description = "Returns a paginated list of all orders, sorted by order date descending")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved orders",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<Order>> getAllOrders(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Size of page") @RequestParam(defaultValue = "10") int size) {
        Page<Order> orders = orderRepository.findAll(
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate")));
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get an order by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the order",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Order.class))),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(
            @Parameter(description = "ID of order to be searched") @PathVariable Long id) {
        Optional<Order> order = orderRepository.findById(id);
        return order.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get orders by customer ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved customer orders",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Order.class)))
    })
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Order>> getOrdersByCustomerId(
            @Parameter(description = "ID of customer to find orders for") @PathVariable Long customerId) {
        List<Order> orders = orderRepository.findByCustomerId(customerId);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get orders by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved orders",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Order.class)))
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Order>> getOrdersByStatus(
            @Parameter(description = "Status to filter by") @PathVariable Order.OrderStatus status) {
        List<Order> orders = orderRepository.findByStatus(status);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get stale orders", description = "Returns orders in a specific status that haven't been updated since a given time")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved stale orders",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Order.class)))
    })
    @GetMapping("/stale")
    public ResponseEntity<List<Order>> getStaleOrders(
            @Parameter(description = "Status to filter by") @RequestParam Order.OrderStatus status,
            @Parameter(description = "Orders older than this date/time") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime before) {
        List<Order> orders = orderRepository.findStaleOrders(status, before);
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "Get orders needing attention", description = "Returns PENDING or PROCESSING orders older than the cutoff date")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved orders needing attention",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Order.class)))
    })
    @GetMapping("/needs-attention")
    public ResponseEntity<List<Order>> getOrdersNeedingAttention(
            @Parameter(description = "Orders older than this date/time") 
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) @RequestParam LocalDateTime cutoffDate) {
        List<Order> orders = orderRepository.findOrdersNeedingAttention(cutoffDate);
        return ResponseEntity.ok(orders);
    }
}