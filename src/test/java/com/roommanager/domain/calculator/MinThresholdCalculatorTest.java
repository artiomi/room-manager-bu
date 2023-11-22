package com.roommanager.domain.calculator;

import static com.roommanager.domain.model.Currency.EUR;
import static com.roommanager.domain.model.RoomType.ECONOMY;
import static com.roommanager.domain.model.RoomType.PREMIUM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.roommanager.domain.model.Customer;
import com.roommanager.domain.model.RoomsAvailabilityDto;
import com.roommanager.remote.api.RoomsAvailabilityRequest;
import com.roommanager.remote.repositories.CustomerRepo;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MinThresholdCalculatorTest {

  private static final BigDecimal MIN_THRESHOLD = BigDecimal.valueOf(123.45);
  @Mock
  private CustomerRepo customerRepo;
  private MinThresholdCalculator minThresholdCalculator;

  private static List<Customer> premiumCustomersStub() {
    return List.of(new Customer(BigDecimal.valueOf(234.12)),
        new Customer(BigDecimal.valueOf(196.34)),
        new Customer(BigDecimal.valueOf(148)));
  }

  private static List<Customer> economyCustomersStub() {
    return List.of(new Customer(BigDecimal.valueOf(123)),
        new Customer(BigDecimal.valueOf(109.42)),
        new Customer(BigDecimal.valueOf(23.15)));
  }

  @BeforeEach
  void beforeEach() {
    minThresholdCalculator = new MinThresholdCalculator(MIN_THRESHOLD.doubleValue(), customerRepo);
  }

  @Nested
  class ExecuteTest {

    @Test
    void returnEmptyArrayWhenNoPremiumAndEconomyRoomsAvailable() {
      var response = minThresholdCalculator.execute(new RoomsAvailabilityRequest(0, 0));
      assertThat(response).isEmpty();
      verify(customerRepo, never()).findByPriceOfferGTEOrderByPriceOfferDesc(any(BigDecimal.class), anyInt());
      verify(customerRepo, never()).findByPriceOfferLTOrderByPriceOfferDesc(any(BigDecimal.class), anyInt());
    }

    @Test
    void returnArrayWithPremiumAvailabilityWhenOnlyPremiumRoomsAvailable() {
      when(customerRepo.findByPriceOfferGTEOrderByPriceOfferDesc(MIN_THRESHOLD, 3)).thenReturn(premiumCustomersStub());
      var response = minThresholdCalculator.execute(new RoomsAvailabilityRequest(3, 0));
      assertThat(response).size().isEqualTo(1);
      assertThat(response).anyMatch(c -> c.equals(new RoomsAvailabilityDto(PREMIUM, 3, 578.46, EUR)));
      verify(customerRepo, never()).findByPriceOfferLTOrderByPriceOfferDesc(any(BigDecimal.class), anyInt());
    }

    @Test
    void returnArrayWithEconomyAvailabilityWhenOnlyEconomyRoomsAvailable() {
      when(customerRepo.findByPriceOfferLTOrderByPriceOfferDesc(MIN_THRESHOLD, 3)).thenReturn(economyCustomersStub());
      var response = minThresholdCalculator.execute(new RoomsAvailabilityRequest(0, 3));
      assertThat(response).size().isEqualTo(1);
      assertThat(response).anyMatch(c -> c.equals(new RoomsAvailabilityDto(ECONOMY, 3, 255.57, EUR)));
      verify(customerRepo, never()).findByPriceOfferGTEOrderByPriceOfferDesc(any(BigDecimal.class), anyInt());
    }

    @Test
    void returnArrayWithPremiumAndEconomyAvailabilityWhenRoomsOfBothTypeAreAvailable() {
      when(customerRepo.findByPriceOfferGTEOrderByPriceOfferDesc(MIN_THRESHOLD, 3)).thenReturn(premiumCustomersStub());
      when(customerRepo.findByPriceOfferLTOrderByPriceOfferDesc(MIN_THRESHOLD, 3)).thenReturn(economyCustomersStub());
      var response = minThresholdCalculator.execute(new RoomsAvailabilityRequest(3, 3));

      assertThat(response).size().isEqualTo(2);
      assertThat(response).anyMatch(c -> c.equals(new RoomsAvailabilityDto(PREMIUM, 3, 578.46, EUR)));
      assertThat(response).anyMatch(c -> c.equals(new RoomsAvailabilityDto(ECONOMY, 3, 255.57, EUR)));
    }

    @Test
    @DisplayName("no economy customer is moved to premium, even if there are premium rooms available")
    void returnArrayWithPremiumAndEconomyAvailabilityWhenRoomsOfBothTypeAreAvailable2() {
      when(customerRepo.findByPriceOfferGTEOrderByPriceOfferDesc(MIN_THRESHOLD, 5)).thenReturn(premiumCustomersStub());
      when(customerRepo.findByPriceOfferLTOrderByPriceOfferDesc(MIN_THRESHOLD, 9)).thenReturn(economyCustomersStub());
      var response = minThresholdCalculator.execute(new RoomsAvailabilityRequest(5, 7));

      assertThat(response).size().isEqualTo(2);
      assertThat(response).anyMatch(c -> c.equals(new RoomsAvailabilityDto(PREMIUM, 3, 578.46, EUR)));
      assertThat(response).anyMatch(c -> c.equals(new RoomsAvailabilityDto(ECONOMY, 3, 255.57, EUR)));
    }

    @Test
    @DisplayName("no economy customer is moved to premium, if there are enough economy rooms available")
    void returnArrayWithPremiumAndEconomyAvailabilityWhenRoomsOfBothTypeAreAvailable3() {
      when(customerRepo.findByPriceOfferGTEOrderByPriceOfferDesc(MIN_THRESHOLD, 3)).thenReturn(premiumCustomersStub());
      when(customerRepo.findByPriceOfferLTOrderByPriceOfferDesc(MIN_THRESHOLD, 7)).thenReturn(economyCustomersStub());
      var response = minThresholdCalculator.execute(new RoomsAvailabilityRequest(3, 7));

      assertThat(response).size().isEqualTo(2);
      assertThat(response).anyMatch(c -> c.equals(new RoomsAvailabilityDto(PREMIUM, 3, 578.46, EUR)));
      assertThat(response).anyMatch(c -> c.equals(new RoomsAvailabilityDto(ECONOMY, 3, 255.57, EUR)));
    }

    @Test
    @DisplayName("an economy customer with highest price is moved to premium, if all economy rooms are full and there is premium room available")
    void returnArrayWithPremiumAndEconomyAvailabilityWhenRoomsOfBothTypeAreAvailable4() {
      when(customerRepo.findByPriceOfferGTEOrderByPriceOfferDesc(MIN_THRESHOLD, 5)).thenReturn(premiumCustomersStub());
      when(customerRepo.findByPriceOfferLTOrderByPriceOfferDesc(MIN_THRESHOLD, 4)).thenReturn(economyCustomersStub());
      var response = minThresholdCalculator.execute(new RoomsAvailabilityRequest(5, 2));

      assertThat(response).size().isEqualTo(2);
      assertThat(response).anyMatch(c -> c.equals(new RoomsAvailabilityDto(PREMIUM, 4, 701.46, EUR)));
      assertThat(response).anyMatch(c -> c.equals(new RoomsAvailabilityDto(ECONOMY, 2, 132.57, EUR)));
    }
  }
}