package com.flight.service;

import com.flight.dto.FlightSearchRequest;
import com.flight.dto.FlightSearchResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Flight Search Aggregator Service
 * 
 * Key Business Logic:
 * 1. Query multiple flight data providers in parallel (Amadeus, Sabre, Travelport)
 * 2. Aggregate results from all responding providers
 * 3. Identify and deduplicate the same flight appearing from multiple providers 
 *    (same carrier, flight number, and departure time)
 * 4. Normalize different provider data formats into a consistent structure (FlightOption)
 * 5. Filter results based on customer preferences (direct flights, maximum stops, preferred airlines)
 * 6. Rank results by a combination of factors: price, duration, number of stops, and provider reliability
 * 7. Return the top N results based on the requested limit
 * 
 * Error Handling:
 * - Invalid input data: Handled by validation annotations and GlobalExceptionHandler
 * - Rate limiting: Handled by RateLimitFilter (returns 429 Too Many Requests)
 * - All providers unavailable: Returns empty results with warning message
 */
@Service
public class FlightSearchAggregatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(FlightSearchAggregatorService.class);
    
    private final List<FlightAggregatorProvider> providers;
    private final ExecutorService executorService;
    
    public FlightSearchAggregatorService(List<FlightAggregatorProvider> providers) {
        this.providers = providers;
        this.executorService = Executors.newFixedThreadPool(providers.size());
    }
    
    /**
     * Main search method that orchestrates the entire flight search process
     */
    public FlightSearchResponse searchFlights(FlightSearchRequest request) {
        long startTime = System.currentTimeMillis();
        String searchId = UUID.randomUUID().toString();
        
        List<String> queriedProviders = providers.stream()
            .map(FlightAggregatorProvider::getProviderName)
            .collect(Collectors.toList());
        
        List<String> respondedProviders = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<FlightSearchResponse.FlightOption> allFlightOptions = new ArrayList<>();
        
        // Search all providers in parallel
        List<CompletableFuture<ProviderResult>> futures = providers.stream()
            .map(provider -> CompletableFuture.supplyAsync(() -> 
                searchProvider(provider, request), executorService))
            .collect(Collectors.toList());
        
        // Collect results
        for (CompletableFuture<ProviderResult> future : futures) {
            try {
                ProviderResult result = future.get();
                if (result.success) {
                    respondedProviders.add(result.providerName);
                    allFlightOptions.addAll(result.options);
                } else {
                    warnings.add(result.providerName + ": " + result.errorMessage);
                }
            } catch (Exception e) {
                logger.error("Error getting provider result", e);
                warnings.add("Error processing provider result: " + e.getMessage());
            }
        }
        
        // Handle case when all providers are unavailable
        if (respondedProviders.isEmpty()) {
            logger.error("All providers unavailable. Queried: {}, Warnings: {}", queriedProviders, warnings);
            if (warnings.isEmpty()) {
                warnings.add("All flight providers are currently unavailable. Please try again later.");
            } else {
                warnings.add(0, "All flight providers are currently unavailable. Please try again later.");
            }
        }
        
        // Filter flight options based on customer preferences
        List<FlightSearchResponse.FlightOption> filteredOptions = filterFlightOptions(allFlightOptions, request);
        
        // Normalize and deduplicate flight options (same carrier, flight number, departure time)
        List<FlightSearchResponse.FlightOption> deduplicatedOptions = deduplicateFlights(filteredOptions);
        
        // Rank results by price, duration, number of stops, and provider reliability
        List<FlightSearchResponse.FlightOption> rankedOptions = rankFlightOptions(deduplicatedOptions, request);
        
        // Return top N results based on requested limit
        if (request.getMaxResults() != null && rankedOptions.size() > request.getMaxResults()) {
            rankedOptions = rankedOptions.subList(0, request.getMaxResults());
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        
        // Build response
        FlightSearchResponse response = new FlightSearchResponse();
        response.setSearchId(searchId);
        response.setFlightOptions(rankedOptions);
        
        FlightSearchResponse.SearchMetadata metadata = new FlightSearchResponse.SearchMetadata();
        metadata.setQueriedProviders(queriedProviders);
        metadata.setRespondedProviders(respondedProviders);
        metadata.setExecutionTimeMs(executionTime);
        metadata.setWarnings(warnings.isEmpty() ? null : warnings);
        response.setMetadata(metadata);
        
        return response;
    }
    
    private ProviderResult searchProvider(FlightAggregatorProvider provider, FlightSearchRequest request) {
        try {
            List<FlightSearchResponse.FlightOption> options = provider.searchFlights(request);
            return new ProviderResult(provider.getProviderName(), options, true, null);
        } catch (Exception e) {
            logger.error("Error searching flights with provider: {}", provider.getProviderName(), e);
            return new ProviderResult(provider.getProviderName(), Collections.emptyList(), false, e.getMessage());
        }
    }
    
    /**
     * Filter flight options based on customer preferences:
     * - Direct flights only
     * - Maximum number of stops
     * - Preferred airlines
     */
    private List<FlightSearchResponse.FlightOption> filterFlightOptions(
            List<FlightSearchResponse.FlightOption> options, FlightSearchRequest request) {
        
        return options.stream()
            .filter(option -> {
                if (option.getSegments() == null || option.getSegments().isEmpty()) {
                    return false;
                }
                
                // Filter: Direct flights only
                if (request.getDirectFlightsOnly() != null && request.getDirectFlightsOnly()) {
                    // Direct flight = only one segment for one-way, or two segments for round-trip
                    int segmentCount = option.getSegments().size();
                    if (request.getReturnDate() != null) {
                        // Round trip: should have exactly 2 segments (outbound + return)
                        if (segmentCount != 2) {
                            return false;
                        }
                    } else {
                        // One way: should have exactly 1 segment
                        if (segmentCount != 1) {
                            return false;
                        }
                    }
                }
                
                // Filter: Maximum number of stops
                if (request.getMaxStops() != null) {
                    // Calculate stops: segments - 1 for one-way, or (segments/2 - 1) for round-trip
                    int stops;
                    if (request.getReturnDate() != null) {
                        // Round trip: check outbound stops (first half of segments)
                        int outboundSegments = option.getSegments().size() / 2;
                        stops = outboundSegments - 1;
                    } else {
                        // One way: stops = segments - 1
                        stops = option.getSegments().size() - 1;
                    }
                    if (stops > request.getMaxStops()) {
                        return false;
                    }
                }
                
                // Filter: Preferred airlines
                if (request.getPreferredAirlines() != null && !request.getPreferredAirlines().isEmpty()) {
                    boolean hasPreferredAirline = option.getSegments().stream()
                        .anyMatch(segment -> segment.getCarrier() != null && 
                            request.getPreferredAirlines().contains(segment.getCarrier()));
                    if (!hasPreferredAirline) {
                        return false;
                    }
                }
                
                return true;
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Deduplicate flights: same carrier, flight number, and departure time
     * Merge providers when duplicates are found
     */
    private List<FlightSearchResponse.FlightOption> deduplicateFlights(
            List<FlightSearchResponse.FlightOption> options) {
        
        Map<String, FlightSearchResponse.FlightOption> uniqueOptions = new LinkedHashMap<>();
        
        for (FlightSearchResponse.FlightOption option : options) {
            String key = generateFlightKey(option);
            if (!uniqueOptions.containsKey(key)) {
                uniqueOptions.put(key, option);
            } else {
                // Merge providers if duplicate (same carrier, flight number, departure time)
                FlightSearchResponse.FlightOption existing = uniqueOptions.get(key);
                Set<String> allProviders = new HashSet<>(existing.getProviders());
                if (option.getProviders() != null) {
                    allProviders.addAll(option.getProviders());
                }
                existing.setProviders(new ArrayList<>(allProviders));
            }
        }
        
        return new ArrayList<>(uniqueOptions.values());
    }
    
    /**
     * Rank flight options by:
     * - Price (lower is better)
     * - Duration (shorter is better)
     * - Number of stops (fewer is better)
     * - Provider reliability (more providers = more reliable)
     */
    private List<FlightSearchResponse.FlightOption> rankFlightOptions(
            List<FlightSearchResponse.FlightOption> options, FlightSearchRequest request) {
        
        // Calculate ranking scores
        for (FlightSearchResponse.FlightOption option : options) {
            option.setRankingScore(calculateRankingScore(option, request));
        }
        
        // Sort by ranking score (higher is better)
        options.sort((a, b) -> Double.compare(
            b.getRankingScore() != null ? b.getRankingScore() : 0.0,
            a.getRankingScore() != null ? a.getRankingScore() : 0.0
        ));
        
        return options;
    }
    
    private String generateFlightKey(FlightSearchResponse.FlightOption option) {
        // Generate a unique key based on flight segments
        if (option.getSegments() == null || option.getSegments().isEmpty()) {
            return "empty-" + System.currentTimeMillis();
        }
        return option.getSegments().stream()
            .map(s -> s.getCarrier() + "-" + s.getFlightNumber() + "-" + 
                      s.getDepartureAirport() + "-" + s.getArrivalAirport() + "-" +
                      (s.getDepartureTime() != null ? s.getDepartureTime().toString() : ""))
            .collect(Collectors.joining("|"));
    }
    
    /**
     * Calculate ranking score based on:
     * 1. Price (lower is better) - 40% weight
     * 2. Duration (shorter is better) - 30% weight
     * 3. Number of stops (fewer is better) - 20% weight
     * 4. Provider reliability (more providers = more reliable) - 10% weight
     */
    private Double calculateRankingScore(FlightSearchResponse.FlightOption option, FlightSearchRequest request) {
        double score = 0.0;
        
        // 1. Price factor (40% weight) - Lower price = higher score
        if (option.getPricing() != null && option.getPricing().getTotalPrice() != null) {
            double price = option.getPricing().getTotalPrice();
            // Normalize: higher price gets lower score (inverse relationship)
            // Using formula: 1000 / (1 + price) to give higher scores for lower prices
            score += (1000.0 / (1.0 + price)) * 0.4;
        }
        
        // 2. Duration factor (30% weight) - Shorter duration = higher score
        if (option.getSegments() != null && !option.getSegments().isEmpty()) {
            int totalDurationMinutes = option.getSegments().stream()
                .mapToInt(segment -> segment.getDurationMinutes() != null ? segment.getDurationMinutes() : 0)
                .sum();
            
            if (totalDurationMinutes > 0) {
                // Normalize: shorter duration gets higher score
                // Assuming max duration of 24 hours (1440 minutes) for normalization
                double durationScore = (1440.0 / (1.0 + totalDurationMinutes));
                score += durationScore * 0.3;
            }
        }
        
        // 3. Number of stops factor (20% weight) - Fewer stops = higher score
        if (option.getSegments() != null && !option.getSegments().isEmpty()) {
            int stops;
            if (request.getReturnDate() != null) {
                // Round trip: calculate stops for outbound journey
                int outboundSegments = option.getSegments().size() / 2;
                stops = Math.max(0, outboundSegments - 1);
            } else {
                // One way: stops = segments - 1
                stops = Math.max(0, option.getSegments().size() - 1);
            }
            
            // Fewer stops = higher score (direct flights get max score)
            double stopsScore = stops == 0 ? 100.0 : (100.0 / (1.0 + stops));
            score += stopsScore * 0.2;
        }
        
        // 4. Provider reliability factor (10% weight) - More providers = more reliable
        if (option.getProviders() != null && !option.getProviders().isEmpty()) {
            // More providers means the flight was found by multiple sources (more reliable)
            double reliabilityScore = option.getProviders().size() * 25.0; // Max 3 providers = 75 points
            score += reliabilityScore * 0.1;
        }
        
        // Bonus: Preferred airlines (additional 5 points)
        if (request.getPreferredAirlines() != null && !request.getPreferredAirlines().isEmpty() 
            && option.getSegments() != null) {
            boolean hasPreferred = option.getSegments().stream()
                .anyMatch(s -> s.getCarrier() != null && request.getPreferredAirlines().contains(s.getCarrier()));
            if (hasPreferred) {
                score += 5.0;
            }
        }
        
        return score;
    }
    
    private static class ProviderResult {
        final String providerName;
        final List<FlightSearchResponse.FlightOption> options;
        final boolean success;
        final String errorMessage;
        
        ProviderResult(String providerName, List<FlightSearchResponse.FlightOption> options, 
                      boolean success, String errorMessage) {
            this.providerName = providerName;
            this.options = options;
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }
}

