package com.roommanager.remote.api;

import com.roommanager.domain.model.RoomsAvailabilityResult;

public record RoomsAvailabilityResponse(String roomType, int customersCount, double totalPrice, String currency) {

  public static RoomsAvailabilityResponse from(RoomsAvailabilityResult result) {
    return new RoomsAvailabilityResponse(result.roomType().toString(),
        result.customersCount(),
        result.totalPrice(),
        result.currency().toString());
  }
}
