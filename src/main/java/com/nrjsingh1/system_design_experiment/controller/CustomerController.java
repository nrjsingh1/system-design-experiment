package com.nrjsingh1.system_design_experiment.controller;

import com.nrjsingh1.system_design_experiment.model.Customer;
import com.nrjsingh1.system_design_experiment.repository.CustomerRepository;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

import java.util.Optional;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customer", description = "The Customer API")
public class CustomerController {

    @Autowired
    private CustomerRepository customerRepository;

    @Operation(summary = "Get all customers", description = "Returns a paginated list of all customers")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved customers",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Page.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<Customer>> getAllCustomers(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Size of page") @RequestParam(defaultValue = "10") int size) {
        Page<Customer> customers = customerRepository.findAll(PageRequest.of(page, size));
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Get a customer by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the customer",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Customer.class))),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(
            @Parameter(description = "ID of customer to be searched") @PathVariable Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Search customers by last name", description = "Returns a list of customers with the specified last name, ordered by first name")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved customers",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Customer.class)))
    })
    @GetMapping("/search/{lastName}")
    public ResponseEntity<List<Customer>> searchCustomersByLastName(
            @Parameter(description = "Last name to search for") @PathVariable String lastName) {
        List<Customer> customers = customerRepository.findByLastNameOrderByFirstNameAsc(lastName);
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Get a customer by email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Found the customer",
                content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Customer.class))),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<Customer> getCustomerByEmail(
            @Parameter(description = "Email of customer to be searched") @PathVariable String email) {
        Optional<Customer> customer = customerRepository.findByEmail(email);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}