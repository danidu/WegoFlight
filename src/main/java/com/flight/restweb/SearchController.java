package com.flight.restweb;

import com.flight.dto.FlightSearchRequest;
import com.flight.dto.FlightSearchResponse;
import com.flight.service.FlightSearchAggregatorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Flight Search Controller
 * 
 * Provides REST API endpoint for searching flights across multiple aggregators.
 * 
 * Features:
 * - Parallel querying of multiple providers (Amadeus, Sabre, Travelport)
 * - Intelligent ranking and filtering
 * - Rate limiting protection
 * - Comprehensive error handling
 */
@RestController
@RequestMapping("/api/v1/flights")
@Tag(name = "Flight Search", description = "Search for flights across multiple aggregators")
public class SearchController {

    private final FlightSearchAggregatorService aggregatorService;

    public SearchController(FlightSearchAggregatorService aggregatorService) {
        this.aggregatorService = aggregatorService;
    }

    /**
     * Search for flights
     * 
     * Queries multiple flight aggregators in parallel and returns aggregated, ranked results.
     * 
     * @param request Flight search request with origin, destination, dates, passengers, and filters
     * @return Flight search response with ranked options and metadata
     */
    @Operation(
        summary = "Search for flights",
        description = "Searches across multiple flight aggregators (Amadeus, Sabre, Travelport) in parallel. " +
                     "Results are deduplicated, filtered, ranked, and returned based on the request parameters. " +
                     "The ranking algorithm considers price (40%), duration (30%), number of stops (20%), and provider reliability (10%)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully",
            content = @Content(schema = @Schema(implementation = FlightSearchResponse.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid input data (bad airport codes, invalid dates, invalid passenger counts)",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Rate limit exceeded. Too many requests.",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))
        )
    })
    @PostMapping("/search")
    public ResponseEntity<FlightSearchResponse> search(@Valid @RequestBody FlightSearchRequest request) {
        FlightSearchResponse response = aggregatorService.searchFlights(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    /**
     * Error response schema for Swagger documentation
     */
    private static class ErrorResponse {
        public String error;
        public String message;
        public Object details;
    }
}
