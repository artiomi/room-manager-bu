package com.roommanager.domain.calculator;

import static com.roommanager.domain.model.Currency.EUR;
import static com.roommanager.domain.model.RoomType.ECONOMY;
import static com.roommanager.domain.model.RoomType.PREMIUM;

import com.roommanager.domain.model.Customer;
import com.roommanager.domain.model.RoomsAvailabilityQuery;
import com.roommanager.domain.model.RoomsAvailabilityResult;
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
  public List<RoomsAvailabilityResult> execute(RoomsAvailabilityQuery query) {
    PremiumRoomsInfo premiumRoomsInfo = getPremiumCustomers(query);
    EconomyRoomsInfo economyRoomsInfo = getEconomyCustomers(query, premiumRoomsInfo.remainingRooms());

    return composeResponse(premiumRoomsInfo, economyRoomsInfo);
  }

  private List<RoomsAvailabilityResult> composeResponse(PremiumRoomsInfo premiumRoomsInfo,
      EconomyRoomsInfo economyRoomsInfo) {
    var premiumPrices = getPremiumAvailability(premiumRoomsInfo, economyRoomsInfo);
    var economyPrices = getEconomyAvailability(economyRoomsInfo);
    List<RoomsAvailabilityResult> response = new ArrayList<>();
    premiumPrices.ifPresent(response::add);
    economyPrices.ifPresent(response::add);

    return Collections.unmodifiableList(response);
  }

  private Optional<RoomsAvailabilityResult> getPremiumAvailability(PremiumRoomsInfo premiumRoomsInfo,
      EconomyRoomsInfo economyRoomsInfo) {
    Collection<Customer> customers = new ArrayList<>(premiumRoomsInfo.customers());
    customers.addAll(economyRoomsInfo.customers().subList(0, economyRoomsInfo.premiumCandidates()));
    if (customers.isEmpty()) {
      return Optional.empty();
    }
    double totalPrice = calculateTotalPrice(customers);

    return Optional.of(new RoomsAvailabilityResult(PREMIUM, customers.size(), totalPrice, EUR));

  }

  private Optional<RoomsAvailabilityResult> getEconomyAvailability(EconomyRoomsInfo economyRoomsInfo) {
    List<Customer> customers = economyRoomsInfo.customers();
    List<Customer> reducedCustomers = customers.subList(economyRoomsInfo.premiumCandidates(), customers.size());
    if (customers.isEmpty()) {
      return Optional.empty();
    }
    double totalPrice = calculateTotalPrice(reducedCustomers);

    return Optional.of(new RoomsAvailabilityResult(ECONOMY, reducedCustomers.size(), totalPrice, EUR));
  }


  private double calculateTotalPrice(Collection<Customer> customers) {
    return customers.stream()
        .map(Customer::priceOffer)
        .reduce(BigDecimal.ZERO, BigDecimal::add)
        .doubleValue();
  }

  private PremiumRoomsInfo getPremiumCustomers(RoomsAvailabilityQuery query) {
    int remainingRooms = 0;
    List<Customer> customers = new ArrayList<>();
    if (query.availablePremiumRooms() > 0) {
      customers = customerRepo.findByPriceOfferGTEOrderByPriceOfferDesc(premiumPriceMinThreshold,
          query.availablePremiumRooms());
      remainingRooms = Math.max(0, query.availablePremiumRooms() - customers.size());
    }
    return new PremiumRoomsInfo(customers, remainingRooms);
  }

  private EconomyRoomsInfo getEconomyCustomers(RoomsAvailabilityQuery query, int remainingPremiumRooms) {
    int extraCustomers = 0;
    List<Customer> customers = new ArrayList<>();
    if (query.availableEconomyRooms() > 0 || remainingPremiumRooms > 0) {
      int limit = query.availableEconomyRooms() + remainingPremiumRooms;
      customers = customerRepo.findByPriceOfferLTOrderByPriceOfferDesc(premiumPriceMinThreshold, limit);
      extraCustomers = Math.max(0, customers.size() - query.availableEconomyRooms());
    }
    return new EconomyRoomsInfo(customers, extraCustomers);
  }
}

record PremiumRoomsInfo(List<Customer> customers, int remainingRooms) {

}

record EconomyRoomsInfo(List<Customer> customers, int premiumCandidates) {

}