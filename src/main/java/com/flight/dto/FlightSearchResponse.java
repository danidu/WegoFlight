package com.flight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Flight Search Response DTO
 * 
 * Contains aggregated flight search results with:
 * - Unique search identifier
 * - Ranked and deduplicated flight options
 * - Metadata about the search (providers queried, execution time, warnings)
 */
@Schema(description = "Flight search response with aggregated results from multiple providers")
public class FlightSearchResponse {
    
    private String searchId;
    private List<FlightOption> flightOptions;
    private SearchMetadata metadata;

    // Getters and Setters
    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public List<FlightOption> getFlightOptions() {
        return flightOptions;
    }

    public void setFlightOptions(List<FlightOption> flightOptions) {
        this.flightOptions = flightOptions;
    }

    public SearchMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(SearchMetadata metadata) {
        this.metadata = metadata;
    }

    public static class FlightOption {
        private List<FlightSegment> segments;
        private Pricing pricing;
        private List<String> providers;
        private Double rankingScore;

        // Getters and Setters
        public List<FlightSegment> getSegments() {
            return segments;
        }

        public void setSegments(List<FlightSegment> segments) {
            this.segments = segments;
        }

        public Pricing getPricing() {
            return pricing;
        }

        public void setPricing(Pricing pricing) {
            this.pricing = pricing;
        }

        public List<String> getProviders() {
            return providers;
        }

        public void setProviders(List<String> providers) {
            this.providers = providers;
        }

        public Double getRankingScore() {
            return rankingScore;
        }

        public void setRankingScore(Double rankingScore) {
            this.rankingScore = rankingScore;
        }
    }

    public static class FlightSegment {
        private String carrier;
        private String flightNumber;
        private String departureAirport;
        private String arrivalAirport;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime departureTime;
        
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime arrivalTime;
        
        private Integer durationMinutes;
        private String aircraftType;

        // Getters and Setters
        public String getCarrier() {
            return carrier;
        }

        public void setCarrier(String carrier) {
            this.carrier = carrier;
        }

        public String getFlightNumber() {
            return flightNumber;
        }

        public void setFlightNumber(String flightNumber) {
            this.flightNumber = flightNumber;
        }

        public String getDepartureAirport() {
            return departureAirport;
        }

        public void setDepartureAirport(String departureAirport) {
            this.departureAirport = departureAirport;
        }

        public String getArrivalAirport() {
            return arrivalAirport;
        }

        public void setArrivalAirport(String arrivalAirport) {
            this.arrivalAirport = arrivalAirport;
        }

        public LocalDateTime getDepartureTime() {
            return departureTime;
        }

        public void setDepartureTime(LocalDateTime departureTime) {
            this.departureTime = departureTime;
        }

        public LocalDateTime getArrivalTime() {
            return arrivalTime;
        }

        public void setArrivalTime(LocalDateTime arrivalTime) {
            this.arrivalTime = arrivalTime;
        }

        public Integer getDurationMinutes() {
            return durationMinutes;
        }

        public void setDurationMinutes(Integer durationMinutes) {
            this.durationMinutes = durationMinutes;
        }

        public String getAircraftType() {
            return aircraftType;
        }

        public void setAircraftType(String aircraftType) {
            this.aircraftType = aircraftType;
        }
    }

    public static class Pricing {
        private Double totalPrice;
        private String currency;
        private PriceBreakdown breakdown;
        private Double taxesAndFees;

        // Getters and Setters
        public Double getTotalPrice() {
            return totalPrice;
        }

        public void setTotalPrice(Double totalPrice) {
            this.totalPrice = totalPrice;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        public PriceBreakdown getBreakdown() {
            return breakdown;
        }

        public void setBreakdown(PriceBreakdown breakdown) {
            this.breakdown = breakdown;
        }

        public Double getTaxesAndFees() {
            return taxesAndFees;
        }

        public void setTaxesAndFees(Double taxesAndFees) {
            this.taxesAndFees = taxesAndFees;
        }
    }

    public static class PriceBreakdown {
        private Double adults;
        private Double children;
        private Double infants;

        // Getters and Setters
        public Double getAdults() {
            return adults;
        }

        public void setAdults(Double adults) {
            this.adults = adults;
        }

        public Double getChildren() {
            return children;
        }

        public void setChildren(Double children) {
            this.children = children;
        }

        public Double getInfants() {
            return infants;
        }

        public void setInfants(Double infants) {
            this.infants = infants;
        }
    }

    public static class SearchMetadata {
        private List<String> queriedProviders;
        private List<String> respondedProviders;
        private Long executionTimeMs;
        private List<String> warnings;

        // Getters and Setters
        public List<String> getQueriedProviders() {
            return queriedProviders;
        }

        public void setQueriedProviders(List<String> queriedProviders) {
            this.queriedProviders = queriedProviders;
        }

        public List<String> getRespondedProviders() {
            return respondedProviders;
        }

        public void setRespondedProviders(List<String> respondedProviders) {
            this.respondedProviders = respondedProviders;
        }

        public Long getExecutionTimeMs() {
            return executionTimeMs;
        }

        public void setExecutionTimeMs(Long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public void setWarnings(List<String> warnings) {
            this.warnings = warnings;
        }
    }
}

