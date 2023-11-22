package com.roommanager.remote.api;

import com.roommanager.domain.services.RoomsAvailabilitySvc;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/rooms")
public class RoomsAvailabilityController {

  private final RoomsAvailabilitySvc roomsAvailabilitySvc;

  public RoomsAvailabilityController(RoomsAvailabilitySvc roomsAvailabilitySvc) {
    this.roomsAvailabilitySvc = roomsAvailabilitySvc;
  }

  @GetMapping("/availability")
  public ResponseEntity<List<RoomsAvailabilityResponse>> getRoomsAvailability(
      @RequestParam(defaultValue = "0") @PositiveOrZero int availablePremiumRooms,
      @RequestParam(defaultValue = "0") @PositiveOrZero int availableEconomyRooms
  ) {
    var response = roomsAvailabilitySvc.calculateAvailability(
        new RoomsAvailabilityRequest(availablePremiumRooms, availableEconomyRooms));
    return ResponseEntity.ok(response);
  }
}
