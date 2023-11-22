package com.roommanager.services;

import static com.roommanager.domain.model.Currency.EUR;
import static com.roommanager.domain.model.RoomType.ECONOMY;
import static com.roommanager.domain.model.RoomType.PREMIUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roommanager.domain.calculator.AvailabilityCalculator;
import com.roommanager.domain.model.RoomsAvailabilityDto;
import com.roommanager.domain.services.RoomsAvailabilitySvc;
import com.roommanager.remote.api.RoomsAvailabilityRequest;
import com.roommanager.remote.api.RoomsAvailabilityResponse;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RoomsAvailabilitySvcTest {

  @Mock
  private AvailabilityCalculator availabilityCalculator;
  private RoomsAvailabilitySvc roomsAvailabilitySvc;

  @BeforeEach
  void beforeEach() {
    roomsAvailabilitySvc = new RoomsAvailabilitySvc(availabilityCalculator);
  }

  @Nested
  class CalculateAvailabilityTest {

    @Test
    void returnEmptyListForWhenNoAvailableRooms() {
      var response = roomsAvailabilitySvc.calculateAvailability(new RoomsAvailabilityRequest(0, 0));
      assertThat(response).isEmpty();
      verify(availabilityCalculator, never()).execute(any(RoomsAvailabilityRequest.class));
    }

    @Test
    void returnListOfRequestsWhenNoAvailableRooms() {
      RoomsAvailabilityRequest request = new RoomsAvailabilityRequest(5, 12);
      RoomsAvailabilityDto premiumDto = new RoomsAvailabilityDto(PREMIUM, 3, 12.34, EUR);
      RoomsAvailabilityDto economyDto = new RoomsAvailabilityDto(ECONOMY, 8, 172.65, EUR);

      when(availabilityCalculator.execute(request)).thenReturn(List.of(premiumDto, economyDto));
      var response = roomsAvailabilitySvc.calculateAvailability(request);
      assertThat(response).size().isEqualTo(2);
      assertThat(response).anyMatch(matchResponse(premiumDto));
      assertThat(response).anyMatch(matchResponse(economyDto));

    }

    private Predicate<RoomsAvailabilityResponse> matchResponse(RoomsAvailabilityDto dto) {
      return response -> response.roomType().equals(dto.roomType().toString()) &&
                         response.currency().equals(dto.currency().toString()) &&
                         response.customersCount() == dto.customersCount() &&
                         response.totalPrice() == dto.totalPrice();
    }
  }
}