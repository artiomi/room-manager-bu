package com.roommanager.remote.repo;

import com.roommanager.domain.Customer;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryCustomerRepo implements CustomerRepo {

  private static final List<Double> PRICES = List.of(23D, 45D, 155D, 374D, 22D, 99.99D, 100D, 101D, 115D, 209D);
  private List<Customer> customers = null;

  @Override
  public List<Customer> findAll() {
    return customers;
  }

  @PostConstruct
  void postConstruct() {
    customers = PRICES.stream().map(p -> new Customer(BigDecimal.valueOf(p))).toList();
  }
}
