package com.nrjsingh1.system_design_experiment.controller;

import com.nrjsingh1.system_design_experiment.model.Product;
import com.nrjsingh1.system_design_experiment.dto.ProductDTO;
import com.nrjsingh1.system_design_experiment.repository.ProductRepository;
import com.nrjsingh1.system_design_experiment.service.ScalabilityMetricsService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Optional;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product", description = "The Product API")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MeterRegistry meterRegistry;

    @Autowired
    private ScalabilityMetricsService scalabilityMetrics;

    private final Timer productSearchTimer;

    @Autowired
    public ProductController(MeterRegistry registry) {
        this.meterRegistry = registry;
        this.productSearchTimer = Timer.builder("product.search.time")
                .description("Time taken to search for products")
                .register(registry);
    }

    @Operation(summary = "Get all products", description = "Returns a paginated list of all products")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    @Timed(value = "products.get.all", description = "Time taken to get all products")
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Size of page") @RequestParam(defaultValue = "10") int size) {
        scalabilityMetrics.recordRequestStart();
        Timer.Sample timer = scalabilityMetrics.startTimer();
        
        try {
            return productSearchTimer.record(() -> {
                Page<Product> productsPage = productRepository.findAll(PageRequest.of(page, size));
                List<ProductDTO> productDTOs = productsPage.getContent().stream()
                        .map(ProductDTO::fromEntity)
                        .collect(Collectors.toList());
                Page<ProductDTO> dtoPage = new PageImpl<>(productDTOs, productsPage.getPageable(), productsPage.getTotalElements());
                
                // Record metrics
                meterRegistry.counter("products.accessed.total").increment();
                meterRegistry.gauge("products.page.size", size);
                
                return ResponseEntity.ok(dtoPage);
            });
        } finally {
            scalabilityMetrics.stopTimer(timer);
            scalabilityMetrics.recordRequestEnd();
        }
    }

    @Operation(summary = "Get a product by its id")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the product",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class))),
        @ApiResponse(responseCode = "404", description = "Product not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "ID of product to be searched") @PathVariable Long id) {
        return productRepository.findById(id)
                .map(product -> ResponseEntity.ok(ProductDTO.fromEntity(product)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get products by category")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class))),
        @ApiResponse(responseCode = "404", description = "No products found in this category")
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(
            @Parameter(description = "Category to filter by") @PathVariable String category) {
        List<ProductDTO> productDTOs = productRepository.findByCategory(category).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDTOs);
    }

    @Operation(summary = "Get all available products", description = "Returns all products with stock > 0, ordered by stock level ascending")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved available products",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ProductDTO.class)))
    })
    @GetMapping("/available")
    @Timed(value = "products.get.available", description = "Time taken to get available products")
    public ResponseEntity<List<ProductDTO>> getAvailableProducts() {
        return productSearchTimer.record(() -> {
            List<ProductDTO> productDTOs = productRepository.findAvailableProductsOrderByStockAsc().stream()
                    .map(ProductDTO::fromEntity)
                    .collect(Collectors.toList());
            
            // Record metrics
            meterRegistry.counter("products.available.accessed").increment();
            meterRegistry.gauge("products.available.count", productDTOs.size());
            
            return ResponseEntity.ok(productDTOs);
        });
    }

    @Operation(summary = "Get products with low stock")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved low stock products",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Product.class)))
    })
    @GetMapping("/low-stock/{minStock}")
    public ResponseEntity<List<Product>> getLowStockProducts(
            @Parameter(description = "Minimum stock threshold") @PathVariable Integer minStock) {
        List<Product> products = productRepository.findByStockLessThan(minStock);
        return ResponseEntity.ok(products);
    }
}