package com.flight.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.flight.validation.ValidAirlineCode;
import com.flight.validation.ValidAirportCode;
import com.flight.validation.ValidDateRange;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;
import java.util.List;

/**
 * Flight Search Request DTO
 * 
 * Represents a flight search request with validation for:
 * - Airport codes (IATA): LAX, JFK, LHR, CDG, DXB, SIN, ORD, SFO
 * - Airline codes (IATA): AA, UA, DL, BA, LH, AF, EK, SQ
 * - Date ranges: Departure must be future, return after departure
 * - Passenger counts: Non-negative integers
 */
@ValidDateRange
@Schema(description = "Flight search request with origin, destination, dates, passengers, and optional filters")
public class FlightSearchRequest {
    
    @NotBlank(message = "Origin airport IATA code is required")
    @ValidAirportCode
    @Schema(description = "Origin airport IATA code", example = "JFK", required = true, 
            allowableValues = {"LAX", "JFK", "LHR", "CDG", "DXB", "SIN", "ORD", "SFO"})
    private String origin;
    
    @NotBlank(message = "Destination airport IATA code is required")
    @ValidAirportCode
    @Schema(description = "Destination airport IATA code", example = "LHR", required = true,
            allowableValues = {"LAX", "JFK", "LHR", "CDG", "DXB", "SIN", "ORD", "SFO"})
    private String destination;
    
    @NotNull(message = "Departure date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Departure date in YYYY-MM-DD format. Must be a future date.", 
            example = "2024-06-15", required = true)
    private LocalDate departureDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "Return date for round-trip flights. Must be after departure date.", 
            example = "2024-06-22", required = false)
    private LocalDate returnDate;
    
    @NotNull(message = "Adult passenger count is required")
    @Min(value = 0, message = "Adult count must be non-negative")
    @Schema(description = "Number of adult passengers", example = "2", required = true, minimum = "0")
    private Integer adults;
    
    @Min(value = 0, message = "Children count must be non-negative")
    @Schema(description = "Number of children passengers", example = "1", defaultValue = "0", minimum = "0")
    private Integer children = 0;
    
    @Min(value = 0, message = "Infants count must be non-negative")
    @Schema(description = "Number of infant passengers", example = "0", defaultValue = "0", minimum = "0")
    private Integer infants = 0;
    
    @Schema(description = "Cabin class preference", example = "ECONOMY", 
            allowableValues = {"ECONOMY", "PREMIUM_ECONOMY", "BUSINESS", "FIRST"}, defaultValue = "ECONOMY")
    private CabinClass cabinClass = CabinClass.ECONOMY;
    
    @Schema(description = "Filter for direct flights only (no stops)", example = "false", defaultValue = "false")
    private Boolean directFlightsOnly = false;
    
    @Positive(message = "Max stops must be positive")
    @Schema(description = "Maximum number of stops allowed", example = "1", minimum = "1")
    private Integer maxStops;
    
    @ValidAirlineCode
    @Schema(description = "List of preferred airline IATA codes", example = "[\"EK\", \"SQ\", \"BA\"]",
            allowableValues = {"AA", "UA", "DL", "BA", "LH", "AF", "EK", "SQ"})
    private List<String> preferredAirlines;
    
    @Positive(message = "Max results must be positive")
    @Schema(description = "Maximum number of results to return", example = "50", defaultValue = "50", minimum = "1")
    private Integer maxResults = 50;

    // Getters and Setters
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Integer getAdults() {
        return adults;
    }

    public void setAdults(Integer adults) {
        this.adults = adults;
    }

    public Integer getChildren() {
        return children;
    }

    public void setChildren(Integer children) {
        this.children = children;
    }

    public Integer getInfants() {
        return infants;
    }

    public void setInfants(Integer infants) {
        this.infants = infants;
    }

    public CabinClass getCabinClass() {
        return cabinClass;
    }

    public void setCabinClass(CabinClass cabinClass) {
        this.cabinClass = cabinClass;
    }

    public Boolean getDirectFlightsOnly() {
        return directFlightsOnly;
    }

    public void setDirectFlightsOnly(Boolean directFlightsOnly) {
        this.directFlightsOnly = directFlightsOnly;
    }

    public Integer getMaxStops() {
        return maxStops;
    }

    public void setMaxStops(Integer maxStops) {
        this.maxStops = maxStops;
    }

    public List<String> getPreferredAirlines() {
        return preferredAirlines;
    }

    public void setPreferredAirlines(List<String> preferredAirlines) {
        this.preferredAirlines = preferredAirlines;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public enum CabinClass {
        ECONOMY,
        PREMIUM_ECONOMY,
        BUSINESS,
        FIRST
    }
}

