package com.flight.service;

import com.flight.dto.FlightSearchRequest;
import com.flight.dto.FlightSearchResponse;

import java.util.List;

public interface FlightAggregatorProvider {
    
    String getProviderName();
    
    List<FlightSearchResponse.FlightOption> searchFlights(FlightSearchRequest request);
}

