package com.roommanager.domain.services;

import com.roommanager.domain.calculator.AvailabilityCalculator;
import com.roommanager.domain.model.RoomsAvailabilityQuery;
import com.roommanager.remote.api.RoomsAvailabilityRequest;
import com.roommanager.remote.api.RoomsAvailabilityResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RoomsAvailabilitySvc {

  private final AvailabilityCalculator availabilityCalculator;

  public RoomsAvailabilitySvc(AvailabilityCalculator availabilityCalculator) {
    this.availabilityCalculator = availabilityCalculator;
  }

  public List<RoomsAvailabilityResponse> calculateAvailability(RoomsAvailabilityRequest request) {
    log.info("Rooms availability request received:{}", request);
    if (request.availableEconomyRooms() < 1 && request.availablePremiumRooms() < 1) {
      return List.of();
    }
    var query = new RoomsAvailabilityQuery(request.availablePremiumRooms(),        request.availableEconomyRooms());
    var response = availabilityCalculator.execute(query).stream()
        .map(RoomsAvailabilityResponse::from)
        .toList();
    log.info("Rooms availability response: {}", response);
    return response;
  }
}
