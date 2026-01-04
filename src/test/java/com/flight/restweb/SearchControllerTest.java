package com.flight.restweb;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flight.dto.FlightSearchRequest;
import com.flight.dto.FlightSearchResponse;
import com.flight.service.FlightSearchAggregatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FlightSearchAggregatorService aggregatorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testValidRequest() throws Exception {
        // Given
        FlightSearchRequest request = new FlightSearchRequest();
        request.setOrigin("JFK");
        request.setDestination("LHR");
        // Use a future date (at least tomorrow)
        request.setDepartureDate(LocalDate.now().plusDays(1));
        request.setAdults(2);
        request.setChildren(1);

        FlightSearchResponse response = new FlightSearchResponse();
        response.setSearchId("test-search-id");
        response.setFlightOptions(new ArrayList<>());

        when(aggregatorService.searchFlights(any(FlightSearchRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.searchId").value("test-search-id"))
                .andExpect(jsonPath("$.flightOptions").isArray());
    }

    @Test
    void testInvalidAirportCode() throws Exception {
        // Given
        FlightSearchRequest request = new FlightSearchRequest();
        request.setOrigin("XXX"); // Invalid airport code
        request.setDestination("LHR");
        request.setDepartureDate(LocalDate.now().plusDays(1));
        request.setAdults(2);

        // When & Then
        mockMvc.perform(post("/api/v1/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").exists());
    }

    @Test
    void testMissingRequiredFields() throws Exception {
        // Given
        FlightSearchRequest request = new FlightSearchRequest();
        // Missing origin, destination, departureDate, adults

        // When & Then
        mockMvc.perform(post("/api/v1/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void testInvalidDateRange() throws Exception {
        // Given
        FlightSearchRequest request = new FlightSearchRequest();
        request.setOrigin("JFK");
        request.setDestination("LHR");
        LocalDate futureDate = LocalDate.now().plusDays(10);
        request.setDepartureDate(futureDate);
        request.setReturnDate(futureDate.minusDays(5)); // Return before departure
        request.setAdults(2);

        // When & Then
        mockMvc.perform(post("/api/v1/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    void testInvalidPassengerCount() throws Exception {
        // Given
        FlightSearchRequest request = new FlightSearchRequest();
        request.setOrigin("JFK");
        request.setDestination("LHR");
        request.setDepartureDate(LocalDate.now().plusDays(1));
        request.setAdults(-1); // Negative adults

        // When & Then
        mockMvc.perform(post("/api/v1/flights/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}

