package com.roommanager.domain.services;

import static com.roommanager.domain.model.Currency.EUR;
import static com.roommanager.domain.model.RoomType.ECONOMY;
import static com.roommanager.domain.model.RoomType.PREMIUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roommanager.domain.calculator.AvailabilityCalculator;
import com.roommanager.domain.model.RoomsAvailabilityQuery;
import com.roommanager.domain.model.RoomsAvailabilityResult;
import com.roommanager.remote.api.RoomsAvailabilityRequest;
import com.roommanager.remote.api.RoomsAvailabilityResponse;
import java.util.List;
import java.util.function.Predicate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("empty list is returned, when availability for both room types is zero")
    void returnEmptyList() {
      var response = roomsAvailabilitySvc.calculateAvailability(new RoomsAvailabilityRequest(0, 0));
      assertThat(response).isEmpty();
      verify(availabilityCalculator, never()).execute(any(RoomsAvailabilityQuery.class));
    }

    @Test
    @DisplayName("list with availability for both rooms types is returned, when available count for both provided")
    void returnListOfAvailableRooms() {
      RoomsAvailabilityRequest request = new RoomsAvailabilityRequest(5, 12);
      RoomsAvailabilityQuery query = new RoomsAvailabilityQuery(5, 12);
      RoomsAvailabilityResult premiumResult = new RoomsAvailabilityResult(PREMIUM, 3, 12.34, EUR);
      RoomsAvailabilityResult economyResult = new RoomsAvailabilityResult(ECONOMY, 8, 172.65, EUR);

      when(availabilityCalculator.execute(query)).thenReturn(List.of(premiumResult, economyResult));
      var response = roomsAvailabilitySvc.calculateAvailability(request);
      assertThat(response).size().isEqualTo(2);
      assertThat(response).anyMatch(matchResponse(premiumResult));
      assertThat(response).anyMatch(matchResponse(economyResult));

    }

    private Predicate<RoomsAvailabilityResponse> matchResponse(RoomsAvailabilityResult result) {
      return response -> response.roomType().equals(result.roomType().toString()) &&
                         response.currency().equals(result.currency().toString()) &&
                         response.customersCount() == result.customersCount() &&
                         response.totalPrice() == result.totalPrice();
    }
  }
}