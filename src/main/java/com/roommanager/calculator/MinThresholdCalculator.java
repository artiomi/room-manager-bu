package com.roommanager.calculator;

import static com.roommanager.model.Currency.EUR;
import static com.roommanager.model.RoomType.ECONOMY;
import static com.roommanager.model.RoomType.PREMIUM;

import com.roommanager.model.Customer;
import com.roommanager.model.RoomsAvailabilityDto;
import com.roommanager.remote.api.RoomsAvailabilityRequest;
import com.roommanager.remote.repositories.CustomerRepo;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MinThresholdCalculator implements AvailabilityCalculator {

  private final BigDecimal premiumPriceMinThreshold;
  private final CustomerRepo customerRepo;

  public MinThresholdCalculator(
      @Value("${premium.min-threshold:100}") double premiumPriceMinThreshold,
      CustomerRepo customerRepo) {
    this.premiumPriceMinThreshold = BigDecimal.valueOf(premiumPriceMinThreshold);
    this.customerRepo = customerRepo;
  }

  @Override
  public List<RoomsAvailabilityDto> execute(RoomsAvailabilityRequest request) {
    PremiumRoomsInfo premiumRoomsInfo = getPremiumCustomers(request);
    EconomyRoomsInfo economyRoomsInfo = getEconomyCustomers(request, premiumRoomsInfo.remainingRooms());

    return composeResponse(premiumRoomsInfo, economyRoomsInfo);
  }

  private List<RoomsAvailabilityDto> composeResponse(PremiumRoomsInfo premiumRoomsInfo,
      EconomyRoomsInfo economyRoomsInfo) {
    var premiumPrices = getPremiumAvailability(premiumRoomsInfo, economyRoomsInfo);
    var economyPrices = getEconomyAvailability(economyRoomsInfo);
    List<RoomsAvailabilityDto> response = new ArrayList<>();
    premiumPrices.ifPresent(response::add);
    economyPrices.ifPresent(response::add);

    return Collections.unmodifiableList(response);
  }

  private Optional<RoomsAvailabilityDto> getPremiumAvailability(PremiumRoomsInfo premiumRoomsInfo,
      EconomyRoomsInfo economyRoomsInfo) {
    Collection<Customer> customers = new ArrayList<>(premiumRoomsInfo.customers());
    customers.addAll(economyRoomsInfo.customers().subList(0, economyRoomsInfo.premumCandidates()));
    if (customers.isEmpty()) {
      return Optional.empty();
    }
    double totalPrice = calculateTotalPrice(customers);

    return Optional.of(new RoomsAvailabilityDto(PREMIUM, customers.size(), totalPrice, EUR));

  }

  private Optional<RoomsAvailabilityDto> getEconomyAvailability(EconomyRoomsInfo economyRoomsInfo) {
    List<Customer> customers = economyRoomsInfo.customers();
    List<Customer> reducedCustomers = customers.subList(economyRoomsInfo.premumCandidates(), customers.size());
    if (customers.isEmpty()) {
      return Optional.empty();
    }
    double totalPrice = calculateTotalPrice(reducedCustomers);

    return Optional.of(new RoomsAvailabilityDto(ECONOMY, reducedCustomers.size(), totalPrice, EUR));
  }


  private double calculateTotalPrice(Collection<Customer> customers) {
    return customers.stream()
        .map(Customer::priceOffer)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue();
  }

  private PremiumRoomsInfo getPremiumCustomers(RoomsAvailabilityRequest request) {
    int remainingRooms = 0;
    List<Customer> customers = new ArrayList<>();
    if (request.availablePremiumRooms() > 0) {
      customers = customerRepo.findByPriceOfferGTEOrderByPriceOfferDesc(premiumPriceMinThreshold, request.availablePremiumRooms());
      remainingRooms = Math.max(0, request.availablePremiumRooms() - customers.size());
    }
    return new PremiumRoomsInfo(customers, remainingRooms);
  }

  private EconomyRoomsInfo getEconomyCustomers(RoomsAvailabilityRequest request, int remainingPremiumRooms) {
    int extraCustomers = 0;
    List<Customer> customers = new ArrayList<>();
    if (request.availableEconomyRooms() > 0 || remainingPremiumRooms > 0) {
      int limit = request.availableEconomyRooms() + remainingPremiumRooms;
      customers = customerRepo.findByPriceOfferLTOrderByPriceOfferDesc(premiumPriceMinThreshold, limit);
      extraCustomers = Math.max(0, customers.size() - request.availableEconomyRooms());
    }
    return new EconomyRoomsInfo(customers, extraCustomers);
  }
}

record PremiumRoomsInfo(List<Customer> customers, int remainingRooms) {

}

record EconomyRoomsInfo(List<Customer> customers, int premumCandidates) {

}