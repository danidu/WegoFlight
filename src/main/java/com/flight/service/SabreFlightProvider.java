package com.flight.service;

import com.flight.dto.FlightSearchRequest;
import com.flight.dto.FlightSearchResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class SabreFlightProvider implements FlightAggregatorProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(SabreFlightProvider.class);
    
    private final WebClient webClient;
    
    @Value("${aggregator.sabre.base-url:https://api.sabre.com}")
    private String baseUrl;
    
    @Value("${aggregator.sabre.timeout-seconds:10}")
    private int timeoutSeconds;
    
    public SabreFlightProvider(WebClient.Builder webClientBuilder,
                               @Value("${aggregator.sabre.base-url:https://api.sabre.com}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.webClient = webClientBuilder
            .baseUrl(baseUrl)
            .build();
    }
    
    @Override
    public String getProviderName() {
        return "Sabre";
    }
    
    @Override
    @Retry(name = "flightProvider", fallbackMethod = "retryFallbackSearch")
    @CircuitBreaker(name = "flightProvider", fallbackMethod = "fallbackSearch")
    @Cacheable(value = "flightSearchCache", key = "#request.origin + '-' + #request.destination + '-' + #request.departureDate + '-sabre'")
    public List<FlightSearchResponse.FlightOption> searchFlights(FlightSearchRequest request) {
        // Log request
        logger.info("[SABRE] Request received - Origin: {}, Destination: {}, DepartureDate: {}, ReturnDate: {}, Adults: {}, Children: {}, Infants: {}, CabinClass: {}, DirectFlightsOnly: {}, MaxStops: {}, PreferredAirlines: {}, MaxResults: {}",
            request.getOrigin(),
            request.getDestination(),
            request.getDepartureDate(),
            request.getReturnDate() != null ? request.getReturnDate() : "N/A",
            request.getAdults(),
            request.getChildren() != null ? request.getChildren() : 0,
            request.getInfants() != null ? request.getInfants() : 0,
            request.getCabinClass(),
            request.getDirectFlightsOnly() != null ? request.getDirectFlightsOnly() : false,
            request.getMaxStops() != null ? request.getMaxStops() : "N/A",
            request.getPreferredAirlines() != null ? request.getPreferredAirlines() : "N/A",
            request.getMaxResults() != null ? request.getMaxResults() : 50
        );
        
        long startTime = System.currentTimeMillis();
        
        // Simulate API delay
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        List<FlightSearchResponse.FlightOption> results = generateDummyFlights(request, "Sabre");
        long duration = System.currentTimeMillis() - startTime;
        
        // Log response
        logger.info("[SABRE] Response sent - Status: SUCCESS, FlightOptionsCount: {}, Duration: {}ms, TotalPriceRange: ${}-${}",
            results.size(),
            duration,
            results.isEmpty() ? "N/A" : String.format("%.2f", results.stream()
                .mapToDouble(opt -> opt.getPricing() != null && opt.getPricing().getTotalPrice() != null 
                    ? opt.getPricing().getTotalPrice() : 0.0)
                .min().orElse(0.0)),
            results.isEmpty() ? "N/A" : String.format("%.2f", results.stream()
                .mapToDouble(opt -> opt.getPricing() != null && opt.getPricing().getTotalPrice() != null 
                    ? opt.getPricing().getTotalPrice() : 0.0)
                .max().orElse(0.0))
        );
        
        return results;
    }
    
    /**
     * Retry fallback - called when all retry attempts are exhausted
     */
    public List<FlightSearchResponse.FlightOption> retryFallbackSearch(FlightSearchRequest request, Exception e) {
        logger.warn("[SABRE] Retry exhausted - Request: Origin={}, Destination={}, DepartureDate={}, Attempts: 3, Error: {}", 
            request.getOrigin(), request.getDestination(), request.getDepartureDate(), e.getMessage());
        // Re-throw to trigger circuit breaker fallback
        throw new RuntimeException("Sabre API retry exhausted: " + e.getMessage(), e);
    }
    
    /**
     * Circuit breaker fallback - called when circuit is open or retry fails
     */
    public List<FlightSearchResponse.FlightOption> fallbackSearch(FlightSearchRequest request, Exception e) {
        logger.warn("[SABRE] Circuit breaker opened - Request: Origin={}, Destination={}, DepartureDate={}, Error: {}", 
            request.getOrigin(), request.getDestination(), request.getDepartureDate(), e.getMessage());
        logger.warn("[SABRE] Response sent - Status: FAILED (Circuit Breaker), FlightOptionsCount: 0, Error: {}", e.getMessage());
        return Collections.emptyList();
    }
    
    private List<FlightSearchResponse.FlightOption> generateDummyFlights(FlightSearchRequest request, String provider) {
        List<FlightSearchResponse.FlightOption> options = new ArrayList<>();
        
        // Generate different flight options with different carriers
        String[] carriers = {"AA", "DL", "UA", "AS", "WN"};
        String[] aircraftTypes = {"Boeing 737", "Airbus A320", "Boeing 767", "Airbus A330"};
        
        for (int i = 0; i < 3; i++) {
            FlightSearchResponse.FlightOption option = new FlightSearchResponse.FlightOption();
            
            // Create segments
            List<FlightSearchResponse.FlightSegment> segments = new ArrayList<>();
            String carrier = carriers[i % carriers.length];
            
            LocalDateTime departure = request.getDepartureDate().atTime(8 + i * 4, 30);
            LocalDateTime arrival = departure.plusHours(7 + i * 2).plusMinutes(45);
            
            FlightSearchResponse.FlightSegment segment = new FlightSearchResponse.FlightSegment();
            segment.setCarrier(carrier);
            segment.setFlightNumber(carrier + (300 + i * 15));
            segment.setDepartureAirport(request.getOrigin());
            segment.setArrivalAirport(request.getDestination());
            segment.setDepartureTime(departure);
            segment.setArrivalTime(arrival);
            segment.setDurationMinutes((int) java.time.Duration.between(departure, arrival).toMinutes());
            segment.setAircraftType(aircraftTypes[i % aircraftTypes.length]);
            segments.add(segment);
            
            // Add return segment if return date is provided
            if (request.getReturnDate() != null) {
                LocalDateTime returnDeparture = request.getReturnDate().atTime(10 + i * 2, 15);
                LocalDateTime returnArrival = returnDeparture.plusHours(8 + i).plusMinutes(30);
                
                FlightSearchResponse.FlightSegment returnSegment = new FlightSearchResponse.FlightSegment();
                returnSegment.setCarrier(carrier);
                returnSegment.setFlightNumber(carrier + (400 + i * 15));
                returnSegment.setDepartureAirport(request.getDestination());
                returnSegment.setArrivalAirport(request.getOrigin());
                returnSegment.setDepartureTime(returnDeparture);
                returnSegment.setArrivalTime(returnArrival);
                returnSegment.setDurationMinutes((int) java.time.Duration.between(returnDeparture, returnArrival).toMinutes());
                returnSegment.setAircraftType(aircraftTypes[(i + 2) % aircraftTypes.length]);
                segments.add(returnSegment);
            }
            
            option.setSegments(segments);
            
            // Create pricing (slightly different from Amadeus)
            FlightSearchResponse.Pricing pricing = new FlightSearchResponse.Pricing();
            double basePrice = 380.0 + (i * 90.0);
            double totalPrice = basePrice * (request.getAdults() != null ? request.getAdults() : 1) +
                               (basePrice * 0.7) * (request.getChildren() != null ? request.getChildren() : 0) +
                               (basePrice * 0.1) * (request.getInfants() != null ? request.getInfants() : 0);
            double taxes = totalPrice * 0.18;
            
            pricing.setTotalPrice(totalPrice + taxes);
            pricing.setCurrency("USD");
            pricing.setTaxesAndFees(taxes);
            
            FlightSearchResponse.PriceBreakdown breakdown = new FlightSearchResponse.PriceBreakdown();
            breakdown.setAdults(basePrice * (request.getAdults() != null ? request.getAdults() : 1));
            breakdown.setChildren((basePrice * 0.7) * (request.getChildren() != null ? request.getChildren() : 0));
            breakdown.setInfants((basePrice * 0.1) * (request.getInfants() != null ? request.getInfants() : 0));
            pricing.setBreakdown(breakdown);
            
            option.setPricing(pricing);
            option.setProviders(List.of(provider));
            
            options.add(option);
        }
        
        return options;
    }
}

