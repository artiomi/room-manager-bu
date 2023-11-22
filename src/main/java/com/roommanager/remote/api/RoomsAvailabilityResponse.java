package com.roommanager.remote.api;

import com.roommanager.domain.model.RoomsAvailabilityDto;

public record RoomsAvailabilityResponse(String roomType, int customersCount, double totalPrice, String currency) {

  public static RoomsAvailabilityResponse from(RoomsAvailabilityDto dto) {
    return new RoomsAvailabilityResponse(dto.roomType().toString(),
        dto.customersCount(),
        dto.totalPrice(),
        dto.currency().toString());
  }
}
